package com.dtool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.dtool.service.VehicleCoreService;

/**
 * 启动接收器
 *
 * 功能：
 * 1. 监听设备启动、锁屏、解锁等事件
 * 2. 启动VehicleCoreService核心服务
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        // 启动核心服务
        if (isRelevantAction(action)) {
            try {
                Intent serviceIntent = new Intent(context, VehicleCoreService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+使用startForegroundService
                    context.startForegroundService(serviceIntent);
                } else {
                    // 旧版本使用startService
                    context.startService(serviceIntent);
                }
                Log.d(TAG, "已启动VehicleCoreService");
            } catch (Exception e) {
                Log.e(TAG, "启动服务失败", e);
            }
        }
    }

    /**
     * 检查是否是相关动作
     */
    private boolean isRelevantAction(String action) {
        return action.equals(Intent.ACTION_BOOT_COMPLETED)
            || action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)
            || action.equals("android.intent.action.QUICKBOOT_POWERON")
            || action.equals(Intent.ACTION_REBOOT)
            || action.equals(Intent.ACTION_POWER_CONNECTED)
            || action.equals(Intent.ACTION_POWER_DISCONNECTED)
            || action.equals(Intent.ACTION_SCREEN_ON)
            || action.equals(Intent.ACTION_USER_PRESENT);
    }
}
