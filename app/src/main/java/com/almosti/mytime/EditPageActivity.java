package com.almosti.mytime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import static com.almosti.mytime.MainActivity.REQUEST_CODE_EDIT_PAGE_DATA;
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
        RefreshUI();
    }

    //根据page对象更新本页显示的数据
    private void RefreshUI(){

    }

    //初始化数据
    private void InitData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            page = (TimePage) bundle.getSerializable("TimePage");
            position = bundle.getInt("Position");
        }

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
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.edit_toolbar_delete:
                        Intent intentToDelete=getIntent();
                        Bundle bundle = new Bundle();
                        bundle.putInt("Position", position);
                        intentToDelete.putExtras(bundle);
                        setResult(RESULT_DELETE, intentToDelete);
                        finish();
                        break;
                    case R.id.edit_toolbar_share:
                        //TODO:添加分享功能
                        break;
                    case R.id.edit_toolbar_edit:
                        Intent intentToEdit = new Intent();
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
                    finish();
                }
            });
        }
    }


}
