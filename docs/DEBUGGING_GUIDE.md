# DBTOOL 调试指南

## 问题: 无法监听到信息，功能无法正常使用

本指南帮助您诊断和解决 DBTOOL 无法监听到音乐应用信息的问题。

---

## 快速诊断清单

在开始详细调试前，请按照以下清单检查:

- [ ] 已在系统设置中启用通知监听权限
- [ ] 已在系统设置中启用无障碍服务
- [ ] 已启动 DBTOOL 核心服务
- [ ] 正在播放的音乐应用在支持列表中
- [ ] 音乐应用已发送通知

---

## 第一步: 检查权限启用状态

### 1.1 启用通知监听权限

**步骤**:
1. 打开系统设置
2. 进入 **应用和通知** → **通知** → **通知访问权限**
3. 找到 **DBTOOL** 应用
4. 启用权限

**验证方法**:
- 在 DBTOOL 主界面，检查 "通知监听" 是否显示 **✓ 已启用**
- 如果显示 **✗ 未启用**，说明权限未正确启用

**logcat 日志**:
```
D/MediaNotificationListener: === MediaNotificationListener 已连接 ===
D/MediaNotificationListener: 通知监听服务已启用，开始监听媒体通知
```

### 1.2 启用无障碍服务

**步骤**:
1. 打开系统设置
2. 进入 **无障碍** → **已安装的服务**
3. 找到 **DBTOOL 无障碍服务**
4. 启用服务

**验证方法**:
- 在 DBTOOL 主界面，检查 "辅助服务" 是否显示 **✓ 已启用**
- 如果显示 **✗ 未启用**，说明服务未正确启用

### 1.3 启动核心服务

**步骤**:
1. 打开 DBTOOL 应用
2. 点击 **启动服务** 按钮
3. 看到 "服务已启动" 提示

**验证方法**:
- 在 DBTOOL 主界面，检查 "核心服务" 是否显示 **✓ 运行中**
- 如果显示 **✗ 未运行**，点击 **启动服务** 按钮

---

## 第二步: 检查应用支持列表

### 2.1 查看支持的音乐应用

DBTOOL 目前支持以下应用:

| 应用名称 | Package Name | 状态 |
|---------|-------------|------|
| 网易云音乐 | `com.netease.cloudmusic` | ✅ 支持 |
| QQ音乐 | `com.tencent.qqmusic` | ✅ 支持 |
| 酷狗音乐 | `com.kugou.player` | ✅ 支持 |
| 千千音乐 | `com.baidu.music` | ✅ 支持 |
| 喜马拉雅 | `com.ximalaya.ting` | ✅ 支持 |
| 懒人听书 | `com.qianqian.audio` | ✅ 支持 |
| 虾米音乐 | `com.xm.sparta` | ✅ 支持 |
| 微信音乐 | `org.cocos11.wechat` | ✅ 支持 |
| 抖音 | `com.ss.android.ugc.aweme` | ✅ 支持 |
| 抖音Lite | `com.ss.android.ugc.aweme.lite` | ✅ 支持 |
| 印象笔记 | `com.evernote` | ✅ 支持 |

### 2.2 检查您的应用是否在列表中

**方法 1: 通过 adb 查询应用 package name**

```bash
# 列出所有已安装的应用
adb shell pm list packages | grep -i music

# 或查询特定应用
adb shell pm list packages | grep -i qq
```

**方法 2: 通过 logcat 查看**

1. 启动音乐应用并播放音乐
2. 运行: `adb logcat | grep MediaNotificationListener`
3. 查看日志中的 package name

**示例日志**:
```
D/MediaNotificationListener: 收到通知: com.netease.cloudmusic
D/MediaNotificationListener: 检查是否是媒体应用: com.netease.cloudmusic
D/MediaNotificationListener: 不是媒体应用: com.netease.cloudmusic  ← 说明应用不在列表中
```

### 2.3 添加不支持的应用

如果您的应用不在支持列表中，需要修改源代码:

**文件**: `app/src/main/java/com/dtool/service/MediaNotificationListener.java`

**修改位置** (第 35-46 行):
```java
private static final String[] MEDIA_PACKAGES = {
    "com.netease.cloudmusic",     // 网易云音乐
    "com.tencent.qqmusic",        // QQ音乐
    // ... 其他应用
    "YOUR_APP_PACKAGE_NAME",      // ← 添加您的应用
};
```

