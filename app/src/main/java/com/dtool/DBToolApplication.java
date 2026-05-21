package com.dtool;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

/**
 * DBTOOL Application
 */
public class DBToolApplication extends Application {

    private static final String TAG = "DBTOOL";
    public static final String CHANNEL_ID = "dtool_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DBTOOL Application started");

        // 设置全局异常处理
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "未捕获的异常", throwable);
            // 可以在这里上报异常或重启应用
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "DBTOOL Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("DBTOOL核心服务通知");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
