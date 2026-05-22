# DBTOOL 快速参考

## 核心功能一句话总结

**DBTOOL 是一个媒体信息中转站：监听手机上的音乐播放，将信息伪装成特定应用格式发送给车机，同时接收车机的控制命令并转发给音乐应用。**

---

## 主要组件速查表

| 组件 | 类名 | 功能 | 关键方法 |
|------|------|------|---------|
| 应用入口 | DBToolApplication | 初始化、异常处理 | onCreate() |
| 主界面 | MainActivity | UI 展示、权限激活 | onCreate(), updateStatus() |
| 核心服务 | VehicleCoreService | 前台服务、生命周期 | onCreate(), onStartCommand() |
| 通知监听 | MediaNotificationListener | 监听媒体通知 | onNotificationPosted() |
| 数据提供 | NowPlayingProvider | 提供媒体信息给车机 | query(), call() |
| 控制接收 | DesktopCardReceiver | 接收车机控制 | onReceive() |
| 启动接收 | BootReceiver | 系统启动事件 | onReceive() |
| 无障碍服务 | DBToolAccessibilityService | 发送媒体按键 | sendMediaKey() |

---

## 数据流向速查

```
播放音乐 → 系统通知 → MediaNotificationListener → currentMedia
                                                      ↓
                                                  NowPlayingProvider
                                                      ↓
                                                   车机查询
                                                      ↓
                                                  返回伪装数据
                                                      ↓
                                                   车机显示
                                                      ↓
                                                  用户点击
                                                      ↓
                                                DesktopCardReceiver
                                                      ↓
                                                  AudioManager
                                                      ↓
                                                  媒体应用
```

---

## 权限检查清单

### 用户需要手动启用的权限
- [ ] 通知监听服务 (Notification Listener Service)
- [ ] 无障碍服务 (Accessibility Service)

### 系统自动授予的权限
- [x] 网络访问
- [x] 前台服务
- [x] 存储访问

---

## 支持的媒体应用

```
网易云音乐      com.netease.cloudmusic
QQ音乐         com.tencent.qqmusic
酷狗音乐       com.kugou.player
千千音乐       com.baidu.music
喜马拉雅       com.ximalaya.ting
懒人听书       com.qianqian.audio
印象笔记       com.evernote
虾米音乐       com.xm.sparta
微信音乐       org.cocos11.wechat
抖音          com.ss.android.ugc.aweme
抖音Lite       com.ss.android.ugc.aweme.lite
```

---

## 支持的车机协议

### 亿连协议 (ecarx)
```
Action: ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER
Extra: media_action
  0 = 播放
  1 = 暂停
  2 = 播放/暂停
  3 = 下一曲
  4 = 上一曲
```

### 极氪/几何协议 (geely)
```
com.geely.mediawidget.ACTION_WIDGET_TOGGLE_PLAY  → 播放/暂停
com.geely.mediawidget.ACTION_WIDGET_NEXT         → 下一曲
com.geely.mediawidget.ACTION_WIDGET_PREV         → 上一曲
```

---

## 关键常量

| 常量 | 值 | 说明 |
|------|-----|------|
| CHANNEL_ID | dtool_service_channel | 通知通道 ID |
| AUTHORITY | com.dtool.media | ContentProvider 权限 |
| FAKE_PACKAGE_NAME | cn.fanbook.android | 伪装应用包名 |
| FAKE_APP_NAME | 帆书 | 伪装应用名称 |
| NOTIFICATION_ID | 1001 | 前台服务通知 ID |

---

## 常见问题排查

### 问题：应用闪退
**原因：** FileProvider 配置错误
**解决：** 检查 file_paths.xml 和 AndroidManifest.xml 中的 FileProvider 配置

### 问题：无法监听媒体通知
**原因：** NotificationListenerService 未启用
**解决：** 点击"通知监听"按钮，在系统设置中启用

### 问题：车机无法显示媒体信息
**原因：** NowPlayingProvider 未被正确查询
**解决：** 检查车机是否有权限访问 content://com.dtool.media/nowplaying

### 问题：车机控制无效
**原因：** DesktopCardReceiver 未接收到广播或媒体应用不支持按键
**解决：** 检查广播权限和媒体应用是否支持媒体按键

### 问题：服务被系统杀死
**原因：** 前台服务配置不正确
**解决：** 确保 VehicleCoreService 正确调用 startForeground()

---

## 调试技巧

### 查看日志
```bash
adb logcat | grep -E "DBTOOL|MediaNotificationListener|NowPlayingProvider|DesktopCardReceiver"
```

### 检查服务状态
```bash
adb shell dumpsys activity services | grep com.dtool
```

### 检查权限
```bash
adb shell pm list permissions | grep dtool
```

### 发送测试广播
```bash
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 2 \
  -n com.dtool/.receiver.DesktopCardReceiver
```

---

## 性能指标

| 指标 | 值 | 说明 |
|------|-----|------|
| 最小 SDK | 28 | Android 9.0+ |
| 目标 SDK | 34 | Android 14 |
| 编译 SDK | 34 | Android 14 |
| 应用大小 | ~5MB | 基础 APK |
| 内存占用 | ~50MB | 运行时 |
| 电池消耗 | 低 | 前台服务 |

---

## 文件结构

```
app/src/main/
├── java/com/dtool/
│   ├── DBToolApplication.java          # 应用入口
│   ├── activity/
│   │   ├── MainActivity.java           # 主界面
│   │   └── ActivationActivity.java     # 激活界面
│   ├── service/
│   │   ├── VehicleCoreService.java     # 核心服务
│   │   ├── MediaNotificationListener.java  # 通知监听
│   │   └── DBToolAccessibilityService.java # 无障碍服务
│   ├── receiver/
│   │   ├── BootReceiver.java           # 启动接收
│   │   └── DesktopCardReceiver.java    # 控制接收
│   └── provider/
│       └── NowPlayingProvider.java     # 数据提供
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── activity_activation.xml
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── xml/
│       ├── file_paths.xml
│       └── accessibility_service_config.xml
└── AndroidManifest.xml
```

---

## 开发建议

### 添加新的媒体应用支持
1. 在 `MediaNotificationListener.MEDIA_PACKAGES` 中添加包名
2. 测试通知解析是否正确
3. 更新文档

### 添加新的车机协议支持
1. 在 `DesktopCardReceiver.onReceive()` 中添加新的 action 处理
2. 实现相应的控制逻辑
3. 测试广播接收和命令执行

### 优化性能
1. 使用 ProGuard 混淆代码
2. 减少日志输出
3. 优化内存使用
4. 使用 WorkManager 替代定时任务

### 增强安全性
1. 使用更高级别的权限保护
2. 验证广播来源
3. 加密敏感数据
4. 定期安全审计

---

## 相关文档

- `DBTOOL_LOGIC.md` - 详细处理逻辑
- `ARCHITECTURE.md` - 系统架构设计
- `README.md` - 项目概述
- `CHANGELOG.md` - 版本历史
- `QUICK_START.md` - 快速开始指南
