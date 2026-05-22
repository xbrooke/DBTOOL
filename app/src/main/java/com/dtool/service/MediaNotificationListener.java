package com.dtool.service;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 媒体通知监听服务
 *
 * 功能：
 * 1. 监听系统媒体通知 (网易云音乐、QQ音乐、酷狗等)
 * 2. 解析音乐信息 (标题、艺术家、专辑、封面)
 * 3. 推送音乐信息到NowPlayingProvider
 * 4. 伪装成帆书(fanbook)推送给车机
 */
public class MediaNotificationListener extends NotificationListenerService {

    private static final String TAG = "MediaNotificationListener";

    // 媒体包名列表
    private static final String[] MEDIA_PACKAGES = {
        "com.netease.cloudmusic",     // 网易云音乐
        "com.tencent.qqmusic",        // QQ音乐
        "com.kugou.player",           // 酷狗音乐
        "com.baidu.music",            // 千千音乐
        "com.ximalaya.ting",          // 喜马拉雅
        "com.qianqian.audio",         // 懒人听书
        "com.evernote",               // 印象笔记
        "com.xm.sparta",              // 虾米音乐
        "org.cocos11.wechat",         // 微信音乐
        "com.ss.android.ugc.aweme",   // 抖音
        "com.ss.android.ugc.aweme.lite", // 抖音Lite
    };

    // 当前播放信息（使用同步锁保证线程安全）
    private static final Object lock = new Object();
    private static MediaInfo currentMedia = null;

