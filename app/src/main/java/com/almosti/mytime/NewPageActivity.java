package com.almosti.mytime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import static com.almosti.mytime.MainActivity.FILENAME;
import static com.almosti.mytime.MainActivity.verifyStoragePermissions;

public class NewPageActivity extends AppCompatActivity {

    private TimePage page;
    private TextView editTimeText;
    private Calendar targetCalendar;
    private int cycle=-1;
    private String imagePath;
    private Calendar futureCalendar;
    private Calendar pastCalendar;
    private static final int QUEST_IMAGE=1;
    private static final int CAL_FUTURE=2;
    private static final int CAL_PAST=3;
    //默认图片集
    private int defaultPictures[]={
      R.drawable.default_picture1,
      R.drawable.default_picture2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_page);
        //去除标题栏
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        //提前确认已经获取权限
        verifyStoragePermissions(NewPageActivity.this);

        FloatingActionButton fabCancel=findViewById(R.id.edit_cancel);
        FloatingActionButton fabSave = findViewById(R.id.edit_save);
        final EditText editTitle = findViewById(R.id.edit_title);
        final EditText editRemark = findViewById(R.id.edit_remark);
        View editTime=findViewById(R.id.edit_time_section);
        View editRepeat = findViewById(R.id.edit_repeat_section);
        View editImage=findViewById(R.id.edit_image_section);
        editTimeText=findViewById(R.id.edit_time);
        targetCalendar=Calendar.getInstance();

