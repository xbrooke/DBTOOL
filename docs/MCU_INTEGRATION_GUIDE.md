# MCU 接口集成指南

基于车机系统日志分析，本指南提供了 DBTOOL 与 MCU 接口集成的详细方案。

---

## 1. MCU 控制接口概述

### 1.1 MCU 是什么？

MCU (Microcontroller Unit) 是车机系统中的微控制器，负责:
- 音量控制
- 媒体控制 (播放、暂停、下一曲等)
- 方向盘按键处理
- 其他硬件控制

### 1.2 MCU 通信流程

```
DBTOOL 应用
    ↓
McuControl 接口
    ↓
McuControlWrapper
    ↓
MCU 硬件
    ↓
车机系统硬件 (音量、媒体控制等)
```

### 1.3 日志中的 MCU 操作

```
05-22 13:11:33.176 McuControl: Enter sendMsg oper_id:26, oper_type:1, value1:0x19, value2:0x 0
05-22 13:11:33.176 McuControl: volumeAdjustFG2OpID data.step:19
05-22 13:11:33.176 McuControl: sendMsg +++++ sServiceId:3
```

**参数说明**:
- `oper_id`: 操作 ID (26 = 音量调节)
- `oper_type`: 操作类型 (1 = 调节)
- `value1`: 值 (0x19 = 25, 音量步数)
- `value2`: 额外参数
- `sServiceId`: 服务 ID (3 = 音频服务)

---

## 2. MCU 操作 ID 列表

基于日志分析，推断的 MCU 操作 ID:

| 操作 ID | 操作名称 | 操作类型 | 参数 | 说明 |
|--------|--------|--------|------|------|
| 26 | 音量调节 | 1 | step (0-31) | 调节音量 |
| ? | 播放 | ? | ? | 需要确认 |
| ? | 暂停 | ? | ? | 需要确认 |
| ? | 下一曲 | ? | ? | 需要确认 |
| ? | 上一曲 | ? | ? | 需要确认 |
| ? | 播放/暂停 | ? | ? | 需要确认 |

---

## 3. MCU 接口实现

### 3.1 获取 MCU 控制接口

```java
// 方法 1: 通过系统服务获取
Context context = getApplicationContext();
Object mcuControl = context.getSystemService("mcu_control");

// 方法 2: 通过反射获取
try {
    Class<?> mcuControlClass = Class.forName("com.xxx.mcu.McuControl");
    Method getInstance = mcuControlClass.getMethod("getInstance");
    Object mcuControl = getInstance.invoke(null);
} catch (Exception e) {
    Log.e(TAG, "Failed to get MCU control", e);
}
```

### 3.2 发送 MCU 命令

```java
public class McuControlHelper {
    private static final String TAG = "McuControlHelper";
    
    // MCU 操作 ID
    private static final int OPER_ID_VOLUME = 26;
    private static final int OPER_ID_PLAY = 27;      // 推测
    private static final int OPER_ID_PAUSE = 28;     // 推测
    private static final int OPER_ID_NEXT = 29;      // 推测
    private static final int OPER_ID_PREV = 30;      // 推测
    
    // 操作类型
    private static final int OPER_TYPE_SET = 1;
    private static final int OPER_TYPE_GET = 2;
    
    // 服务 ID
    private static final int SERVICE_ID_AUDIO = 3;
    
    private Object mcuControl;
    
    public McuControlHelper(Context context) {
        try {
            // 获取 MCU 控制接口
            this.mcuControl = context.getSystemService("mcu_control");
            if (mcuControl == null) {
                Log.w(TAG, "MCU control service not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize MCU control", e);
        }
    }
    
    /**
     * 发送 MCU 命令
     */
    public boolean sendMcuCommand(int operId, int operType, int value1, int value2) {
        if (mcuControl == null) {
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
            Log.d(TAG, "MCU command sent: oper_id=" + operId + ", result=" + result);
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
        // step: 0-31 (0 = 静音, 31 = 最大)
        if (step < 0 || step > 31) {
            Log.w(TAG, "Invalid volume step: " + step);
            return false;
        }
        
        Log.d(TAG, "Setting volume to step: " + step);
        return sendMcuCommand(OPER_ID_VOLUME, OPER_TYPE_SET, step, 0);
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
}
```

### 3.3 在 DBTOOL 中使用 MCU 接口

