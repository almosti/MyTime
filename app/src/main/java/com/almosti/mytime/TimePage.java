package com.almosti.mytime;

import java.io.Serializable;
import java.util.Calendar;

public class TimePage implements Serializable {

    //用于防止页面在写数据过程中出错导致的变量未正确初始化
    public TimePage() {
        title="";
        remark="";
        pictureID=-1;
        year=-1;
        month=-1;
        day=-1;
        hour=-1;
        minute=-1;
        cycle=-1;
        second=0;//由于timepicker无法设定秒，故直接默认为整分钟
    }

    //用于获取剩余时间
    Calendar getTimeDistance(){
        Calendar calendar=Calendar.getInstance();
        calendar.set(year-calendar.get(Calendar.YEAR),
                month-calendar.get(Calendar.MONTH),
                day-calendar.get(Calendar.DATE),
                hour-calendar.get(Calendar.HOUR_OF_DAY),
                minute-calendar.get(Calendar.MINUTE),
                second-calendar.get(Calendar.SECOND));
        return calendar;
    }

    //检查倒计时是否已过
    boolean getTimeDistanceSign(){
        Calendar calendar=Calendar.getInstance();
        calendar=getTimeDistance();
        if(calendar.get(Calendar.YEAR)<0)
            return false;
        else if(calendar.get(Calendar.MONTH)<0)
            return false;
        else if(calendar.get(Calendar.DATE)<0)
            return false;
        else if(calendar.get(Calendar.HOUR_OF_DAY)<0)
            return false;
        else if(calendar.get(Calendar.MINUTE)<0)
            return false;
        else if(calendar.get(Calendar.SECOND)<0)
            return false;
        return true;
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

    public int getPictureID() {
        return pictureID;
    }

    public void setPictureID(int pictureID) {
        this.pictureID = pictureID;
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

    private String title;
    private String remark;
    private int pictureID;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int cycle;

}
