package com.dtool.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

/**
 * DBTool无障碍服务
 *
 * 功能：
 * 1. 模拟媒体按键点击
 * 2. 监听前台应用变化
 * 3. 控制音乐App的播放/暂停
 */
public class DBToolAccessibilityService extends AccessibilityService {

    private static final String TAG = "DBToolAccessibilityService";
    // 使用WeakReference避免内存泄漏
    private static WeakReference<DBToolAccessibilityService> instanceRef = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instanceRef = new WeakReference<>(this);
        Log.d(TAG, "DBToolAccessibilityService已创建");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instanceRef = null;
        Log.d(TAG, "DBToolAccessibilityService已销毁");
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "无障碍服务已连接");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT
            | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;

        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        String packageName = event.getPackageName().toString();
        int eventType = event.getEventType();

        Log.d(TAG, "无障碍事件: " + packageName + ", type=" + eventType);

        // 可以在这里监听特定App的窗口变化
        // 但对于媒体控制，我们主要使用AudioManager发送按键
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "无障碍服务被中断");
    }

    /**
     * 发送媒体按键
     */
    public static void sendMediaKey(int keyCode) {
        DBToolAccessibilityService instance = instanceRef != null ? instanceRef.get() : null;
        if (instance == null) {
            Log.w(TAG, "无障碍服务未连接");
            return;
        }

        try {
            AudioManager audioManager = (AudioManager) instance.getSystemService(AudioManager.class);
            if (audioManager != null) {
                // 发送按键事件
                android.view.KeyEvent downEvent = new android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN, keyCode);
                audioManager.dispatchMediaKeyEvent(downEvent);
                
                android.view.KeyEvent upEvent = new android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP, keyCode);
                audioManager.dispatchMediaKeyEvent(upEvent);

                Log.d(TAG, "发送媒体按键: " + keyCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "发送媒体按键失败", e);
        }
    }

    /**
     * 执行播放/暂停
     */
    public static void playPause() {
        sendMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    /**
     * 执行下一曲
     */
    public static void next() {
        sendMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    /**
     * 执行上一曲
     */
    public static void previous() {
        sendMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }
}
