# DBTOOL 无法监听信息 - 诊断和修复指南

## 问题分析

### 根本原因
**MediaNotificationListener 是一个系统级服务，不能通过 startService() 启动。** 它必须由用户在系统设置中手动启用。

### 为什么无法监听
1. ❌ NotificationListenerService 未在系统设置中启用
2. ❌ 应用没有通知监听权限
3. ❌ 媒体应用不在支持列表中
4. ❌ 通知内容格式不符合预期
5. ❌ 系统权限被撤销

---

## 诊断步骤

### 步骤 1：检查服务是否启用

**在 Android 设备上：**
```
设置 → 应用和通知 → 高级 → 特殊应用权限 → 通知访问
```

**查看 DBTOOL 是否在列表中且已启用**

如果没有看到 DBTOOL：
- 点击"允许访问通知"
- 找到 DBTOOL
- 启用它

### 步骤 2：检查权限

**在 Android 设备上：**
```
设置 → 应用 → DBTOOL → 权限
```

**确保以下权限已授予：**
- ✅ 通知
- ✅ 存储
- ✅ 其他应用

### 步骤 3：检查媒体应用

**确保你正在使用的媒体应用在支持列表中：**
- 网易云音乐 (com.netease.cloudmusic)
- QQ音乐 (com.tencent.qqmusic)
- 酷狗音乐 (com.kugou.player)
- 千千音乐 (com.baidu.music)
- 喜马拉雅 (com.ximalaya.ting)
- 懒人听书 (com.qianqian.audio)
- 印象笔记 (com.evernote)
- 虾米音乐 (com.xm.sparta)
- 微信音乐 (org.cocos11.wechat)
- 抖音 (com.ss.android.ugc.aweme)
- 抖音Lite (com.ss.android.ugc.aweme.lite)

### 步骤 4：检查日志

**在电脑上运行：**
```bash
adb logcat | grep -E "MediaNotificationListener|DBTOOL"
```

**查看是否有以下日志：**
- ✅ "MediaNotificationListener已创建"
- ✅ "媒体通知已更新"
- ✅ "伪装package"

**如果看到以下日志说明有问题：**
- ❌ "无障碍服务未连接"
- ❌ "检查通知监听失败"
- ❌ "启动服务失败"

### 步骤 5：测试通知监听

**在电脑上运行测试命令：**
```bash
# 发送测试通知
adb shell am broadcast -a android.intent.action.MEDIA_BUTTON \
  --ei keycode 79 \
  -n com.dtool/.receiver.DesktopCardReceiver

# 查看是否收到
adb logcat | grep "DesktopCardReceiver"
```

---

## 常见问题和解决方案

### 问题 1：应用启动后无法看到媒体信息

**症状：**
- MainActivity 中 "暂无播放" 一直显示
- 即使在播放音乐也没有信息

**原因：**
1. NotificationListenerService 未启用
2. 媒体应用不在支持列表中
3. 权限未授予

**解决方案：**
```
1. 打开 DBTOOL 应用
2. 点击 "通知监听" 按钮
3. 在系统设置中找到 DBTOOL
4. 启用通知访问权限
5. 返回应用，播放音乐
6. 检查是否显示媒体信息
```

### 问题 2：权限检查显示 "未启用"

**症状：**
- MainActivity 显示 "通知监听: 未启用"
- 即使已在设置中启用

**原因：**
- 权限检查代码有问题
- 系统权限被撤销

**解决方案：**
```bash
# 检查权限状态
adb shell dumpsys notification | grep com.dtool

# 重新授予权限
adb shell pm grant com.dtool android.permission.BIND_NOTIFICATION_LISTENER_SERVICE

# 重启应用
adb shell am force-stop com.dtool
adb shell am start -n com.dtool/.activity.MainActivity
```

### 问题 3：只能监听某些应用

**症状：**
- 网易云音乐可以监听
- QQ音乐无法监听

**原因：**
- 不同应用的通知格式不同
- 通知内容可能被加密或隐藏

**解决方案：**
```
1. 在 MediaNotificationListener 中添加日志
2. 查看该应用的通知内容
3. 调整解析逻辑
4. 添加到支持列表
```

### 问题 4：监听到信息但车机无法显示

**症状：**
- DBTOOL 能监听到媒体信息
- 但车机上看不到

**原因：**
1. NowPlayingProvider 未被正确查询
2. 车机权限不足
3. 伪装信息不正确

**解决方案：**
```bash
# 测试 ContentProvider
adb shell content query --uri content://com.dtool.media/nowplaying

# 检查权限
adb shell pm list permissions | grep dtool

# 查看日志
adb logcat | grep "NowPlayingProvider"
```

### 问题 5：车机控制无效

**症状：**
- 车机能显示媒体信息
- 但点击控制按钮无效

**原因：**
1. DesktopCardReceiver 未接收到广播
2. 媒体应用不支持按键事件
3. 权限不足