**获取应用 package name 的方法**:
```bash
# 方法 1: 通过 adb
adb shell pm list packages | grep -i "应用名称关键词"

# 方法 2: 通过 Settings 应用
# 打开 Settings → 应用 → 应用信息 → 查看 package name

# 方法 3: 通过 logcat
# 启动应用，运行: adb logcat | grep "ActivityManager"
```

---

## 第三步: 检查通知发送

### 3.1 验证应用是否发送通知

**方法 1: 通过 logcat 检查**

```bash
# 启动音乐应用并播放音乐
adb logcat | grep MediaNotificationListener
```

**预期日志**:
```
D/MediaNotificationListener: 收到通知: com.netease.cloudmusic
D/MediaNotificationListener: 检测到媒体通知: com.netease.cloudmusic
D/MediaNotificationListener: 解析通知 - 标题: 歌曲名, 艺术家: 歌手名, 专辑: 专辑名
D/MediaNotificationListener: 媒体通知已更新: 歌曲名 - 歌手名
```

**问题日志**:
```
D/MediaNotificationListener: 收到通知: com.netease.cloudmusic
D/MediaNotificationListener: 不是媒体通知: com.netease.cloudmusic  ← 应用不在列表中
```

或

```
D/MediaNotificationListener: 收到通知: com.netease.cloudmusic
D/MediaNotificationListener: 通知 extras 为空: com.netease.cloudmusic  ← 通知格式不支持
```

### 3.2 检查通知权限

某些应用可能需要额外的权限才能发送通知:

```bash
# 检查应用权限
adb shell dumpsys package com.netease.cloudmusic | grep -i permission
```

---

## 第四步: 详细日志分析

### 4.1 启用详细日志

```bash
# 清空日志
adb logc

# 启动 DBTOOL
adb shell am start -n com.dtool/.activity.MainActivity

# 启动音乐应用并播放音乐

# 收集日志
adb logcat > dbtool_debug.log
```

### 4.2 关键日志点

| 日志内容 | 含义 | 解决方案 |
|---------|------|--------|
| `=== MediaNotificationListener 已连接 ===` | 通知监听服务已启用 | ✅ 正常 |
| `=== MediaNotificationListener 已断开连接 ===` | 通知监听服务已禁用 | ❌ 需要启用权限 |
| `收到通知: com.xxx.xxx` | 收到应用通知 | ✅ 正常 |
| `不是媒体应用: com.xxx.xxx` | 应用不在支持列表中 | ❌ 需要添加应用 |
| `通知 extras 为空: com.xxx.xxx` | 通知格式不支持 | ❌ 应用通知格式不兼容 |
| `媒体通知已更新: 标题 - 艺术家` | 成功解析媒体信息 | ✅ 正常 |

### 4.3 完整日志示例

**正常工作的日志**:
```
D/MediaNotificationListener: === MediaNotificationListener 已连接 ===
D/MediaNotificationListener: 通知监听服务已启用，开始监听媒体通知
D/MediaNotificationListener: 收到通知: com.netease.cloudmusic
D/MediaNotificationListener: 检查是否是媒体应用: com.netease.cloudmusic
D/MediaNotificationListener: 检测到媒体通知: com.netease.cloudmusic
D/MediaNotificationListener: 解析通知 - 标题: 晴天, 艺术家: 周杰伦, 专辑: 叶惠美
D/MediaNotificationListener: 媒体通知已更新: 晴天 - 周杰伦
D/NowPlayingProvider: query: content://com.dtool.media/nowplaying
D/NowPlayingProvider: 伪装package: com.netease.cloudmusic -> cn.fanbook.android
D/NowPlayingProvider: 伪装app_name: 网易云音乐 -> 帆书
D/NowPlayingProvider: 返回Cursor, 数量: 1
```

**异常工作的日志**:
```
D/MediaNotificationListener: === MediaNotificationListener 已断开连接 ===
D/MediaNotificationListener: 通知监听服务已禁用，停止监听媒体通知
```

---

## 第五步: 测试控制命令转发

### 5.1 验证方向盘命令接收

```bash
# 模拟发送方向盘命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 2 \
  --es package_name com.netease.cloudmusic
```

**预期日志**:
```
D/DesktopCardReceiver: 收到广播: ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER
D/DesktopCardReceiver: 亿连协议: package=com.netease.cloudmusic, action=2
D/DesktopCardReceiver: 处理播放/暂停
D/DesktopCardReceiver: 发送媒体按键: 85
```