    // 广播Action
    public static final String ACTION_MEDIA_UPDATED = "com.dtool.action.MEDIA_UPDATED";
    public static final String ACTION_MEDIA_CONTROL = "com.dtool.action.MEDIA_CONTROL";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_ALBUM = "album";
    public static final String EXTRA_PACKAGE = "package";
    public static final String EXTRA_IS_PLAYING = "is_playing";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) {
            Log.w(TAG, "收到空的通知");
            return;
        }

        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();

        Log.d(TAG, "收到通知: " + packageName);

        // 检查是否是媒体通知
        if (!isMediaNotification(packageName, notification)) {
            Log.d(TAG, "不是媒体通知: " + packageName);
            return;
        }

        Log.d(TAG, "检测到媒体通知: " + packageName);

        // 解析媒体信息
        MediaInfo mediaInfo = parseMediaNotification(packageName, notification);

        if (mediaInfo != null) {
            synchronized(lock) {
                currentMedia = mediaInfo;
            }
            Log.d(TAG, "媒体通知已更新: " + mediaInfo.title + " - " + mediaInfo.artist);
            Log.d(TAG, "完整信息: " + mediaInfo.toString());

            // 广播更新
            broadcastMediaUpdate(mediaInfo);
        } else {
            Log.w(TAG, "无法解析媒体信息: " + packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (isMediaPackage(packageName)) {
            Log.d(TAG, "媒体通知已移除: " + packageName);
            // 清空当前播放信息
            synchronized(lock) {
                currentMedia = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
        synchronized(lock) {
            currentMedia = null;
        }
        Log.d(TAG, "MediaNotificationListener已销毁");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "=== MediaNotificationListener 已连接 ===");
        Log.d(TAG, "通知监听服务已启用，开始监听媒体通知");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.w(TAG, "=== MediaNotificationListener 已断开连接 ===");
        Log.w(TAG, "通知监听服务已禁用，停止监听媒体通知");
    }

    /**
     * 检查是否是媒体通知
     */
    private boolean isMediaNotification(String packageName, Notification notification) {
        if (notification == null) {
            Log.d(TAG, "通知为空: " + packageName);
            return false;
        }

        // 检查是否是媒体应用
        if (!isMediaPackage(packageName)) {
            Log.d(TAG, "不是媒体应用: " + packageName);
            return false;
        }

        // 检查通知内容
        if (notification.extras == null) {
            Log.w(TAG, "通知 extras 为空: " + packageName);
            return false;
        }

        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        Log.d(TAG, "通知内容 - 标题: " + title + ", 文本: " + text);

        // 需要有标题才认为是媒体通知
        boolean isMedia = title != null && title.length() > 0;
        if (!isMedia) {
            Log.w(TAG, "通知没有标题: " + packageName);
        }
        return isMedia;
    }

    /**
     * 检查是否是媒体包
     */
    private boolean isMediaPackage(String packageName) {
        for (String pkg : MEDIA_PACKAGES) {
            if (pkg.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析媒体通知
     */
    private MediaInfo parseMediaNotification(String packageName, Notification notification) {
        if (notification == null || notification.extras == null) {
            Log.w(TAG, "无法解析: 通知或 extras 为空");
            return null;
        }

        MediaInfo info = new MediaInfo();
        info.packageName = packageName;
        info.appName = getAppName(packageName);

        // 获取通知内容
        CharSequence titleSeq = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "");
        info.title = titleSeq != null ? titleSeq.toString() : "";
        
        CharSequence artistSeq = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT, "");
        info.artist = artistSeq != null ? artistSeq.toString() : "";
        
        CharSequence albumSeq = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "");
        info.album = albumSeq != null ? albumSeq.toString() : "";

        Log.d(TAG, "解析通知 - 标题: " + info.title + ", 艺术家: " + info.artist + ", 专辑: " + info.album);

        // 获取播放状态
        info.isPlaying = isPlaying(notification);

        // 获取大文本(有时包含完整信息)
        CharSequence bigText = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (bigText != null && info.artist.length() == 0) {
            Log.d(TAG, "尝试从大文本解析: " + bigText);
            // 如果艺术家为空，尝试从大文本中解析
            String[] parts = bigText.toString().split(" - ");
            if (parts.length >= 2) {
                info.artist = parts[0];
                info.title = parts[1];
                Log.d(TAG, "从大文本解析成功 - 标题: " + info.title + ", 艺术家: " + info.artist);
            }
        }

        // 获取媒体图片
        if (notification.largeIcon != null) {
            info.hasAlbumArt = true;
            Log.d(TAG, "检测到专辑封面");
        }

        return info;
    }

    /**
     * 判断是否正在播放
     */
    private boolean isPlaying(Notification notification) {
        // 检查MediaStyle的播放状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            Notification.Action[] actions = notification.actions;
            if (actions != null) {
                for (Notification.Action action : actions) {
                    if (action != null && action.title != null) {
                        String title = action.title.toString().toLowerCase();
                        if (title.contains("play") || title.contains("pause")) {
                            // 可以通过action判断播放状态，但MediaStyle通常已经标记
                        }
                    }
                }
            }
        }

        // 默认认为有通知就是在播放
        return true;
    }

    /**
     * 获取应用名称
     */
    private String getAppName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    /**
     * 广播媒体更新
     */
    private void broadcastMediaUpdate(MediaInfo mediaInfo) {
        Intent intent = new Intent(ACTION_MEDIA_UPDATED);
        intent.putExtra(EXTRA_TITLE, mediaInfo.title);
        intent.putExtra(EXTRA_ARTIST, mediaInfo.artist);
        intent.putExtra(EXTRA_ALBUM, mediaInfo.album);
        intent.putExtra(EXTRA_PACKAGE, mediaInfo.packageName);
        intent.putExtra(EXTRA_IS_PLAYING, mediaInfo.isPlaying);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // 同时通知NowPlayingProvider更新
        notifyNowPlayingProvider(mediaInfo);
    }

    /**
     * 通知NowPlayingProvider更新
     */
    private void notifyNowPlayingProvider(MediaInfo mediaInfo) {
        // 通过本地ContentProvider更新数据
        getContentResolver().notifyChange(
            android.net.Uri.parse("content://com.dtool.media/nowplaying"),
            null
        );
    }

    /**
     * 获取当前媒体信息
     */
    public static MediaInfo getCurrentMedia() {
        synchronized(lock) {
            return currentMedia != null ? new MediaInfo(currentMedia) : null;
        }
    }

    /**
     * 媒体信息类
     */
    public static class MediaInfo {
        public String packageName;
        public String appName;
        public String title;
        public String artist;
        public String album;
        public boolean isPlaying;
        public boolean hasAlbumArt;

        // 复制构造函数
        public MediaInfo(MediaInfo other) {
            if (other != null) {
                this.packageName = other.packageName;
                this.appName = other.appName;
                this.title = other.title;
                this.artist = other.artist;
                this.album = other.album;
                this.isPlaying = other.isPlaying;
                this.hasAlbumArt = other.hasAlbumArt;
            }
        }

        public MediaInfo() {
        }

        @Override
        public String toString() {
            return "MediaInfo{" +
                    "package='" + packageName + '\'' +
                    ", app='" + appName + '\'' +
                    ", title='" + title + '\'' +
                    ", artist='" + artist + '\'' +
                    ", album='" + album + '\'' +
                    ", isPlaying=" + isPlaying +
                    '}';
        }
    }
}
