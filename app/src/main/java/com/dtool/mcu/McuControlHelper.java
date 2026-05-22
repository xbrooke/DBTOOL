package com.dtool.mcu;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * MCU 控制助手
 *
 * 功能：
 * 1. 获取 MCU 控制接口
 * 2. 发送媒体控制命令
 * 3. 控制音量
 * 4. 错误处理和日志记录
 */
public class McuControlHelper {
    private static final String TAG = "McuControlHelper";

    // MCU 操作 ID
    private static final int OPER_ID_VOLUME = 26;
    private static final int OPER_ID_PLAY = 27;
    private static final int OPER_ID_PAUSE = 28;
    private static final int OPER_ID_NEXT = 29;
    private static final int OPER_ID_PREV = 30;

    // 操作类型
    private static final int OPER_TYPE_SET = 1;
    private static final int OPER_TYPE_GET = 2;

    // 服务 ID
    private static final int SERVICE_ID_AUDIO = 3;

    // 最大音量步数
    private static final int MAX_VOLUME_STEP = 31;

    private Object mcuControl;
    private boolean mcuAvailable = false;

    public McuControlHelper(Context context) {
        try {
            // 方法 1: 通过系统服务获取
            this.mcuControl = context.getSystemService("mcu_control");
            if (mcuControl != null) {
                mcuAvailable = true;
                Log.d(TAG, "MCU control service obtained via getSystemService");
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get MCU control via getSystemService", e);
        }

        try {
            // 方法 2: 通过反射获取
            Class<?> mcuControlClass = Class.forName("com.xxx.mcu.McuControl");
            Method getInstance = mcuControlClass.getMethod("getInstance");
            this.mcuControl = getInstance.invoke(null);
            if (mcuControl != null) {
                mcuAvailable = true;
                Log.d(TAG, "MCU control service obtained via reflection");
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get MCU control via reflection", e);
        }

        Log.w(TAG, "MCU control service not available");
    }

    /**
     * 检查 MCU 控制是否可用
     */
    public boolean isMcuAvailable() {
        return mcuAvailable && mcuControl != null;
    }

    /**
     * 发送 MCU 命令
     */
    public boolean sendMcuCommand(int operId, int operType, int value1, int value2) {
        if (!isMcuAvailable()) {
            Log.w(TAG, "MCU control not available");
            return false;
        }

        try {
            // 通过反射调用 sendMsg 方法
            Method sendMsg = mcuControl.getClass().getMethod(
                "sendMsg",
                int.class,  // oper_id
                int.class,  // oper_type
                int.class,  // value1
                int.class   // value2
            );

            Object result = sendMsg.invoke(mcuControl, operId, operType, value1, value2);
            Log.d(TAG, String.format(
                "MCU command sent: oper_id=%d, oper_type=%d, value1=0x%02x, value2=0x%02x, result=%s",
                operId, operType, value1, value2, result
            ));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send MCU command", e);
            return false;
        }
    }

    /**
     * 调节音量
     */
    public boolean setVolume(int step) {
        if (step < 0 || step > MAX_VOLUME_STEP) {
            Log.w(TAG, "Invalid volume step: " + step);
            return false;
        }

        Log.d(TAG, "Setting volume to step: " + step);
        return sendMcuCommand(OPER_ID_VOLUME, OPER_TYPE_SET, step, 0);
    }

    /**
     * 设置音量百分比 (0-100%)
     */
    public boolean setVolumePercent(int percent) {
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        // 转换为 MCU 步数 (0-31)
        int step = (percent * MAX_VOLUME_STEP) / 100;
        return setVolume(step);
    }

    /**
     * 播放
     */
    public boolean play() {
        Log.d(TAG, "Sending play command");
        return sendMcuCommand(OPER_ID_PLAY, OPER_TYPE_SET, 1, 0);
    }

    /**
     * 暂停
     */
    public boolean pause() {
        Log.d(TAG, "Sending pause command");
        return sendMcuCommand(OPER_ID_PAUSE, OPER_TYPE_SET, 1, 0);
    }

    /**
     * 下一曲
     */
    public boolean next() {
        Log.d(TAG, "Sending next command");
        return sendMcuCommand(OPER_ID_NEXT, OPER_TYPE_SET, 1, 0);
    }

    /**
     * 上一曲
     */
    public boolean previous() {
        Log.d(TAG, "Sending previous command");
        return sendMcuCommand(OPER_ID_PREV, OPER_TYPE_SET, 1, 0);
    }

    /**
     * 播放/暂停 (切换)
     */
    public boolean togglePlayPause() {
        Log.d(TAG, "Toggling play/pause");
        // 先尝试播放，如果失败则暂停
        if (!play()) {
            return pause();
        }
        return true;
    }

    /**
     * 增加音量
     */
    public boolean increaseVolume() {
        Log.d(TAG, "Increasing volume");
        // 这需要先获取当前音量，暂时增加 3 步
        return sendMcuCommand(OPER_ID_VOLUME, OPER_TYPE_SET, 3, 0);
    }

    /**
     * 减少音量
     */
    public boolean decreaseVolume() {
        Log.d(TAG, "Decreasing volume");
        // 这需要先获取当前音量，暂时减少 3 步
        return sendMcuCommand(OPER_ID_VOLUME, OPER_TYPE_SET, -3, 0);
    }

    /**
     * 获取 MCU 操作 ID
     */
    public static class OperationId {
        public static final int VOLUME = 26;
        public static final int PLAY = 27;
        public static final int PAUSE = 28;
        public static final int NEXT = 29;
        public static final int PREV = 30;
    }

    /**
     * 获取 MCU 操作类型
     */
    public static class OperationType {
        public static final int SET = 1;
        public static final int GET = 2;
    }

    /**
     * 获取 MCU 服务 ID
     */
    public static class ServiceId {
        public static final int AUDIO = 3;
    }
}
