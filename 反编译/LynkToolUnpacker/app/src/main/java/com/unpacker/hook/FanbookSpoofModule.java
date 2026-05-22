package com.unpacker.hook;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * NowPlayingProvider 伪装模块
 *
 * 功能：拦截 NowPlayingProvider 的查询结果，将 package name 伪装成帆书(fanbook)
 *
 * 目标ContentProvider: cn.navitool.bridge.NowPlayingProvider
 * 目标应用: cn.navitool
 *
 * 需要在LSPosed中激活并勾选目标应用
 */
public class FanbookSpoofModule implements IXposedHookLoadPackage {

    private static final String TAG = "FanbookSpoof";
    private static final String TARGET_PACKAGE = "cn.navitool";
    private static final String TARGET_AUTHORITY = "cn.navitool.media";

    // 帆书的包名
    private static final String FANSBOOK_PACKAGE = "cn.fanbook.android";
    // 帆书的应用名
    private static final String FANSBOOK_APP_NAME = "帆书";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        Log.d(TAG, "开始Hook目标应用: " + TARGET_PACKAGE);

        // Hook ContentProvider.query()
        hookContentProviderQuery(lpparam);

        // Hook ContentProvider.call()
        hookContentProviderCall(lpparam);

        Log.d(TAG, "Hook设置完成");
    }

    /**
     * Hook ContentProvider.query()
     * 拦截查询并修改返回的package name
     */
    private void hookContentProviderQuery(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.content.ContentProvider",
            lpparam.classLoader,
            "query",
            Uri.class,                      // uri
            String[].class,                // projection
            String.class,                  // selection
            String[].class,                // selectionArgs
            String.class,                  // sortOrder
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Uri uri = (Uri) param.args[0];
                    if (uri != null && uri.toString().contains(TARGET_AUTHORITY)) {
                        Log.d(TAG, "拦截到NowPlayingProvider查询: " + uri);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Uri uri = (Uri) param.args[0];
                    if (uri != null && uri.toString().contains(TARGET_AUTHORITY)) {
                        Cursor cursor = (Cursor) param.getResult();
                        if (cursor != null && cursor.getCount() > 0) {
                            Log.d(TAG, "获取到Cursor, 数量: " + cursor.getCount());
                            // 尝试修改Cursor中的数据
                            modifyCursorPackageName(cursor);
                        }
                    }
                }
            }
        );

        // 重载版本 (Android 13+)
        XposedHelpers.findAndHookMethod(
            "android.content.ContentProvider",
            lpparam.classLoader,
            "query",
            Uri.class,
            Bundle.class,
            XC_MethodReplacement.DO_NOTHING
        );
    }

    /**
     * Hook ContentProvider.call()
     * 有些Provider使用call方法返回数据
     */
    private void hookContentProviderCall(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.content.ContentProvider",
            lpparam.classLoader,
            "call",
            String.class,                  // method
            String.class,                  // arg
            Bundle.class,                  // extras
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String method = (String) param.args[0];
                    if (method != null && method.contains("NowPlaying")) {
                        Log.d(TAG, "拦截到call方法: " + method);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Bundle result = (Bundle) param.getResult();
                    if (result != null) {
                        // 检查并修改Bundle中的package信息
                        modifyBundlePackageName(result);
                    }
                }
            }
        );
    }

    /**
     * 修改Cursor中的package name
     *
     * NowPlayingProvider通常返回包含以下列的Cursor:
     * - package_name: 应用包名
     * - app_name: 应用名称
     * - title: 歌曲标题
     * - artist: 艺术家
     * - album: 专辑
     * - duration: 时长
     * - position: 播放位置
     * - state: 播放状态
     */
    private void modifyCursorPackageName(Cursor cursor) {
        try {
            // 获取列索引
            int packageCol = cursor.getColumnIndex("package_name");
            int appNameCol = cursor.getColumnIndex("app_name");

            if (packageCol == -1) {
                // 尝试其他可能的列名
                packageCol = cursor.getColumnIndex("packageName");
            }
            if (appNameCol == -1) {
                appNameCol = cursor.getColumnIndex("appName");
            }

            if (packageCol == -1 && appNameCol == -1) {
                Log.d(TAG, "未找到package相关列，尝试打印所有列名");
                printColumnNames(cursor);
                return;
            }

            Log.d(TAG, "找到package列: " + packageCol + ", appName列: " + appNameCol);

            // 注意: Cursor是只读的，无法直接修改
            // 需要使用其他方法

        } catch (Throwable t) {
            Log.e(TAG, "修改Cursor失败: " + t.getMessage());
        }
    }

    /**
     * 打印Cursor的所有列名
     */
    private void printColumnNames(Cursor cursor) {
        try {
            String[] columns = cursor.getColumnNames();
            StringBuilder sb = new StringBuilder("Cursor列名: [");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(columns[i]);
            }
            sb.append("]");
            Log.d(TAG, sb.toString());
        } catch (Throwable t) {
            Log.e(TAG, "打印列名失败: " + t.getMessage());
        }
    }

    /**
     * 修改Bundle中的package name
     */
    private void modifyBundlePackageName(Bundle bundle) {
        try {
            // 检查常见的key
            String[] packageKeys = {"package_name", "packageName", "package", "app_package"};
            String[] appNameKeys = {"app_name", "appName", "app_name", "application_name"};

            for (String key : packageKeys) {
                if (bundle.containsKey(key)) {
                    String original = bundle.getString(key);
                    Log.d(TAG, "修改Bundle中 " + key + ": " + original + " -> " + FANSBOOK_PACKAGE);
                    bundle.putString(key, FANSBOOK_PACKAGE);
                }
            }

            for (String key : appNameKeys) {
                if (bundle.containsKey(key)) {
                    String original = bundle.getString(key);
                    Log.d(TAG, "修改Bundle中 " + key + ": " + original + " -> " + FANSBOOK_APP_NAME);
                    bundle.putString(key, FANSBOOK_APP_NAME);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "修改Bundle失败: " + t.getMessage());
        }
    }
}
