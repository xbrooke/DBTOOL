package com.unpacker.hook;

import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.app.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import dalvik.system.DexFile;
import dalvik.system.InMemoryDexClassLoader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 360加固保 DEX解密Hook模块
 *
 * Hook关键点:
 * 1. dalvik.system.DexFile - loadDex等方法
 * 2. dalvik.system.InMemoryDexClassLoader - 内存中加载的DEX
 * 3. android.app.Application - attach()方法
 * 4. libjiagu native方法 - 解密后的DEX回调
 *
 * 使用方法:
 * 1. 安装到支持Xposed的ROM (需要Root)
 * 2. 在LSPosed/Lsposed中激活本模块
 * 3. 选择目标应用 (cn.navitool)
 * 4. 勾选"作用于所有版本"
 */
public class JiaguUnpacker implements IXposedHookLoadPackage {

    private static final String TAG = "JiaguUnpacker";
    private static final String TARGET_PACKAGE = "cn.navitool";

    // DEX输出目录
    private static final String OUTPUT_DIR = "/sdcard/Download/dex_unpacked/";
    private static int dexCount = 0;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        Log.d(TAG, "开始Hook目标应用: " + TARGET_PACKAGE);

        // Hook Application.attach() - 360加固会在此处初始化
        hookApplicationAttach(lpparam);

        // Hook DexFile.loadDex() - 拦截DEX加载
        hookDexFileLoad(lpparam);

        // Hook ClassLoader.loadClass() - 监控类加载
        hookClassLoader(lpparam);

        // Hook native方法 - 360加固的核心解密回调
        hookNativeMethods(lpparam);

