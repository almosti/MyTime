package com.almosti.mytime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class NewPageActivity extends AppCompatActivity {

    TimePage page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_page);
        //去除标题栏
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        page=new TimePage();
        FloatingActionButton fabCancel=findViewById(R.id.edit_cancel);
        FloatingActionButton fabSave = findViewById(R.id.edit_save);
        final EditText editTitle = findViewById(R.id.edit_title);
        final EditText editRemark = findViewById(R.id.edit_remark);
        View editTime=findViewById(R.id.edit_time_section);
        View editRepeat = findViewById(R.id.edit_repeat_section);
        View editImage=findViewById(R.id.edit_image_section);
        final TextView editTimeText=findViewById(R.id.edit_time);

        //取消按钮
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        //保存按钮
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page.setTitle(editTitle.getText().toString());
                page.setRemark(editRemark.getText().toString());

                if(!page.isValid()){
                    Toast.makeText(getBaseContext(), "输入的倒计时无效,请检查标题与所选日期", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent=getIntent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("TimePage", page);
                intent.putExtras(bundle);
                setResult(RESULT_OK);
                finish();
            }
        });
        //点击编辑时间部分
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar=Calendar.getInstance();
                final Calendar targetTime=Calendar.getInstance();
                DatePickerDialog datePickerDialog=new DatePickerDialog(NewPageActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                                TimePickerDialog timePickerDialog=new TimePickerDialog(NewPageActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                targetTime.set(year,month,dayOfMonth,hourOfDay,minute);
                                                editTimeText.setText(String.format(getString(R.string.set_edit_time),year,month,dayOfMonth,hourOfDay,minute));
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
        });
        editTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO:长按使用日期计算器
                return false;
            }
        });
        editRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:设置重复
            }
        });
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:设置图片
            }
        });
    }
}
