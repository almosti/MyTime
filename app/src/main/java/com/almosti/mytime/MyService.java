package com.almosti.mytime;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    private Runnable runnable;
    private Handler handler;
    private int Time = 1000*5;//周期时间
    private Timer timer = new Timer();
    private ArrayList<TimePage> TimeList;
    private static final long Epsilon=1000*60*2;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 方式一：采用Handler的postDelayed(Runnable, long)方法
         */
        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                // handler自带方法实现定时器
                Log.d("1", "定时1");
                //CheckTime();
                handler.postDelayed(this, Time);

            }
        };
        handler.postDelayed(runnable, 1000);//延时多长时间启动定时器

        /**
         * 方式二：采用timer及TimerTask结合的方法
         */
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("1", "定时2");
                //CheckTime();
            }
        };
        timer.schedule(timerTask,
                1000,//延迟1秒执行
                Time);//周期时间


    }
    /**
     * 方式三：采用AlarmManager机制
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d("1", "定时3");
                CheckTime();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + Time;
        Intent intent2 = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    //核心的检测时间代码
    private void CheckTime() {
        TimeListOperator operator=new TimeListOperator();
        TimeList=operator.load(getBaseContext());
        if (TimeList != null) {
            for (int i = 0; i < TimeList.size(); i++) {
                if (TimeList.get(i).getTimeDistance() < Epsilon && TimeList.get(i).canNotification()) {
                    String title = TimeList.get(i).getTitle();
                    String content = "即将到期";

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
                    PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,0,intent,0);
                    NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    if(Build.VERSION.SDK_INT >= 26)
                    {
                        //当sdk版本大于26
                        String id = "channel_1";
                        String description = "143";
                        int importance = NotificationManager.IMPORTANCE_LOW;
                        NotificationChannel channel = new NotificationChannel(id, description, importance);
                        manager.createNotificationChannel(channel);
                        Notification notification = new Notification.Builder(MyService.this, id)
                                .setCategory(Notification.CATEGORY_MESSAGE)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(title)
                                .setContentText(content)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .build();
                        manager.notify(1, notification);
                    }
                    else
                    {
                        //当sdk版本小于26
                        Notification notification = new NotificationCompat.Builder(MyService.this)
                                .setContentTitle(title)
                                .setContentText(content)
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .build();
                        manager.notify(1,notification);
                    }
                }
            }
        }
    }
}