```java
// 在 DesktopCardReceiver 中
public class DesktopCardReceiver extends BroadcastReceiver {
    private static final String TAG = "DesktopCardReceiver";
    private McuControlHelper mcuControl;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mcuControl == null) {
            mcuControl = new McuControlHelper(context);
        }
        
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        
        try {
            switch (action) {
                case "ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER":
                    handleEcarxAction(context, intent);
                    break;
                    
                case "com.geely.mediawidget.ACTION_WIDGET_TOGGLE_PLAY":
                    mcuControl.play();  // 或 pause()，取决于当前状态
                    break;
                    
                case "com.geely.mediawidget.ACTION_WIDGET_NEXT":
                    mcuControl.next();
                    break;
                    
                case "com.geely.mediawidget.ACTION_WIDGET_PREV":
                    mcuControl.previous();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling broadcast", e);
        }
    }
    
    private void handleEcarxAction(Context context, Intent intent) {
        int mediaAction = intent.getIntExtra("media_action", -1);
        
        switch (mediaAction) {
            case 0: // 播放
                mcuControl.play();
                break;
            case 1: // 暂停
                mcuControl.pause();
                break;
            case 2: // 播放/暂停
                // 需要获取当前状态
                mcuControl.play();  // 或 pause()
                break;
            case 3: // 下一曲
                mcuControl.next();
                break;
            case 4: // 上一曲
                mcuControl.previous();
                break;
        }
    }
}
```

---

## 4. 音量控制集成

### 4.1 通过 MCU 接口控制音量

```java
public class VolumeControlHelper {
    private McuControlHelper mcuControl;
    private static final int MAX_VOLUME_STEP = 31;
    
    public VolumeControlHelper(Context context) {
        this.mcuControl = new McuControlHelper(context);
    }
    
    /**
     * 设置音量 (0-100%)
     */
    public void setVolume(int percent) {
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        
        // 转换为 MCU 步数 (0-31)
        int step = (percent * MAX_VOLUME_STEP) / 100;
        mcuControl.setVolume(step);
    }
    
    /**
     * 增加音量
     */
    public void increaseVolume() {
        // 获取当前音量
        int currentStep = getCurrentVolume();
        if (currentStep < MAX_VOLUME_STEP) {
            mcuControl.setVolume(currentStep + 1);
        }
    }
    
    /**
     * 减少音量
     */
    public void decreaseVolume() {
        // 获取当前音量
        int currentStep = getCurrentVolume();
        if (currentStep > 0) {
            mcuControl.setVolume(currentStep - 1);
        }
    }
    
    /**
     * 获取当前音量
     */
    private int getCurrentVolume() {
        // 需要从 MCU 查询当前音量
        // 这需要实现 OPER_TYPE_GET 操作
        return 15;  // 默认中等音量
    }
}
```

---

## 5. 权限和安全性

### 5.1 MCU 接口权限

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="com.xxx.permission.MCU_CONTROL" />
<uses-permission android:name="com.xxx.permission.MEDIA_CONTROL" />
```

### 5.2 权限检查

```java
public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    
    /**
     * 检查 MCU 控制权限
     */
    public static boolean hasMcuControlPermission(Context context) {
        try {
            int result = context.checkPermission(
                "com.xxx.permission.MCU_CONTROL",
                android.os.Process.myPid(),
                android.os.Process.myUid()
            );
            return result == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check MCU control permission", e);
            return false;
        }
    }
    
    /**
     * 请求 MCU 控制权限
     */
    public static void requestMcuControlPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                new String[]{"com.xxx.permission.MCU_CONTROL"},
                REQUEST_CODE_MCU_CONTROL
            );
        }
    }
}
```

---

## 6. 错误处理和日志

### 6.1 MCU 命令错误处理

```java
public class McuCommandExecutor {
    private static final String TAG = "McuCommandExecutor";
    
    /**
     * 执行 MCU 命令并处理错误
     */
    public static boolean executeMcuCommand(
        McuControlHelper mcuControl,
        int operId,
        int operType,
        int value1,
        int value2
    ) {
        try {
            boolean result = mcuControl.sendMcuCommand(operId, operType, value1, value2);
            
            if (result) {
                Log.d(TAG, "MCU command executed successfully");
                return true;
            } else {
                Log.w(TAG, "MCU command execution failed");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "MCU command execution error", e);
            return false;
        }
    }
    
    /**
     * 重试 MCU 命令
     */
    public static boolean executeMcuCommandWithRetry(
        McuControlHelper mcuControl,
        int operId,
        int operType,
        int value1,
        int value2,
        int maxRetries
    ) {
        for (int i = 0; i < maxRetries; i++) {
            if (executeMcuCommand(mcuControl, operId, operType, value1, value2)) {
                return true;
            }
            
            // 等待后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Log.e(TAG, "MCU command failed after " + maxRetries + " retries");
        return false;
    }
}
```

### 6.2 日志记录

```java
public class McuLogger {
    private static final String TAG = "McuLogger";
    
