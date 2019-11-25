package com.almosti.mytime;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,listview_fragment.callbackvalue {

    /*定义全局变量*/
    private ArrayList<TimePage> TimeList;
    ListViewAdapter theListAdapter;
    private static final int REQUEST_CODE_NEW_PAGE=10;

    /*定义listviewadapter
     * 重写其中的add，remove，getcount，getitem，removeitem，makeitemview，getview方法*/
    public class ListViewAdapter extends BaseAdapter {
        ArrayList<View> itemViews;

        public ListViewAdapter(ArrayList<TimePage> TimeList){
            itemViews = new ArrayList<View>(TimeList.size());

            //初始化列表
            for (int i=0; i<TimeList.size(); ++i){
                itemViews.add(makeItemView(TimeList.get(i)));
            }
        }

        public void addItem(TimePage page) {
            TimeList.add(page);
            View view=makeItemView(page);
            itemViews.add(view);

        }

        public void removeItem(int positon){
            itemViews.remove(positon);
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
            int timeDistance=page.getTimeDistance().get(Calendar.DATE);
            String timeDistanceString;
            if(page.getTimeDistanceSign()){
                timeDistanceString="还剩\n"+timeDistance+"天";
            }else{
                timeDistanceString="已经\n"+(-timeDistance)+"天";
            }
            dateText.setText(timeDistanceString);

            //在有内容的页面显示中要获取行数并只显示最多三行
            TextView contentText = (TextView) itemView.findViewById(R.id.timeContent);
            String contentString=page.getTitle()+"\n"+page.getYear()+"年"+page.getMonth()+"月"+page.getDay()+"日";
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
        if(TimeList==null){
            TimeList=new ArrayList<>();
        }
        theListAdapter=new ListViewAdapter(TimeList);
        final listview_fragment fragment = new listview_fragment(theListAdapter);
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container,fragment);
        fragmentTransaction.commit();
    }

    /*实现回调函数
     * 通过这个函数获取listview点击的项目*/
    @Override
    public void sendvalue(int selecteditem) {
        TimePage page = TimeList.get(selecteditem);
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

    //去除右上角设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*创建activity的返回值*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_NEW_PAGE:
                if (resultCode == RESULT_OK){
                    Bundle bundle=data.getExtras();
                    TimePage page;
                    if(bundle.getSerializable("TimePage")!=null){
                        page = (TimePage) bundle.getSerializable("TimePage");
                    }else{
                        Toast.makeText(MainActivity.this, "添加新倒计时产生错误！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    theListAdapter.addItem(page);
                    theListAdapter.notifyDataSetChanged();
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
}
