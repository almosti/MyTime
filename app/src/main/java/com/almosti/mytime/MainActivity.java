package com.almosti.mytime;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /*定义全局变量*/
    static final int RESULT_DELETE = 2;
    static final int REQUEST_CODE_NEW_PAGE=10;
    static final int REQUEST_CODE_EDIT_PAGE=11;
    static final int REQUEST_CODE_EDIT_PAGE_DATA=12;
    static final int REQUEST_EXTERNAL_STORAGE = 15;
    static final int REQUEST_CODE_SETTINGS=14;
    static final int MSG_TIME = 13;
    static final String FILENAME = "Settings";
    private ArrayList<TimePage> TimeList;
    private ListViewAdapter theListAdapter;
    private TimePage currentPage;
    private TextView mainTimerText;
    private Timer timer;
    private TimerTask timerTask;
    private int themeColor;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_TIME:
                    RefreshTimer();
                    break;
            }
        }
    };
    /*定义listviewadapter
     * 重写其中的add，remove，getcount，getitem，removeitem，makeitemview，getview方法*/
    public class ListViewAdapter extends BaseAdapter {
        ArrayList<View> itemViews;

        ListViewAdapter(ArrayList<TimePage> TimeList){
            itemViews = new ArrayList<>(TimeList.size());
            //初始化列表
            for (int i=0; i<TimeList.size(); ++i){
                itemViews.add(makeItemView(TimeList.get(i)));
            }
        }

        void addItem(TimePage page) {
            TimeList.add(page);
            TimeListOperator operator=new TimeListOperator();
            operator.save(MainActivity.this.getBaseContext(),TimeList);
            View view=makeItemView(page);
            itemViews.add(view);
        }

        void removeItem(int positon){
            TimeList.remove(positon);
            TimeListOperator operator=new TimeListOperator();
            operator.save(MainActivity.this.getBaseContext(),TimeList);
            itemViews.remove(positon);
        }

        public int getCount() {
            return itemViews.size();
        }

        public View getItem(int position) {
            return itemViews.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        //重写listview显示方式
        private View makeItemView(TimePage page) {
            LayoutInflater inflater = (LayoutInflater)MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 使用View的对象itemView与R.layout.item关联
            View itemView = inflater.inflate(R.layout.time_list_item, null);
            // 通过findViewById()方法实例R.layout.item内各组件
            TextView dateText = itemView.findViewById(R.id.timeDistance);
            //用相隔毫秒数计算相隔天数
            int timeDistance = (int)page.getTimeDistance() / (1000 * 3600 * 24);
            String timeDistanceString;
            if(page.getTimeDistanceSign()){
                timeDistanceString="还剩\n"+timeDistance+"天";
            }else{
                timeDistanceString="已经\n"+timeDistance+"天";
            }
            dateText.setText(timeDistanceString);
            //有自定义图片优先使用自定义图片，没有再用默认图片
            if(page.getImagePath()!=null){
                File f = new File(page.getImagePath());
                Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
                dateText.setBackground(drawable);
            }else if(page.getDrawableID()!=-1){
                Drawable drawable = getDrawable(page.getDrawableID());
                dateText.setBackground(drawable);
            }
            //正常无法执行到此处，但为了在图片加载出现错误时仍能显示正常，需要将字体颜色设回黑色
            else {
                dateText.setTextColor(getColor(R.color.black));
            }
            TextView contentText = itemView.findViewById(R.id.timeContent);
            String contentString=page.getTitle()+"\n"+page.getYear()+"年"+(page.getMonth()+1)+"月"+page.getDay()+"日";
            contentText.setText(contentString);
            return itemView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return itemViews.get(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);
        InitComponent();

    }

    //初始化各组件，单独编写使逻辑更清晰
    private void InitComponent(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new AddNewPageListener());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //初始化计时器数据列表
        //当无保存数据时重新生成一份列表
        TimeListOperator operator=new TimeListOperator();
        TimeList=operator.load(getBaseContext());
        if(TimeList==null){
            TimeList=new ArrayList<>();
        }
        theListAdapter=new ListViewAdapter(TimeList);
        ListView theListView = findViewById(R.id.time_list);
        theListView.setAdapter(theListAdapter);

        theListView.setOnItemClickListener(new EditPageListener());

        //设置主题色
        FileInputStream fis;
        try{
            fis = openFileInput(FILENAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            String s = bufferedReader.readLine();
            themeColor = Integer.valueOf(s);
        }catch (FileNotFoundException e) {
            themeColor = getColor(R.color.colorPrimary);
        }catch (IOException e) {
            themeColor = getColor(R.color.colorPrimary);
            e.printStackTrace();
        }
        setThemeColor();
        //初始默认将焦点设置为第一项,放在设置主题色之后，覆盖toolbar颜色
        setMainTimer(0);

        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    //返回键优先关闭左侧滑动菜单
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //创建activity的返回值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_NEW_PAGE:
                //添加新页面，直接使用page对象作为传递值实现了易扩展性
                if (resultCode == RESULT_OK){
                    if (data == null) {
                        Log.d("1", "null pointer intent");
                        return;
                    }
                    Bundle bundle=data.getExtras();
                    TimePage page;
                    if(bundle!=null){
                        page = (TimePage) bundle.getSerializable("TimePage");
                        if(page!=null){
                            if(!page.isValid()){
                                return;
                            }
                            theListAdapter.addItem(page);
                            theListAdapter.notifyDataSetChanged();
                            setMainTimer(TimeList.size() - 1);
                        }
                    }
                }
                //暂无处理
                if(resultCode==RESULT_CANCELED){
                    Log.d("2", "添加取消");
                }
                break;
            case REQUEST_CODE_EDIT_PAGE:
                //编辑页面
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        Log.d("1", "null pointer intent");
                        return;
                    }
                    Bundle bundle=data.getExtras();
                    TimePage page;
                    int position;
                    if(bundle!=null) {
                        page = (TimePage) bundle.getSerializable("TimePage");
                        //根据源码可知getInt的默认值为0，为了保证获取该值不出错，须避免使用默认值
                        position = bundle.getInt("Position");
                        if (page != null&&position!=0) {
                            if (!page.isValid()) {
                                return;
                            }
                            //复原该值
                            --position;
                            theListAdapter.removeItem(position);
                            theListAdapter.addItem(page);
                            theListAdapter.notifyDataSetChanged();
                            setMainTimer(TimeList.size() - 1);
                        }
                    }
                }
                //暂无处理
                if(resultCode==RESULT_CANCELED){
                    Log.d("2", "修改取消");
                }
                if(resultCode==RESULT_DELETE){
                    if(data==null){
                        return;
                    }
                    Bundle bundle = data.getExtras();
                    if(bundle!=null) {
                        int position = bundle.getInt("Position");
                        if (position != 0) {
                            --position;
                            theListAdapter.removeItem(position);
                            theListAdapter.notifyDataSetChanged();
                            setMainTimer(0);
                        }
                    }
                }
                break;
            case REQUEST_CODE_SETTINGS:
                if(resultCode==RESULT_OK){
                    if(data==null){
                        return;
                    }
                    Bundle bundle = data.getExtras();
                    if(bundle!=null) {
                        if (bundle.getInt("Color") != 0) {
                            themeColor = bundle.getInt("Color");
                            setThemeColor();
                            FileOutputStream fos = null;
                            try {
                                //文件路径  /data/data/com.example.myapplication/files/
                                fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                                fos.write(String.valueOf(themeColor).getBytes());
                                fos.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    Log.d("2", "设置取消");
                }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main_page) {
            Log.d("2", "");
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("TimePage", currentPage);
            bundle.putInt("Color",themeColor);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,"MyTime App");
            intent.setType("text/plain");
            //调用Intent.createChooser()这个方法，此时即使用户之前为这个intent设置了默认，选择界面还是会显示
            startActivity(Intent.createChooser(intent,"选择分享应用"));
        } else if (id == R.id.nav_send) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,currentPage.getTitle()+"："+currentPage.getYear()+"年"+(currentPage.getMonth()+1)+"月"+currentPage.getDay()+"日");
            intent.setType("text/plain");
            //调用Intent.createChooser()这个方法，此时即使用户之前为这个intent设置了默认，选择界面还是会显示
            startActivity(Intent.createChooser(intent,"选择分享应用"));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //添加新计时器项
    class AddNewPageListener implements View.OnClickListener{
        public void onClick(View view) {
            Intent intent =new Intent(MainActivity.this,NewPageActivity.class);
            startActivityForResult(intent,REQUEST_CODE_NEW_PAGE);
        }
    }

    //从uri中读取真实路径
    public static String getPathFromUri(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }
        // 判斷是否為Android 4.4之後的版本
        final boolean after44 = Build.VERSION.SDK_INT >= 19;
        if (after44 && DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是Android 4.4之後的版本，而且屬於文件URI
            final String authority = uri.getAuthority();
            // 判斷Authority是否為本地端檔案所使用的
            if ("com.android.externalstorage.documents".equals(authority)) {
                // 外部儲存空間
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                if ("primary".equals(type)) {
                    return Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                } else {
                    return "/storage/".concat(type).concat("/").concat(divide[1]);
                }
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                // 下載目錄
                final String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    return docId.replaceFirst("raw:", "");
                }
                final Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                return queryAbsolutePath(context, downloadUri);
            } else if ("com.android.providers.media.documents".equals(authority)) {
                // 圖片、影音檔案
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                Uri mediaUri;
                if ("image".equals(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                return queryAbsolutePath(context, mediaUri);
            }
        } else {
            // 如果是一般的URI
            final String scheme = uri.getScheme();
            String path = null;
            if ("content".equals(scheme)) {
                // 內容URI
                path = queryAbsolutePath(context, uri);
            } else if ("file".equals(scheme)) {
                // 檔案URI
                path = uri.getPath();
            }
            return path;
        }
        return null;
    }
    public static String queryAbsolutePath(final Context context, final Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(index);
            }
        } catch (final Exception ex) {
            if(cursor==null){
                return uri.getPath();
            }
            ex.printStackTrace();
                cursor.close();
        }
        return null;
    }

    //确认存储权限已获取
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //修改计时器项
    class EditPageListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TimePage page = TimeList.get(position);
            Intent intent = new Intent(MainActivity.this, EditPageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("TimePage", page);
            //根据源码可知getInt的默认值为0，为了保证获取该值不出错，须避免使用默认值
            bundle.putInt("Position", ++position);
            bundle.putInt("Color", themeColor);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PAGE);
        }
    }

    //设置主倒计时焦点
    private void setMainTimer(int position){
        if (TimeList.isEmpty()) {
            return;
        }
        currentPage = TimeList.get(position);
        mainTimerText = findViewById(R.id.main_timer_text);
        if(currentPage.getImagePath()!=null){
            File f = new File(currentPage.getImagePath());
            Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
            mainTimerText.setBackground(drawable);
        }else if(currentPage.getDrawableID()!=-1){
            Drawable drawable = getDrawable(currentPage.getDrawableID());
            mainTimerText.setBackground(drawable);
        }
        if(timer==null&&timerTask==null){
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = MSG_TIME;
                    handler.sendMessage(message);
                }
            };
            if (timer != null) {
                timer.scheduleAtFixedRate(timerTask, 1000, 1000);
            }
        }
    }

    //根据剩余时间刷新显示
    private void RefreshTimer(){
        //当倒计时列表为空时清除内容
        if(TimeList.isEmpty()){
            mainTimerText.setText(getString(R.string.default_main_text));
            mainTimerText.setBackgroundColor(getColor(R.color.colorPrimary));
            return;
        }

        //转化为秒数
        long time = currentPage.getTimeDistance() / 1000;
        //剩余天数
        int remainingDay = (int) time / (3600 * 24);
        time %= (3600 * 24);
        //剩余小时数
        int remainingHour = (int) time / 3600;
        time %= 3600;
        //剩余分钟数
        int remainingMinute = (int) time / 60;
        time %= 60;

        //设置重复，当到达该日期后重置时间为下一个周期始
        if(currentPage.getCycle()>0&&!currentPage.getTimeDistanceSign()){
            Calendar calendar = Calendar.getInstance();
            calendar.set(currentPage.getYear(), currentPage.getMonth(), currentPage.getDay(), currentPage.getHour(), currentPage.getMinute());
            //循环以应对用户更改系统时间的情况
            while(!currentPage.getTimeDistanceSign()){
                calendar.add(Calendar.DATE, currentPage.getCycle());
            }
        }

        //设置主倒计时文字
        String mainTitle="<big>"+currentPage.getTitle()+"</big>";
        String mainTime=currentPage.getYear()+"年"+(currentPage.getMonth()+1)+"月"+currentPage.getDay()+"日";
        String mainTimer = remainingDay + "天" + remainingHour + "小时" + remainingMinute + "分钟" + time + "秒";
        mainTimerText.setText(Html.fromHtml(mainTitle+"<br>"+ mainTime+"<br>"+mainTimer,Html.FROM_HTML_MODE_COMPACT));

    }

    private void setThemeColor(){
        TextView textView = findViewById(R.id.main_timer_text);
        textView.setBackgroundColor(themeColor);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        view.setBackgroundColor(themeColor);
        Window window = getWindow();
        window.setStatusBarColor(themeColor);
    }
}
