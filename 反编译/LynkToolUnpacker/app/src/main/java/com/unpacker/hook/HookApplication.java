package com.unpacker.hook;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed模块Application
 * 实现IXposedHookLoadPackage接口来加载Hook模块
 *
 * 支持的模块:
 * 1. JiaguUnpacker - 360加固DEX脱壳
 * 2. FanbookSpoofModuleV2 - NowPlayingProvider伪装
 */
public class HookApplication extends Application {

    private static final String TAG = "JiaguUnpacker";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "===========================================");
        Log.d(TAG, "Xposed模块加载中...");
        Log.d(TAG, "模块列表:");
        Log.d(TAG, "  1. JiaguUnpacker - DEX脱壳");
        Log.d(TAG, "  2. FanbookSpoofModuleV2 - 帆书伪装");
        Log.d(TAG, "===========================================");

        // 设置全局log输出到Xposed日志
        XposedBridge.setStrictMode(false);
    }

    /**
     * 入口点1: DEX脱壳模块
     */
    public static void loadPackage_JiaguUnpacker(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        JiaguUnpacker unpacker = new JiaguUnpacker();
        unpacker.handleLoadPackage(lpparam);
    }

    /**
     * 入口点2: 帆书伪装模块
     */
    public static void loadPackage_FanbookSpoof(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        FanbookSpoofModuleV2 spoof = new FanbookSpoofModuleV2();
        spoof.handleLoadPackage(lpparam);
    }
}
