package com.almosti.mytime;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TimePage page;
    private SeekBar mSbColor;
    int mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.setting_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        InitData();
    }

    private void InitData(){
        //获取数据
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                page = (TimePage) bundle.getSerializable("TimePage");
                mColor = bundle.getInt("Color");
            }
        }
        //隐藏标题
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        //设置主题色
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        toolbar.setBackgroundColor(mColor);
        Window window = getWindow();
        window.setStatusBarColor(mColor);
        NavigationView navigationView = findViewById(R.id.setting_nav_view);
        View view = navigationView.getHeaderView(0);
        view.setBackgroundColor(mColor);
        Button saveBtn = findViewById(R.id.setting_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saveIntent = getIntent();
                Bundle saveBundle = new Bundle();
                //避免使用getInt的默认值
                if (mColor == 0) {
                    mColor++;
                }
                saveBundle.putInt("Color", mColor);
                saveIntent.putExtras(saveBundle);
                setResult(RESULT_OK, saveIntent);
                finish();
            }
        });
        ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(0, 0, width, height, ColorPickGradient.PICKCOLORBAR_COLORS, ColorPickGradient.PICKCOLORBAR_POSITIONS, Shader.TileMode.REPEAT);
            }
        };
        mSbColor = findViewById(R.id.setting_color);
        PaintDrawable paint = new PaintDrawable();
        paint.setShape(new RectShape());
        paint.setCornerRadius(10);
        paint.setShaderFactory(shaderFactory);
        mSbColor.setProgressDrawable(paint);
        mSbColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float radio = (float)progress / mSbColor.getMax();
                ColorPickGradient mColorPickGradient = new ColorPickGradient();
                mColor = mColorPickGradient.getColor(radio);
                Toolbar toolbar = findViewById(R.id.setting_toolbar);
                toolbar.setBackgroundColor(mColor);
                Window window1 = getWindow();
                window1.setStatusBarColor(mColor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main_page) {
            Intent intent = getIntent();
            setResult(RESULT_CANCELED, intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Log.d("2", "");
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,"MyTime App");
            intent.setType("text/plain");
            //调用Intent.createChooser()这个方法，此时即使用户之前为这个intent设置了默认，选择界面还是会显示
            startActivity(Intent.createChooser(intent,"选择分享应用"));
        } else if (id == R.id.nav_send) {
            if (page != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);

                intent.putExtra(Intent.EXTRA_TEXT,page.getTitle()+"："+page.getYear()+"年"+(page.getMonth()+1)+"月"+page.getDay()+"日");
                intent.setType("text/plain");
                //调用Intent.createChooser()这个方法，此时即使用户之前为这个intent设置了默认，选择界面还是会显示
                startActivity(Intent.createChooser(intent,"选择分享应用"));
            }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
