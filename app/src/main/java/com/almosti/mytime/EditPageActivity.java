package com.almosti.mytime;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import static com.almosti.mytime.MainActivity.RESULT_DELETE;

public class EditPageActivity extends AppCompatActivity {

    TimePage page;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitToolBar(toolbar);
        InitData();

    }

    //初始化数据
    private void InitData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        page = (TimePage) bundle.getSerializable("TimePage");
        position = bundle.getInt("Position");
    }

    //需要在重写toolbar中完成menu的绑定，否则不会正常显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_page,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //为toolbar中各按钮添加监听
    private void InitToolBar(Toolbar toolbar){
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.edit_toolbar_delete:
                        Intent intent=getIntent();
                        Bundle bundle = new Bundle();
                        bundle.putInt("Position", position);
                        intent.putExtras(bundle);
                        setResult(RESULT_DELETE, intent);
                        finish();
                        break;
                    case R.id.edit_toolbar_share:
                        break;
                    case R.id.edit_toolbar_edit:
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
                    finish();
                }
            });
        }
    }


}
