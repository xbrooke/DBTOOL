package com.dtool.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dtool.DBToolApplication;
import com.dtool.R;
import com.dtool.activity.MainActivity;

/**
 * 车机核心服务
 *
 * 功能：
 * 1. 保持应用在后台运行
 * 2. 管理MediaNotificationListener
 * 3. 与车机系统保持通信
 */
public class VehicleCoreService extends Service {

    private static final String TAG = "VehicleCoreService";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VehicleCoreService已创建");

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "VehicleCoreService onStartCommand");

        // 确保NotificationListenerService已启用
        checkNotificationListener();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "VehicleCoreService已销毁");
    }

    /**
     * 创建前台通知
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, DBToolApplication.CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle("DBTOOL 运行中")
            .setContentText("正在监听音乐播放状态")
            .setSmallIcon(R.mipmap.ic_launcher)  // ✅ 使用项目图标
            .setContentIntent(pendingIntent)
            .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        return builder.build();
    }

    /**
     * 检查NotificationListenerService是否已启用
     */
    private void checkNotificationListener() {
        // 检查 NotificationListenerService 是否已授权
        // 如果未授权，可以跳转到设置页面
        try {
            String flat = android.provider.Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
            );

            if (flat != null && flat.contains(getPackageName())) {
                Log.d(TAG, "NotificationListenerService已授权");
            } else {
                Log.w(TAG, "NotificationListenerService未授权");
                // 可以发送广播通知UI提示用户授权
            }
        } catch (Exception e) {
            Log.e(TAG, "检查NotificationListenerService失败", e);
        }
    }
}
