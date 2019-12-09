package com.almosti.mytime;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class TimePage implements Serializable {

    //用于防止页面在写数据过程中出错导致的变量未正确初始化
    TimePage() {
        title="";
        remark="";
        year=-1;
        month=-1;
        day=-1;
        hour=-1;
        minute=-1;
        cycle=-1;
        second=0;//由于timepicker无法设定秒，故直接默认为整分钟
    }

    //用于获取剩余时间，本质是计算时间差，这里先返回差额秒数，由调用者自行转化单位
    long getTimeDistance(){
        Calendar calendar=Calendar.getInstance();
        return Math.abs(calendar.getTimeInMillis()-getTimeDate().getTime());
    }

    //检查倒计时是否已过
    boolean getTimeDistanceSign(){
        Calendar calendar=Calendar.getInstance();
        return calendar.getTime().getTime()<=getTimeDate().getTime();
    }

    Calendar getTimeCalendar(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar;
    }
    Date getTimeDate(){
        Calendar calendar=getTimeCalendar();
        return calendar.getTime();
    }
    //检查倒计时页合法性
    boolean isValid(){
        return (!title.isEmpty()&&
                year!=-1&&
                month!=-1&&
                day!=-1&&
                hour!=-1&&
                minute!=-1);
    }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getSecond() { return second; }

    public void setSecond(int second) { this.second = second; }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath;}

    private String title;
    private String remark;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int cycle;
    private String imagePath;
}