    /**
     * 记录 MCU 命令
     */
    public static void logMcuCommand(
        int operId,
        int operType,
        int value1,
        int value2
    ) {
        Log.d(TAG, String.format(
            "MCU Command: oper_id=%d, oper_type=%d, value1=0x%02x, value2=0x%02x",
            operId, operType, value1, value2
        ));
    }
    
    /**
     * 记录 MCU 响应
     */
    public static void logMcuResponse(Object response) {
        Log.d(TAG, "MCU Response: " + response);
    }
    
    /**
     * 记录 MCU 错误
     */
    public static void logMcuError(String message, Exception e) {
        Log.e(TAG, "MCU Error: " + message, e);
    }
}
```

---

## 7. 测试和验证

### 7.1 MCU 接口测试

```java
public class McuControlTest {
    private static final String TAG = "McuControlTest";
    private McuControlHelper mcuControl;
    
    /**
     * 测试 MCU 接口可用性
     */
    public void testMcuAvailability(Context context) {
        mcuControl = new McuControlHelper(context);
        
        // 尝试发送一个简单的命令
        boolean result = mcuControl.setVolume(15);
        
        if (result) {
            Log.d(TAG, "MCU interface is available");
        } else {
            Log.w(TAG, "MCU interface is not available");
        }
    }
    
    /**
     * 测试所有媒体控制命令
     */
    public void testAllMediaCommands() {
        Log.d(TAG, "Testing all media commands...");
        
        // 测试播放
        mcuControl.play();
        sleep(500);
        
        // 测试暂停
        mcuControl.pause();
        sleep(500);
        
        // 测试下一曲
        mcuControl.next();
        sleep(500);
        
        // 测试上一曲
        mcuControl.previous();
        sleep(500);
        
        Log.d(TAG, "All media commands tested");
    }
    
    /**
     * 测试音量控制
     */
    public void testVolumeControl() {
        Log.d(TAG, "Testing volume control...");
        
        for (int step = 0; step <= 31; step++) {
            mcuControl.setVolume(step);
            sleep(100);
        }
        
        Log.d(TAG, "Volume control tested");
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 7.2 验证日志

```bash
# 启动 DBTOOL 并运行测试
adb logcat | grep "McuControl"

# 预期日志
D/McuControlHelper: Setting volume to step: 15
D/McuControlHelper: MCU command sent: oper_id=26, result=true
D/McuControlHelper: Sending play command
D/McuControlHelper: MCU command sent: oper_id=27, result=true
```

---

## 8. 集成步骤

### 步骤 1: 获取 MCU 接口文档

- [ ] 联系车机系统开发团队
- [ ] 获取 MCU 接口 API 文档
- [ ] 获取所有支持的操作 ID
- [ ] 获取权限要求

### 步骤 2: 实现 MCU 控制类

- [ ] 创建 `McuControlHelper` 类
- [ ] 实现所有媒体控制命令
- [ ] 添加错误处理
- [ ] 添加日志记录

### 步骤 3: 集成到 DBTOOL

- [ ] 修改 `DesktopCardReceiver`
- [ ] 修改 `VehicleCoreService`
- [ ] 添加权限声明
- [ ] 测试所有功能

### 步骤 4: 测试和验证

- [ ] 单元测试
- [ ] 集成测试
- [ ] 手动测试
- [ ] 性能测试

### 步骤 5: 部署和发布

- [ ] 代码审查
- [ ] 安全审查
- [ ] 版本更新
- [ ] 发布到 GitHub

---

## 9. 常见问题

### Q1: 如何获取 MCU 接口？

**A**: 通过系统服务或反射获取。具体方法取决于车机系统的实现。

### Q2: MCU 操作 ID 是什么？

**A**: 基于日志分析，26 是音量调节。其他操作 ID 需要从车机系统开发团队获取。

### Q3: 如何处理 MCU 命令失败？

**A**: 实现重试机制，记录详细日志，提供用户反馈。

### Q4: MCU 接口是否线程安全？

**A**: 需要确认。建议使用同步锁保护 MCU 命令。

### Q5: 如何测试 MCU 接口？

**A**: 在车机系统上运行测试代码，检查日志输出。

---

## 10. 相关文档

- `CAR_SYSTEM_ANALYSIS.md` - 车机系统分析
- `IMPLEMENTATION_VERIFICATION.md` - 功能实现验证
- `DEBUGGING_GUIDE.md` - 调试指南

---

## 总结

MCU 接口集成是 DBTOOL 与车机系统深度集成的关键。通过实现 MCU 控制类，DBTOOL 可以:

✅ 直接控制车机硬件
✅ 支持更多媒体控制命令
✅ 提供更好的用户体验
✅ 与车机系统更紧密集成

**建议**: 与车机系统开发团队合作，获取完整的 MCU 接口文档，以便实现完整的功能。