        ConstraintLayout constraintLayout = findViewById(R.id.edit_above_background);
        //设置主题色
        FileInputStream fis;
        int themeColor;
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
        constraintLayout.setBackgroundColor(themeColor);
        Window window = getWindow();
        window.setStatusBarColor(themeColor);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            page = (TimePage) bundle.getSerializable("TimePage");
            //处理从修改页进入的情况
            if (page != null) {
                editTitle.setText(page.getTitle());
                editRemark.setText(page.getRemark());
                if(page.getImagePath()!=null){
                    File f = new File(page.getImagePath());
                    Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
                    constraintLayout.setBackground(drawable);
                }else if(page.getDrawableID()!=-1){
                    Drawable drawable = getDrawable(page.getDrawableID());
                    constraintLayout.setBackground(drawable);
                }
                editTimeText.setText(String.format(getString(R.string.set_edit_time), page.getYear(), page.getMonth() + 1, page.getDay(), page.getHour(), page.getMinute()));
                if (page.getCycle() != -1) {
                    TextView cycleText = findViewById(R.id.edit_repeat);
                    cycleText.setText(String.valueOf(page.getCycle()));
                }
            }
        }else{
            page=new TimePage();
        }

        //取消按钮
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=getIntent();
                setResult(RESULT_CANCELED,intent);
                finish();
            }
        });
        //保存按钮
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page.setTitle(editTitle.getText().toString());
                page.setRemark(editRemark.getText().toString());
                page.setYear(targetCalendar.get(Calendar.YEAR));
                page.setMonth(targetCalendar.get(Calendar.MONTH));
                page.setDay(targetCalendar.get(Calendar.DATE));
                page.setHour(targetCalendar.get(Calendar.HOUR_OF_DAY));
                page.setMinute(targetCalendar.get(Calendar.MINUTE));
                page.setCycle(cycle);
                //由于uri数据过长，直接序列化传输会导致原活动页重启，这里使用string进行传输
                if (imagePath != null) {
                    page.setImagePath(imagePath);
                    //page.setPictureID(imageURI);
                }
                //否则使用默认图片
                else {
                    //随机一个默认图片
                    int id = defaultPictures[(int)(Math.random() * defaultPictures.length)];
                    page.setDrawableID(id);
                }
                if(!page.isValid()){
                    Toast.makeText(getBaseContext(), "输入的倒计时无效,请检查标题与所选日期", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent=getIntent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("TimePage", page);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        //点击编辑时间部分
        editTime.setOnClickListener(new SetTimeByCalendar());
        //长按显示时间计算器,返回true消除点击时长在长按与单击之间的情况
        editTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SetTimeByCalculator();
                return true;
            }
        });
        editRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView cycleText = findViewById(R.id.edit_repeat);
                final String[] items={"每周","每月","每年","自定义"};
                AlertDialog.Builder builder = new AlertDialog.Builder(NewPageActivity.this);
                builder.setTitle("周期");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                cycle=7;
                                cycleText.setText("每周");
                                break;
                            case 1:
                                cycle=30;
                                cycleText.setText("每月");
                                break;
                            case 2:
                                cycle=365;
                                cycleText.setText("每年");
                                break;
                            case 3:
                                CustomizeCycle();
                                cycleText.setText("自定义");
                                break;
                                default:
                        }

                    }
                });
                builder.create().show();
            }
        });
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,QUEST_IMAGE);
            }
        });
    }

    //自定义编辑对话框实现自定义周期
    private void CustomizeCycle(){
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(NewPageActivity.this);
        View view = LayoutInflater.from(NewPageActivity.this).inflate(R.layout.edit_dialog, null, false);
        editDialogBuilder.setView(view);
        final Dialog editDialog=editDialogBuilder.create();
        final EditText editText=view.findViewById(R.id.edit_dialog_edit);
        Button confirm = view.findViewById(R.id.edit_dialog_confirm);
        Button cancel=view.findViewById(R.id.edit_dialog_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText() == null ? "" : editText.getText().toString();
                if (s.isEmpty() || s.length() > 3 || Integer.valueOf(s) <= 0) {
                    Toast.makeText(getBaseContext(), "请输入合理周期", Toast.LENGTH_LONG).show();
                    return;
                }
                cycle=Integer.valueOf(s);
                editDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog.dismiss();
            }
        });
        editDialog.show();
    }

    //使用官方datepicker和timepicker的对话框进行日期时间的设置
    class SetTimeByCalendar implements View.OnClickListener{
        @Override
        public void onClick(View v){
            final Calendar calendar=Calendar.getInstance();
            DatePickerDialog datePickerDialog=new DatePickerDialog(NewPageActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                            TimePickerDialog timePickerDialog=new TimePickerDialog(NewPageActivity.this,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                            editTimeText.setText(String.format(getString(R.string.set_edit_time), year, month + 1, dayOfMonth, hourOfDay, minute));
                                            targetCalendar.set(year,month,dayOfMonth,hourOfDay,minute);
                                        }
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true);
                            timePickerDialog.show();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE));
            datePickerDialog.show();
        }
    }

    //自定义对话框实现日期计算器
    private void SetTimeByCalculator(){

        View view= LayoutInflater.from(this).inflate(R.layout.time_calculator_dialog,null,false);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(view).create();

        TextView dialog_edit_time_text=view.findViewById(R.id.dialog_edit_time_text);
        EditText dialog_edit_past_time=view.findViewById(R.id.dialog_edit_past_time);
        final TextView dialog_edit_past_time_text=view.findViewById(R.id.dialog_edit_past_time_text);
        TextView dialog_edit_past_time_select=view.findViewById(R.id.dialog_edit_past_time_select);
        EditText dialog_edit_future_time=view.findViewById(R.id.dialog_edit_future_time);
        final TextView dialog_edit_future_time_text=view.findViewById(R.id.dialog_edit_future_time_text);
        TextView dialog_edit_future_time_select=view.findViewById(R.id.dialog_edit_future_time_select);
        futureCalendar=Calendar.getInstance();
        pastCalendar=Calendar.getInstance();

        //初始化对话框中各项文本
        final Calendar calendar=Calendar.getInstance();
        dialog_edit_time_text.setText(String.format(getString(R.string.dialog_set_edit_time), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE)));
        dialog_edit_future_time_text.setText(String.format(getString(R.string.dialog_set_edit_future_time), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE)));
        dialog_edit_past_time_text.setText(String.format(getString(R.string.dialog_set_edit_past_time), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE)));
        //点击时间图标仍调用日历式设置时间的方法
        dialog_edit_time_text.setOnClickListener(new SetTimeByCalendar());
        //监听输入框内容改变，实时更新对应文本
        dialog_edit_future_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null&&s.length()>0){
                    int dateDistance=Integer.valueOf(s.toString());
                    if(TimeDistanceValid(dateDistance)){
                        Calendar tmpCalendar=Calendar.getInstance();
                        tmpCalendar.add(Calendar.DAY_OF_YEAR, dateDistance);
                        futureCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), tmpCalendar.get(Calendar.DATE));
                        dialog_edit_future_time_text.setText(String.format(getString(R.string.dialog_set_edit_future_time), tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH)+1, tmpCalendar.get(Calendar.DATE)));
                    }
                }

            }
        });
        dialog_edit_past_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null&&s.length()>0){
                    int dateDistance=Integer.valueOf(s.toString());
                    if(TimeDistanceValid(dateDistance)){
                        Calendar tmpCalendar=Calendar.getInstance();
                        tmpCalendar.add(Calendar.DAY_OF_YEAR,-dateDistance);
                        pastCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), tmpCalendar.get(Calendar.DATE));
                        dialog_edit_past_time_text.setText(String.format(getString(R.string.dialog_set_edit_past_time), tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH)+1, tmpCalendar.get(Calendar.DATE)));
                    }
                }
            }
        });
        //添加选取监听，还需设置详细时间，调用timePicker
        dialog_edit_future_time_select.setOnClickListener(new SetTimeByTimePicker(CAL_FUTURE,dialog));
        dialog_edit_past_time_select.setOnClickListener(new SetTimeByTimePicker(CAL_PAST,dialog));
        dialog.show();

    }
    class SetTimeByTimePicker implements View.OnClickListener{
        AlertDialog dialog;
        int calendarCode;
        SetTimeByTimePicker(int calendarCode,AlertDialog dialog){
            this.calendarCode=calendarCode;
            this.dialog=dialog;
        }
        @Override
        public void onClick(View v){
            Calendar calendar=Calendar.getInstance();
            TimePickerDialog timePickerDialog=new TimePickerDialog(NewPageActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int year,month,date;
                            if(calendarCode==CAL_FUTURE){
                                year = futureCalendar.get(Calendar.YEAR);
                                month = futureCalendar.get(Calendar.MONTH);
                                date = futureCalendar.get(Calendar.DATE);
                            } else if (calendarCode == CAL_PAST) {
                                year = pastCalendar.get(Calendar.YEAR);
                                month = pastCalendar.get(Calendar.MONTH);
                                date = pastCalendar.get(Calendar.DATE);
                            }else {
                                return;
                            }
                            editTimeText.setText(String.format(getString(R.string.set_edit_time), year, month + 1, date, hourOfDay, minute));
                            targetCalendar.set(year,month,date,hourOfDay,minute);
                            dialog.dismiss();
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        }
    }

    //检测时间间隔合法性
    private boolean TimeDistanceValid(int timeDistance){
        return timeDistance>=0&&timeDistance<Integer.MAX_VALUE;
    }

    //在成功获取后设置背景
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == QUEST_IMAGE && resultCode == RESULT_OK){
            if (data != null) {
                Uri imageURI=data.getData();
                View view = findViewById(R.id.edit_above_background);
                imagePath = MainActivity.getPathFromUri(getApplicationContext(), imageURI);
                File f = new File(imagePath);
                Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());
                view.setBackground(drawable);
            }
        }
    }


}
