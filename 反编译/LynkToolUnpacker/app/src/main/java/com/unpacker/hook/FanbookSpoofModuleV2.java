package com.unpacker.hook;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * NowPlayingProvider 伪装模块 V2
 *
 * 功能：拦截 NowPlayingProvider 的查询结果，将 package name 伪装成帆书(fanbook)
 *
 * 策略：由于Cursor是只读的，我们使用动态代理来包装Cursor
 *
 * 目标ContentProvider: cn.navitool.bridge.NowPlayingProvider
 * 目标应用: cn.navitool
 */
public class FanbookSpoofModuleV2 implements IXposedHookLoadPackage {

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

        Log.d(TAG, "===========================================");
        Log.d(TAG, "开始Hook目标应用: " + TARGET_PACKAGE);
        Log.d(TAG, "===========================================");

        // Hook ContentProvider.query()
        hookContentProviderQuery(lpparam);

        // Hook ContentProvider.call()
        hookContentProviderCall(lpparam);

        // Hook ActivityThread中加载ContentProvider的地方
        hookActivityThread(lpparam);

        Log.d(TAG, "Hook设置完成");
    }

    /**
     * Hook ContentProvider.query()
     */
    private void hookContentProviderQuery(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.content.ContentProvider",
                lpparam.classLoader,
                "query",
                Uri.class,
                String[].class,
                String.class,
                String[].class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Uri uri = (Uri) param.args[0];
                        if (uri != null && isTargetProvider(uri)) {
                            Log.d(TAG, "拦截到目标Provider查询: " + uri);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Uri uri = (Uri) param.args[0];
                        if (uri != null && isTargetProvider(uri)) {
                            Cursor originalCursor = (Cursor) param.getResult();
                            if (originalCursor != null) {
                                Log.d(TAG, "获取到原始Cursor, 数量: " + originalCursor.getCount());
                                Cursor wrappedCursor = wrapCursorWithSpoof(originalCursor);
                                if (wrappedCursor != null) {
                                    param.setResult(wrappedCursor);
                                    Log.d(TAG, "已返回伪装后的Cursor");
                                }
                            }
                        }
                    }
                }
            );
            Log.d(TAG, "成功Hook ContentProvider.query()");
        } catch (Throwable t) {
            Log.e(TAG, "Hook ContentProvider.query() 失败: " + t.getMessage());
        }
    }

    /**
     * Hook ContentProvider.call()
     */
    private void hookContentProviderCall(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.content.ContentProvider",
                lpparam.classLoader,
                "call",
                String.class,
                String.class,
                String.class,
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String method = (String) param.args[0];
                        if (method != null && isTargetMethod(method)) {
                            Log.d(TAG, "拦截到call方法: " + method);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bundle result = (Bundle) param.getResult();
                        if (result != null) {
                            modifyBundleData(result);
                            param.setResult(result);
                        }
                    }
                }
            );
            Log.d(TAG, "成功Hook ContentProvider.call()");
        } catch (Throwable t) {
            Log.e(TAG, "Hook ContentProvider.call() 失败: " + t.getMessage());
        }
    }

    /**
     * Hook ActivityThread - 拦截ContentProvider的创建和调用
     */
    private void hookActivityThread(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.ActivityThread",
                lpparam.classLoader,
                "acquireProvider",
                android.content.Context.class,
                android.content.pm.ProviderInfo.class,
                boolean.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object clientProvider = param.getResult();
                        if (clientProvider != null) {
                            // 尝试获取ContentProvider的引用
                            try {
                                Field providerField = clientProvider.getClass().getDeclaredField("provider");
                                providerField.setAccessible(true);
                                Object provider = providerField.get(clientProvider);
                                if (provider != null) {
                                    String name = provider.getClass().getName();
                                    if (name.contains("NowPlaying") || name.contains("navitool")) {
                                        Log.d(TAG, "获取到目标Provider: " + name);
                                    }
                                }
                            } catch (Throwable t) {
                                // ignore
                            }
                        }
                    }
                }
            );
            Log.d(TAG, "成功Hook ActivityThread.acquireProvider()");
        } catch (Throwable t) {
            Log.e(TAG, "Hook ActivityThread 失败: " + t.getMessage());
        }
    }

    /**
     * 检查uri是否指向目标Provider
     */
    private boolean isTargetProvider(Uri uri) {
        if (uri == null) return false;
        String uriStr = uri.toString();
        return uriStr.contains(TARGET_AUTHORITY) ||
               uriStr.contains("navitool") ||
               uriStr.contains("nowplaying") ||
               uriStr.contains("media");
    }

    /**
     * 检查是否是目标方法
     */
    private boolean isTargetMethod(String method) {
        if (method == null) return false;
        String lower = method.toLowerCase();
        return lower.contains("nowplaying") ||
               lower.contains("media") ||
               lower.contains("package") ||
               lower.contains("current");
    }

    /**
     * 伪装Cursor - 创建新的Cursor替换原始数据
     */
    private Cursor wrapCursorWithSpoof(Cursor originalCursor) {
        try {
            // 读取原始Cursor的所有数据
            String[] columnNames = originalCursor.getColumnNames();
            int columnCount = columnNames.length;

            // 创建新的MatrixCursor
            MatrixCursor newCursor = new MatrixCursor(columnNames);

            // 复制所有行并修改package相关列
            int packageNameCol = findColumnIndex(columnNames, "package_name", "packageName", "package");
            int appNameCol = findColumnIndex(columnNames, "app_name", "appName", "application_name");
            int titleCol = findColumnIndex(columnNames, "title", "song_name", "track");
            int artistCol = findColumnIndex(columnNames, "artist", "singer", "performer");
            int albumCol = findColumnIndex(columnNames, "album", "album_name");

            Log.d(TAG, "列索引 - package: " + packageNameCol + ", appName: " + appNameCol);

            originalCursor.moveToPosition(-1);
            while (originalCursor.moveToNext()) {
                Object[] newRow = new Object[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    Object originalValue = originalCursor.getObject(i);

                    // 修改package name列
                    if (i == packageNameCol && originalValue != null) {
                        String original = originalValue.toString();
                        if (!original.equals(FANSBOOK_PACKAGE)) {
                            Log.d(TAG, "伪装package: " + original + " -> " + FANSBOOK_PACKAGE);
                            newRow[i] = FANSBOOK_PACKAGE;
                        } else {
                            newRow[i] = originalValue;
                        }
                    }
                    // 修改app name列
                    else if (i == appNameCol && originalValue != null) {
                        String original = originalValue.toString();
                        if (!original.equals(FANSBOOK_APP_NAME)) {
                            Log.d(TAG, "伪装app name: " + original + " -> " + FANSBOOK_APP_NAME);
                            newRow[i] = FANSBOOK_APP_NAME;
                        } else {
                            newRow[i] = originalValue;
                        }
                    }
                    // 其他列保持原样
                    else {
                        newRow[i] = originalValue;
                    }
                }

                newCursor.addRow(newRow);
            }

            originalCursor.close();
            return newCursor;

        } catch (Throwable t) {
            Log.e(TAG, "伪装Cursor失败: " + t.getMessage());
            t.printStackTrace();
            return originalCursor;
        }
    }

    /**
     * 查找列索引
     */
    private int findColumnIndex(String[] columns, String... names) {
        for (String name : names) {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 修改Bundle中的数据
     */
    private void modifyBundleData(Bundle bundle) {
        try {
            // 检查并修改所有可能包含package信息的key
            String[] keysToCheck = {
                "package_name", "packageName", "package",
                "app_name", "appName", "application_name",
                "source_package", "sourcePackage"
            };

            for (String key : keysToCheck) {
                if (bundle.containsKey(key)) {
                    Object value = bundle.get(key);
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (key.toLowerCase().contains("package") && !strValue.equals(FANSBOOK_PACKAGE)) {
                            Log.d(TAG, "伪装Bundle[" + key + "]: " + strValue + " -> " + FANSBOOK_PACKAGE);
                            bundle.putString(key, FANSBOOK_PACKAGE);
                        } else if (key.toLowerCase().contains("app") && !strValue.equals(FANSBOOK_APP_NAME)) {
                            Log.d(TAG, "伪装Bundle[" + key + "]: " + strValue + " -> " + FANSBOOK_APP_NAME);
                            bundle.putString(key, FANSBOOK_APP_NAME);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "修改Bundle失败: " + t.getMessage());
        }
    }
}