        // Hook MediaPlayer相关方法
        hookMediaPlayer(lpparam);
    }

    /**
     * Hook Application.attach() 方法
     * 360加固在Application.attach()时会进行初始化
     */
    private void hookApplicationAttach(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.app.Application",
            lpparam.classLoader,
            "attach",
            Context.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "Application.attach() 被调用");

                    // 尝试获取Context
                    Context context = (Context) param.args[0];
                    if (context != null) {
                        Log.d(TAG, "Application context获取成功");
                        dumpAppInfo(context);
                    }
                }
            }
        );
    }

    /**
     * Hook DexFile.loadDex() 方法
     * 这是360加固加载解密后DEX的关键入口
     */
    private void hookDexFileLoad(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "dalvik.system.DexFile",
                "loadDex",
                String.class, String.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String dexPath = (String) param.args[0];
                        Log.d(TAG, "loadDex called: " + dexPath);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        DexFile dexFile = (DexFile) param.getResult();
                        if (dexFile != null) {
                            Log.d(TAG, "DexFile loaded successfully");
                            // 保存加载的DEX
                            saveDexFile(dexFile, "loadDex");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            Log.e(TAG, "hookDexFileLoad failed: " + t.getMessage());
        }

        // Hook DexFile构造函数
        try {
            XposedHelpers.findAndHookConstructor(
                "dalvik.system.DexFile",
                lpparam.classLoader,
                File.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "DexFile constructor called");
                        DexFile dexFile = (DexFile) param.getResult();
                        if (dexFile != null) {
                            // 保存DEX
                            saveDexFile(dexFile, "constructor");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            Log.e(TAG, "hookDexFile constructor failed: " + t.getMessage());
        }
    }

    /**
     * Hook ClassLoader.loadClass()
     * 监控类的加载，可以获取到解密后的类信息
     */
    private void hookClassLoader(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            ClassLoader.class,
            "loadClass",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String className = (String) param.args[0];
                    // 只关注与我们目标相关的类
                    if (className != null && (
                        className.contains("MediaNotification") ||
                        className.contains("DesktopCard") ||
                        className.contains("NowPlaying") ||
                        className.contains("StubApp") ||
                        className.contains("cn.navitool") ||
                        className.contains("cn.fanbook") ||
                        className.contains("fanbook")
                    )) {
                        Log.d(TAG, "loadClass: " + className);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class<?> clazz = (Class<?>) param.getResult();
                    if (clazz != null) {
                        String className = clazz.getName();
                        // 重点关注的类
                        if (className.contains("MediaNotification") ||
                            className.contains("DesktopCard") ||
                            className.contains("NowPlaying") ||
                            className.contains("StubApp")) {
                            Log.d(TAG, "Class loaded: " + className);
                            // dump类的方法
                            dumpClassMethods(clazz);
                        }
                    }
                }
            }
        );
    }

    /**
     * Hook 360加固的Native方法
     * interface12是360加固中用于获取解密DEX的关键native方法
     */
    private void hookNativeMethods(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hook StubApp中的native方法
        try {
            Class<?> stubAppClass = XposedHelpers.findClassIfExists("com.stub.StubApp", lpparam.classLoader);
            if (stubAppClass != null) {
                Log.d(TAG, "找到StubApp类");

                // 尝试hook所有native方法
                for (Method method : stubAppClass.getDeclaredMethods()) {
                    if (Modifier.isNative(method.getModifiers())) {
                        Log.d(TAG, "Native method: " + method.getName());

                        // Hook interface12 - 通常是获取解密DEX的关键
                        if (method.getName().equals("interface12")) {
                            XposedBridge.hookMethod(method, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    Log.d(TAG, "interface12 called, result: " + param.getResult());
                                    if (param.getResult() instanceof Enumeration) {
                                        // 这可能是解密后的DEX文件列表
                                        dumpEnumeration((Enumeration<?>) param.getResult());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "hookNativeMethods failed: " + t.getMessage());
        }

        // Hook System.loadLibrary - 可能加载libjiagu
        try {
            XposedHelpers.findAndHookMethod(
                System.class,
                "loadLibrary",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String libName = (String) param.args[0];
                        Log.d(TAG, "loadLibrary: " + libName);
                        if (libName != null && libName.contains("jiagu")) {
                            Log.d(TAG, "检测到360加固库加载!");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            Log.e(TAG, "hook loadLibrary failed: " + t.getMessage());
        }
    }

    /**
     * Hook MediaPlayer相关方法
     * 可能与音乐控制相关
     */
    private void hookMediaPlayer(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> mediaPlayerClass = XposedHelpers.findClassIfExists("android.media.MediaPlayer", lpparam.classLoader);
            if (mediaPlayerClass != null) {
                Log.d(TAG, "找到MediaPlayer类");

                // Hook create方法
                XposedHelpers.findAndHookMethod(
                    mediaPlayerClass,
                    "create",
                    Context.class,
                    android.net.Uri.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.d(TAG, "MediaPlayer.create() called");
                        }
                    }
                );
            }
        } catch (Throwable t) {
            Log.e(TAG, "hookMediaPlayer failed: " + t.getMessage());
        }
    }

    /**
     * 保存DEX文件到SD卡
     */
    private void saveDexFile(Object dexFileObj, String source) {
        try {
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 尝试获取DEX文件的字节数据
            if (dexFileObj instanceof DexFile) {
                DexFile dexFile = (DexFile) dexFileObj;

                // DexFile内部有mCookie字段，包含DEX数据
                Field cookieField = XposedHelpers.findFieldIfExists(dexFile.getClass(), "mCookie");
                if (cookieField != null) {
                    Object cookie = cookieField.get(dexFile);
                    Log.d(TAG, "DexFile mCookie: " + (cookie != null ? cookie.getClass().toString() : "null"));
                }

                // 获取加载的DEX路径
                String dexPath = dexFile.getName();
                Log.d(TAG, "DexFile name: " + dexPath);

                // 尝试直接获取byte[]
                Field odexField = XposedHelpers.findFieldIfExists(dexFile.getClass(), "mOdex");
                if (odexField != null) {
                    byte[] odex = (byte[]) odexField.get(dexFile);
                    if (odex != null) {
                        String fileName = String.format("dex_%s_%d.odex", source, dexCount++);
                        File outFile = new File(outputDir, fileName);
                        writeToFile(outFile, odex);
                        Log.d(TAG, "DEX saved to: " + outFile.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "saveDexFile failed: " + t.getMessage());
        }
    }

    /**
     * 导出Enumeration内容
     */
    private void dumpEnumeration(Enumeration<?> enumeration) {
        if (enumeration == null) return;

        int i = 0;
        while (enumeration.hasMoreElements()) {
            Object item = enumeration.nextElement();
            Log.d(TAG, "  Enum[" + i++ + "]: " + item);
            if (item instanceof String && ((String) item).endsWith(".dex")) {
                // 可能需要加载这个DEX
                Log.d(TAG, "    ^ 发现DEX文件: " + item);
            }
        }
    }

    /**
     * dump类的所有方法
     */
    private void dumpClassMethods(Class<?> clazz) {
        if (clazz == null) return;

        String className = clazz.getName();
        Log.d(TAG, "=== Methods of " + className + " ===");

        for (Method method : clazz.getDeclaredMethods()) {
            String modifiers = java.lang.reflect.Modifier.toString(method.getModifiers());
            String returnType = method.getReturnType().getSimpleName();
            String methodName = method.getName();

            Log.d(TAG, "  " + modifiers + " " + returnType + " " + methodName);

            // 如果是native方法，标记
            if (java.lang.reflect.Modifier.isNative(method.getModifiers())) {
                Log.d(TAG, "    ^ NATIVE METHOD");
            }
        }
    }

    /**
     * dump应用信息
     */
    private void dumpAppInfo(Context context) {
        try {
            Log.d(TAG, "=== App Info ===");
            Log.d(TAG, "Package: " + context.getPackageName());

            // 获取应用信息
            android.content.pm.ApplicationInfo appInfo = context.getApplicationInfo();
            Log.d(TAG, "SourceDir: " + appInfo.sourceDir);
            Log.d(TAG, "NativeLibraryDir: " + appInfo.nativeLibraryDir);
            Log.d(TAG, "DataDir: " + appInfo.dataDir);

            // 列出所有已加载的库
            File libDir = new File(appInfo.nativeLibraryDir);
            if (libDir.exists()) {
                File[] libs = libDir.listFiles();
                if (libs != null) {
                    for (File lib : libs) {
                        if (lib.getName().contains("jiagu")) {
                            Log.d(TAG, "  ^ 360加固库: " + lib.getName());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "dumpAppInfo failed: " + t.getMessage());
        }
    }

    /**
     * 写入文件
     */
    private void writeToFile(File file, byte[] data) {
        if (data == null || file == null) return;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
        } catch (Throwable t) {
            Log.e(TAG, "writeToFile failed: " + t.getMessage());
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (Throwable t) {}
            }
        }
    }
}
