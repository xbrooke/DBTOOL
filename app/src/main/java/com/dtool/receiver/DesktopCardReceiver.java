package com.dtool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import com.dtool.service.MediaNotificationListener;

/**
 * 桌面卡片广播接收器
 *
 * 功能：
 * 1. 接收来自车机的媒体控制广播
 * 2. 将控制命令转发给音乐App
 * 3. 支持亿连(ecarx)和极氪/几何(geely)协议
 */
public class DesktopCardReceiver extends BroadcastReceiver {

    private static final String TAG = "DesktopCardReceiver";

    // 亿连协议
    private static final String ECARX_ACTION = "ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER";
    private static final String ECARX_EXTRA_PACKAGE = "package_name";
    private static final String ECARX_EXTRA_ACTION = "media_action";

    // 极氪/几何协议
    private static final String GEELY_PLAY = "com.geely.mediawidget.ACTION_WIDGET_TOGGLE_PLAY";
    private static final String GEELY_NEXT = "com.geely.mediawidget.ACTION_WIDGET_NEXT";
    private static final String GEELY_PREV = "com.geely.mediawidget.ACTION_WIDGET_PREV";

    // 内部广播
    private static final String INTERNAL_PLAY_PAUSE = "com.dtool.action.PLAY_PAUSE";
    private static final String INTERNAL_NEXT = "com.dtool.action.NEXT";
    private static final String INTERNAL_PREV = "com.dtool.action.PREV";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        try {
            switch (action) {
                case ECARX_ACTION:
                    handleEcarxAction(context, intent);
                    break;

                case GEELY_PLAY:
                    handlePlayPause(context);
                    break;

                case GEELY_NEXT:
                    handleNext(context);
                    break;

                case "com.geely.mediawidget.ACTION_WIDGET_PREV":
                    handlePrev(context);
                    break;

                case INTERNAL_PLAY_PAUSE:
                    sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                    break;

                case INTERNAL_NEXT:
                    sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_NEXT);
                    break;

                case INTERNAL_PREV:
                    sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    break;

                default:
                    Log.d(TAG, "未知广播: " + action);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "处理广播异常", e);
        }
    }

    /**
     * 处理亿连协议
     */
    private void handleEcarxAction(Context context, Intent intent) {
        String packageName = intent.getStringExtra(ECARX_EXTRA_PACKAGE);
        int mediaAction = intent.getIntExtra(ECARX_EXTRA_ACTION, -1);

        Log.d(TAG, "亿连协议: package=" + packageName + ", action=" + mediaAction);

        // 根据action执行相应操作
        switch (mediaAction) {
            case 0: // 播放
                sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PLAY);
                break;
            case 1: // 暂停
                sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PAUSE);
                break;
            case 2: // 播放/暂停
                sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
            case 3: // 下一曲
                sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_NEXT);
                break;
            case 4: // 上一曲
                sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                break;
            default:
                Log.w(TAG, "未知亿连action: " + mediaAction);
                break;
        }
    }

    /**
     * 处理播放/暂停
     */
    private void handlePlayPause(Context context) {
        Log.d(TAG, "处理播放/暂停");
        sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    /**
     * 处理下一曲
     */
    private void handleNext(Context context) {
        Log.d(TAG, "处理下一曲");
        sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    /**
     * 处理上一曲
     */
    private void handlePrev(Context context) {
        Log.d(TAG, "处理上一曲");
        sendMediaKey(context, AudioManager.STREAM_MUSIC, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    /**
     * 发送媒体按键事件
     */
    private void sendMediaKey(Context context, int streamType, int keyCode) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // 发送按键事件
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                audioManager.dispatchMediaKeyEvent(event);

                event = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
                audioManager.dispatchMediaKeyEvent(event);

                Log.d(TAG, "发送媒体按键: " + keyCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "发送媒体按键失败", e);
        }
    }
}
