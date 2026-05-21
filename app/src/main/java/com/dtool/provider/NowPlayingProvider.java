package com.dtool.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dtool.service.MediaNotificationListener;
import com.dtool.service.MediaNotificationListener.MediaInfo;

/**
 * NowPlayingProvider - 媒体播放信息提供者
 *
 * 核心功能：
 * 1. 向车机系统提供当前播放信息
 * 2. 伪装成帆书(fanbook)的package name
 *
 * 车机通过ContentResolver查询 content://com.dtool.media/nowplaying 获取当前播放信息
 */
public class NowPlayingProvider extends ContentProvider {

    private static final String TAG = "NowPlayingProvider";

    // Authority
    public static final String AUTHORITY = "com.dtool.media";

    // URI Matcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CODE_NOWPLAYING = 1;
    private static final int CODE_NOWPLAYING_DETAIL = 2;

    static {
        uriMatcher.addURI(AUTHORITY, "nowplaying", CODE_NOWPLAYING);
        uriMatcher.addURI(AUTHORITY, "nowplaying/detail", CODE_NOWPLAYING_DETAIL);
    }

    // 帆书的package name - 关键伪装！
    private static final String FAKE_PACKAGE_NAME = "cn.fanbook.android";
    private static final String FAKE_APP_NAME = "帆书";

    // 列名
    private static final String COLUMN_PACKAGE_NAME = "package_name";
    private static final String COLUMN_APP_NAME = "app_name";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_ARTIST = "artist";
    private static final String COLUMN_ALBUM = "album";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_ALBUM_ART = "album_art";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "NowPlayingProvider created");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Log.d(TAG, "query: " + uri);

        switch (uriMatcher.match(uri)) {
            case CODE_NOWPLAYING:
            case CODE_NOWPLAYING_DETAIL:
                return queryNowPlaying(projection);

            default:
                Log.w(TAG, "Unknown uri: " + uri);
                return null;
        }
    }

    /**
     * 查询当前播放信息
     *
     * 关键：这里会将真实的package name伪装成帆书
     */
    private Cursor queryNowPlaying(String[] projection) {
        MediaInfo media = MediaNotificationListener.getCurrentMedia();

        // 构建列名数组
        String[] columns;
        if (projection != null && projection.length > 0) {
            columns = projection;
        } else {
            columns = new String[]{
                COLUMN_PACKAGE_NAME,
                COLUMN_APP_NAME,
                COLUMN_TITLE,
                COLUMN_ARTIST,
                COLUMN_ALBUM,
                COLUMN_DURATION,
                COLUMN_POSITION,
                COLUMN_STATE,
                COLUMN_ALBUM_ART
            };
        }

        MatrixCursor cursor = new MatrixCursor(columns);

        if (media != null) {
            // 构建行数据
            Object[] row = new Object[columns.length];

            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                switch (col) {
                    case COLUMN_PACKAGE_NAME:
                        // 关键：伪装成帆书！
                        row[i] = FAKE_PACKAGE_NAME;
                        Log.d(TAG, "伪装package: " + media.packageName + " -> " + FAKE_PACKAGE_NAME);
                        break;

                    case COLUMN_APP_NAME:
                        // 伪装成帆书
                        row[i] = FAKE_APP_NAME;
                        Log.d(TAG, "伪装app_name: " + media.appName + " -> " + FAKE_APP_NAME);
                        break;

                    case COLUMN_TITLE:
                        row[i] = media.title != null ? media.title : "";
                        break;

                    case COLUMN_ARTIST:
                        row[i] = media.artist != null ? media.artist : "";
                        break;

                    case COLUMN_ALBUM:
                        row[i] = media.album != null ? media.album : "";
                        break;

                    case COLUMN_DURATION:
                        // 默认时长 (实际应该从MediaSession获取)
                        row[i] = 0;
                        break;

                    case COLUMN_POSITION:
                        // 默认位置
                        row[i] = 0;
                        break;

                    case COLUMN_STATE:
                        // 播放状态: 0=停止, 1=播放, 2=暂停
                        row[i] = media.isPlaying ? 1 : 2;
                        break;

                    case COLUMN_ALBUM_ART:
                        // 专辑封面URI
                        row[i] = "";
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }

            cursor.addRow(row);
        }

        Log.d(TAG, "返回Cursor, 数量: " + cursor.getCount());
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_NOWPLAYING:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".nowplaying";

            case CODE_NOWPLAYING_DETAIL:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".nowplaying";

            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.w(TAG, "insert not supported");
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.w(TAG, "delete not supported");
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.w(TAG, "update not supported");
        return 0;
    }

    /**
     * 处理call方法
     * 有些车机系统使用call方法来获取播放信息
     */
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Log.d(TAG, "call: " + method);

        if ("getNowPlaying".equals(method)) {
            return getNowPlayingBundle();
        }

        return super.call(method, arg, extras);
    }

    /**
     * 返回当前播放信息的Bundle
     * 关键：这里也会伪装成帆书
     */
    private Bundle getNowPlayingBundle() {
        Bundle bundle = new Bundle();
        MediaInfo media = MediaNotificationListener.getCurrentMedia();

        if (media != null) {
            // 伪装成帆书
            bundle.putString(COLUMN_PACKAGE_NAME, FAKE_PACKAGE_NAME);
            bundle.putString(COLUMN_APP_NAME, FAKE_APP_NAME);
            bundle.putString(COLUMN_TITLE, media.title);
            bundle.putString(COLUMN_ARTIST, media.artist);
            bundle.putString(COLUMN_ALBUM, media.album);
            bundle.putInt(COLUMN_STATE, media.isPlaying ? 1 : 2);

            Log.d(TAG, "call返回伪装数据: " + FAKE_PACKAGE_NAME);
        } else {
            // 无播放时返回空
            bundle.putString(COLUMN_PACKAGE_NAME, "");
            bundle.putString(COLUMN_APP_NAME, "");
        }

        return bundle;
    }
}