**解决方案：**
```bash
# 测试广播接收
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 2 \
  -n com.dtool/.receiver.DesktopCardReceiver

# 查看日志
adb logcat | grep "DesktopCardReceiver"

# 检查媒体应用是否支持按键
adb shell dumpsys media_session | grep -A 10 "com.netease.cloudmusic"
```

---

## 完整的启用流程

### 第一次使用 DBTOOL

**1. 安装应用**
```bash
adb install app-release.apk
```

**2. 打开应用**
```bash
adb shell am start -n com.dtool/.activity.MainActivity
```

**3. 启用通知监听**
- 点击应用中的 "通知监听" 按钮
- 或手动进入：设置 → 应用和通知 → 高级 → 特殊应用权限 → 通知访问
- 找到 DBTOOL 并启用

**4. 启用无障碍服务（可选，用于媒体控制）**
- 点击应用中的 "无障碍服务" 按钮
- 或手动进入：设置 → 无障碍 → 已安装的服务
- 找到 DBTOOL 并启用

**5. 启动服务**
- 点击应用中的 "启动服务" 按钮
- 或等待系统自动启动（BootReceiver 会在系统启动时自动启动）

**6. 播放音乐**
- 打开任何支持的媒体应用
- 播放音乐
- 检查 DBTOOL 是否显示媒体信息

**7. 连接车机**
- 车机应该能查询到媒体信息
- 点击车机上的控制按钮应该能控制播放

---

## 调试技巧

### 启用详细日志

**修改 MediaNotificationListener.java：**
```java
@Override
public void onNotificationPosted(StatusBarNotification sbn) {
    String packageName = sbn.getPackageName();
    Notification notification = sbn.getNotification();
    
    // 添加详细日志
    Log.d(TAG, "收到通知: " + packageName);
    Log.d(TAG, "通知内容: " + notification.extras);
    
    // ... 其他代码
}
```

### 监听所有通知（用于调试）

**临时修改 isMediaNotification() 方法：**
```java
private boolean isMediaNotification(String packageName, Notification notification) {
    // 调试模式：监听所有通知
    Log.d(TAG, "检查通知: " + packageName);
    
    if (notification == null) return false;
    if (notification.extras == null) return false;
    
    CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
    Log.d(TAG, "通知标题: " + title);
    
    // 只监听有标题的通知
    return title != null && title.length() > 0;
}
```

### 查看所有通知

```bash
# 实时查看所有通知
adb shell dumpsys notification | grep -A 5 "mNotifications"

# 查看特定应用的通知
adb shell dumpsys notification | grep -A 10 "com.netease.cloudmusic"
```

---

## 性能优化建议

### 1. 减少日志输出
```java
// 生产环境中注释掉日志
// Log.d(TAG, "媒体通知已更新: " + mediaInfo.title);
```

### 2. 优化通知解析
```java
// 缓存应用名称，避免重复查询
private static Map<String, String> appNameCache = new HashMap<>();

private String getAppName(String packageName) {
    if (appNameCache.containsKey(packageName)) {
        return appNameCache.get(packageName);
    }
    
    String appName = packageName;
    try {
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageInfo(packageName, 0);
        appName = info.applicationInfo.loadLabel(pm).toString();
    } catch (PackageManager.NameNotFoundException e) {
        // 使用包名作为备选
    }
    
    appNameCache.put(packageName, appName);
    return appName;
}
```

### 3. 限制广播频率
```java
// 避免频繁发送广播
private long lastBroadcastTime = 0;
private static final long BROADCAST_INTERVAL = 500; // 500ms

private void broadcastMediaUpdate(MediaInfo mediaInfo) {
    long now = System.currentTimeMillis();
    if (now - lastBroadcastTime < BROADCAST_INTERVAL) {
        return; // 忽略频繁更新
    }
    lastBroadcastTime = now;
    
    // ... 发送广播
}
```

---

## 验证清单

在报告问题前，请确保：

- [ ] 已在系统设置中启用 NotificationListenerService
- [ ] 已授予所有必要的权限
- [ ] 正在使用支持的媒体应用
- [ ] 已启动 VehicleCoreService
- [ ] 已检查日志中是否有错误
- [ ] 已尝试重启应用和设备
- [ ] 已尝试卸载并重新安装应用

---

## 获取帮助

如果问题仍未解决，请提供以下信息：

1. **设备信息**
   ```bash
   adb shell getprop ro.build.version.release  # Android 版本
   adb shell getprop ro.product.model          # 设备型号
   ```

2. **应用日志**
   ```bash
   adb logcat -d > logcat.txt
   ```

3. **系统信息**
   ```bash
   adb shell dumpsys notification > notification.txt
   adb shell dumpsys media_session > media_session.txt
   ```

4. **权限信息**
   ```bash
   adb shell pm list permissions -g | grep dtool
   ```

5. **详细描述**
   - 你在做什么
   - 期望发生什么
   - 实际发生了什么
   - 错误信息或日志