### 5.2 验证媒体按键转发

```bash
# 检查 AudioManager 是否成功分发按键
adb logcat | grep "发送媒体按键"
```

---

## 常见问题和解决方案

### 问题 1: 通知监听权限无法启用

**症状**:
- 在系统设置中找不到 DBTOOL 应用
- 无法启用通知监听权限

**解决方案**:
1. 确保 DBTOOL 已正确安装
2. 重启手机
3. 在 DBTOOL 主界面点击 "启用通知监听" 按钮
4. 系统会自动打开设置页面

### 问题 2: 应用不在支持列表中

**症状**:
- logcat 显示 "不是媒体应用"
- 无法监听到应用的音乐信息

**解决方案**:
1. 获取应用的 package name
2. 修改 `MediaNotificationListener.java` 中的 `MEDIA_PACKAGES` 数组
3. 重新编译和安装 APK

### 问题 3: 通知格式不支持

**症状**:
- logcat 显示 "通知 extras 为空"
- 应用发送通知，但无法解析

**解决方案**:
1. 检查应用是否使用了自定义通知格式
2. 修改 `parseMediaNotification()` 方法以支持新格式
3. 可能需要联系应用开发者获取通知格式文档

### 问题 4: 服务无法启动

**症状**:
- 点击 "启动服务" 后没有反应
- logcat 显示异常

**解决方案**:
1. 检查是否已启用所有必要的权限
2. 检查 logcat 中的异常信息
3. 尝试重启应用
4. 尝试重启手机

### 问题 5: 命令无法转发到音乐应用

**症状**:
- 接收到方向盘命令，但音乐应用没有响应
- logcat 显示 "发送媒体按键" 但没有效果

**解决方案**:
1. 检查音乐应用是否支持媒体按键事件
2. 检查无障碍服务是否已启用
3. 尝试手动按下手机上的媒体按键，检查音乐应用是否响应
4. 某些应用可能需要额外的权限或配置

---

## 高级调试技巧

### 技巧 1: 实时监控日志

```bash
# 启动实时日志监控
adb logcat -s MediaNotificationListener:D DesktopCardReceiver:D NowPlayingProvider:D
```

### 技巧 2: 导出完整日志

```bash
# 导出日志到文件
adb logcat > dbtool_full_debug.log

# 导出特定时间范围的日志
adb logcat -d > dbtool_snapshot.log
```

### 技巧 3: 检查系统通知

```bash
# 列出所有通知
adb shell dumpsys notification

# 查看特定应用的通知
adb shell dumpsys notification | grep -A 20 "com.netease.cloudmusic"
```

### 技巧 4: 检查权限状态

```bash
# 检查 DBTOOL 的权限
adb shell dumpsys package com.dtool | grep -i permission

# 检查通知监听权限
adb shell settings get secure enabled_notification_listeners
```

### 技巧 5: 模拟各种场景

```bash
# 模拟播放命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 0 --es package_name com.netease.cloudmusic

# 模拟暂停命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 1 --es package_name com.netease.cloudmusic

# 模拟下一曲命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 3 --es package_name com.netease.cloudmusic

# 模拟上一曲命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 4 --es package_name com.netease.cloudmusic
```

---

## 联系支持

如果按照以上步骤仍无法解决问题，请收集以下信息并联系开发者:

1. **设备信息**:
   - 手机型号
   - Android 版本
   - DBTOOL 版本

2. **日志信息**:
   - 完整的 logcat 输出 (adb logcat > debug.log)
   - 特别是 MediaNotificationListener 的日志

3. **应用信息**:
   - 正在使用的音乐应用名称和版本
   - 应用的 package name

4. **问题描述**:
   - 具体现象
   - 已尝试的解决方案
   - 预期行为

---

## 总结

DBTOOL 无法监听到信息的常见原因:

1. ❌ **权限未启用** - 需要在系统设置中启用通知监听和无障碍服务
2. ❌ **应用不在列表中** - 需要添加应用的 package name
3. ❌ **通知格式不支持** - 某些应用使用自定义通知格式
4. ❌ **服务未启动** - 需要点击 "启动服务" 按钮
5. ❌ **权限冲突** - 某些系统可能有权限限制

按照本指南的步骤逐一排查，应该能够解决大多数问题。
