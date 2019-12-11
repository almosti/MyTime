package com.almosti.mytime;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /*定义全局变量*/
    static final int RESULT_DELETE = 2;
    private ArrayList<TimePage> TimeList;
    private ListViewAdapter theListAdapter;
    private ListView theListView;
    private static final int REQUEST_CODE_NEW_PAGE=10;
    private static final int REQUEST_CODE_EDIT_PAGE=11;
    private static final int REQUEST_EXTERNAL_STORAGE = 10;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    /*定义listviewadapter
     * 重写其中的add，remove，getcount，getitem，removeitem，makeitemview，getview方法*/
    public class ListViewAdapter extends BaseAdapter {
        ArrayList<View> itemViews;

        ListViewAdapter(ArrayList<TimePage> TimeList){
            itemViews = new ArrayList<View>(TimeList.size());

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

        public void removeItem(int positon){
            itemViews.remove(positon);
            TimeListOperator operator=new TimeListOperator();
            operator.save(MainActivity.this.getBaseContext(),TimeList);
            TimeList.remove(positon);
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
            TextView dateText = (TextView) itemView.findViewById(R.id.timeDistance);
            int timeDistance = (int)page.getTimeDistance() / (1000 * 3600 * 24);
            String timeDistanceString;
            if(page.getTimeDistanceSign()){
                timeDistanceString="还剩\n"+timeDistance+"天";
            }else{
                timeDistanceString="已经\n"+timeDistance+"天";
            }
            dateText.setText(timeDistanceString);
            if(page.getImagePath()!=null){
                File f = new File(page.getImagePath());
                Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
                dateText.setBackground(drawable);
            }else {
                //TODO:添加默认图片
            }
            TextView contentText = (TextView) itemView.findViewById(R.id.timeContent);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new AddNewPageListener());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //初始化计时器数据列表
        //当无保存数据时重新生成一份列表
        TimeListOperator operator=new TimeListOperator();
        TimeList=operator.load(getBaseContext());
        if(TimeList==null){
            TimeList=new ArrayList<>();
        }
        theListAdapter=new ListViewAdapter(TimeList);
        theListView = findViewById(R.id.time_list);
        theListView.setAdapter(theListAdapter);

        theListView.setOnItemClickListener(new EditPageListener());
    }

    //返回键优先关闭左侧滑动菜单
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                        }
                    }
                }
                //暂无处理
                if(resultCode==RESULT_CANCELED){

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
                        }
                    }
                }
                //暂无处理
                if(resultCode==RESULT_CANCELED){

                }
                if(resultCode==RESULT_DELETE){
                    if(data==null){
                        return;
                    }
                    Bundle bundle = data.getExtras();
                    int position = bundle.getInt("Position");
                    if (position != 0) {
                        --position;
                        theListAdapter.removeItem(position);
                        theListAdapter.notifyDataSetChanged();
                    }

                }
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //添加新计时器项
    class AddNewPageListener implements View.OnClickListener{
        public void onClick(View view) {
            TimePage page=new TimePage();
            Intent intent =new Intent(MainActivity.this,NewPageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("TimePage",page);
            intent.putExtras(bundle);
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
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                    return path;
                } else {
                    String path = "/storage/".concat(type).concat("/").concat(divide[1]);
                    return path;
                }
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                // 下載目錄
                final String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    final String path = docId.replaceFirst("raw:", "");
                    return path;
                }
                final Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                String path = queryAbsolutePath(context, downloadUri);
                return path;
            } else if ("com.android.providers.media.documents".equals(authority)) {
                // 圖片、影音檔案
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                Uri mediaUri = null;
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
                String path = queryAbsolutePath(context, mediaUri);
                return path;
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
            if (cursor != null) {
                cursor.close();
            }
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
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PAGE);
        }
    }
}
