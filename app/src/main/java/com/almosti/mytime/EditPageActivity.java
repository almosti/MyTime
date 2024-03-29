package com.almosti.mytime;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.almosti.mytime.MainActivity.MSG_TIME;
import static com.almosti.mytime.MainActivity.REQUEST_CODE_EDIT_PAGE_DATA;
import static com.almosti.mytime.MainActivity.RESULT_DELETE;

public class EditPageActivity extends AppCompatActivity {

    private TimePage page;
    private int position;
    private Timer timer;
    private TimerTask timerTask;
    private TextView timerText;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_TIME:
                    RefreshUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        timerText = findViewById(R.id.edit_timer_text);

        InitToolBar(toolbar);
        InitData();

    }

    //根据page对象更新本页显示的数据
    private void RefreshUI(){
        if(page!=null){
            //转化为秒数
            long time = page.getTimeDistance() / 1000;
            //剩余天数
            int remainingDay = (int) time / (3600 * 24);
            time %= (3600 * 24);
            //剩余小时数
            int remainingHour = (int) time / 3600;
            time %= 3600;
            //剩余分钟数
            int remainingMinute = (int) time / 60;
            time %= 60;

            String mainTitle="<big>"+page.getTitle()+"</big>";
            String mainTime=page.getYear()+"年"+(page.getMonth()+1)+"月"+page.getDay()+"日";
            String mainTimer = remainingDay + "天" + remainingHour + "小时" + remainingMinute + "分钟" + time + "秒";
            timerText.setText(Html.fromHtml(mainTitle+"<br>"+ mainTime+"<br>"+mainTimer,Html.FROM_HTML_MODE_COMPACT));
        }
    }

    //初始化数据
    private void InitData(){
        //从bundle中获取所选页面数据
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            page = (TimePage) bundle.getSerializable("TimePage");
            position = bundle.getInt("Position");
            int themeColor = bundle.getInt("Color");
            if (themeColor != 0) {
                Window window = getWindow();
                window.setStatusBarColor(themeColor);
            }
        }
        //设置倒计时
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
        //设置背景
        if(page!=null){
            if(page.getImagePath()!=null){
                File f = new File(page.getImagePath());
                Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
                timerText.setBackground(drawable);
            }else if(page.getDrawableID()!=-1){
                Drawable drawable = getDrawable(page.getDrawableID());
                timerText.setBackground(drawable);
            }
        }

        //设置通知栏，这里只改变了属性，具体操作在更新时进行
        Switch reminder_switch = findViewById(R.id.edit_reminder_switch);
        reminder_switch.setChecked(page.canNotification());
        reminder_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page.setNotification(isChecked);
            }
        });
        //设置快捷方式
        Switch shortcut_switch = findViewById(R.id.edit_shortcut_switch);
        shortcut_switch.setChecked(page.hasDesktop());
        shortcut_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page.setDesktop(isChecked);
                if (isChecked) {
                    ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);//获取shortcutManager
                    if(shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()){
                        Intent shortCutIntent = new Intent(EditPageActivity.this,MainActivity.class);//快捷方式启动页面
                        shortCutIntent.setAction(Intent.ACTION_VIEW);
                        //快捷方式创建相关信息。图标名字 id
                        ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(EditPageActivity.this,"shortcutid")
                                .setIcon(Icon.createWithResource(EditPageActivity.this,R.drawable.icon))
                                .setShortLabel("MyTime")
                                .setIntent(shortCutIntent)
                                .build();
                        //创建快捷方式时候回调
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(EditPageActivity.this,0,new
                                Intent(EditPageActivity.this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
                        shortcutManager.requestPinShortcut(shortcutInfo,pendingIntent.getIntentSender());
                    }
                }
            }
        });
        //设置悬浮窗口
        /*Switch floating_switch = findViewById(R.id.edit_floating_switch);
        floating_switch.setChecked(page.canFloating());
        floating_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page.setFloating(isChecked);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_PAGE_DATA:
                if(resultCode==RESULT_OK){
                    if(data!=null){
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            page = (TimePage) bundle.getSerializable("TimePage");
                            Intent intent = getIntent();
                            Bundle bundle1 = new Bundle();
                            bundle1.putInt("Position", position);
                            bundle1.putSerializable("TimePage", page);
                            intent.putExtras(bundle1);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
                break;
        }
    }

    //需要在重写toolbar中完成menu的绑定，否则不会正常显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_page,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //为toolbar中各按钮添加监听
    private void InitToolBar(Toolbar toolbar){
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    //删除该项倒计时，需要返回位置信息
                    case R.id.edit_toolbar_delete:
                        Intent intentToDelete=getIntent();
                        Bundle bundle = new Bundle();
                        bundle.putInt("Position", position);
                        intentToDelete.putExtras(bundle);
                        setResult(RESULT_DELETE, intentToDelete);
                        finish();
                        break;
                    //将该倒计时用文本方式分享出去
                    case R.id.edit_toolbar_share:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT,page.getTitle()+"："+page.getYear()+"年"+(page.getMonth()+1)+"月"+page.getDay()+"日");
                        intent.setType("text/plain");
                        //调用Intent.createChooser()这个方法，此时即使用户之前为这个intent设置了默认，选择界面还是会显示
                        startActivity(Intent.createChooser(intent,"选择分享应用"));
                        break;
                    //编辑该倒计时，需要传入page信息，也要在onActivityResult处理返回信息
                    case R.id.edit_toolbar_edit:
                        Intent intentToEdit = new Intent(EditPageActivity.this, NewPageActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putSerializable("TimePage", page);
                        intentToEdit.putExtras(bundle1);
                        startActivityForResult(intentToEdit, REQUEST_CODE_EDIT_PAGE_DATA);
                        break;
                }
                return true;
            }
        });

        //显示左侧返回栏,并设置监听
       if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("Position", position);
                    bundle.putSerializable("TimePage", page);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }


}
