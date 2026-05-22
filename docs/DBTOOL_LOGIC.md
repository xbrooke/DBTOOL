# DBTOOL 处理逻辑详解

## 一、应用启动流程

### 1. 应用初始化 (DBToolApplication)
```
应用启动
  ↓
DBToolApplication.onCreate()
  ├─ 设置全局异常处理器
  ├─ 创建通知通道 (CHANNEL_ID: "dtool_service_channel")
  └─ 准备完成，等待 MainActivity 启动
```

**关键点：**
- 全局异常捕获：防止未捕获异常导致应用崩溃
- 通知通道创建：为 Android 8.0+ 的前台服务做准备

---

## 二、主界面流程 (MainActivity)

### 2.1 界面启动
```
MainActivity.onCreate()
  ↓
initViews() - 初始化 UI 组件
  ├─ tvStatus (状态显示)
  ├─ tvNowPlaying (当前播放信息)
  ├─ btnNotificationListener (通知监听按钮)
  ├─ btnAccessibility (无障碍服务按钮)
  └─ btnStartService (启动服务按钮)
  ↓
updateStatus() - 更新状态显示
  ├─ 检查 NotificationListenerService 是否已启用
  ├─ 检查 AccessibilityService 是否已启用
  └─ 显示当前播放的媒体信息
```

### 2.2 用户交互
```
用户点击按钮
  ├─ 通知监听按钮 → openNotificationListenerSettings()
  │   └─ 跳转到系统设置，让用户手动启用 NotificationListenerService
  │
  ├─ 无障碍服务按钮 → openAccessibilitySettings()
  │   └─ 跳转到系统设置，让用户手动启用 AccessibilityService
  │
  └─ 启动服务按钮 → startCoreService()
      └─ 启动 VehicleCoreService
```

---

## 三、核心服务流程 (VehicleCoreService)

### 3.1 服务启动
```
VehicleCoreService.onCreate()
  ↓
startForeground(NOTIFICATION_ID, createNotification())
  ├─ 创建前台通知
  ├─ 显示 "DBTOOL 运行中" 通知
  └─ 防止系统杀死服务
```

### 3.2 服务运行
```
VehicleCoreService.onStartCommand()
  ↓
checkNotificationListener()
  ├─ 检查 NotificationListenerService 是否已授权
  ├─ 如果已授权，继续监听媒体通知
  └─ 如果未授权，记录警告日志
  ↓
返回 START_STICKY
  └─ 服务被杀死后会自动重启
```

---

## 四、媒体通知监听流程 (MediaNotificationListener)

### 4.1 通知监听
```
系统媒体应用发送通知
  ↓
MediaNotificationListener.onNotificationPosted()
  ├─ 检查是否来自媒体应用 (网易云、QQ音乐、酷狗等)
  ├─ 解析通知内容
  │   ├─ 标题 (title)
  │   ├─ 艺术家 (artist)
  │   ├─ 专辑 (album)
  │   ├─ 播放状态 (isPlaying)
  │   └─ 专辑封面 (hasAlbumArt)
  ├─ 保存到 currentMedia (使用同步锁保证线程安全)
  ├─ 发送本地广播 (ACTION_MEDIA_UPDATED)
  └─ 通知 NowPlayingProvider 更新数据
```

### 4.2 通知移除
```
媒体应用移除通知
  ↓
MediaNotificationListener.onNotificationRemoved()
  ├─ 检查是否是媒体应用
  └─ 清空 currentMedia
```

### 4.3 支持的媒体应用
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

---

## 五、媒体信息提供流程 (NowPlayingProvider)

### 5.1 数据查询
```
车机系统查询媒体信息
  ↓
NowPlayingProvider.query(content://com.dtool.media/nowplaying)
  ├─ 获取当前媒体信息 (MediaNotificationListener.getCurrentMedia())
  ├─ 构建 MatrixCursor
  ├─ 填充数据行
  │   ├─ package_name → 伪装成 "cn.fanbook.android" (帆书)
  │   ├─ app_name → 伪装成 "帆书"
  │   ├─ title → 歌曲标题
  │   ├─ artist → 艺术家
  │   ├─ album → 专辑名
  │   ├─ state → 播放状态 (1=播放, 2=暂停)
  │   └─ 其他字段 (duration, position, album_art)
  └─ 返回 Cursor
```

### 5.2 伪装机制
```
真实应用信息
  ↓
NowPlayingProvider 处理
  ├─ 检测到来自 "com.netease.cloudmusic" 的媒体
  ├─ 但返回给车机的是 "cn.fanbook.android" (帆书)
  └─ 车机认为是帆书应用在播放音乐
```

**为什么要伪装？**
- 某些车机系统只识别特定应用的媒体信息
- 伪装成帆书可以确保车机正确识别和显示媒体信息
- 用户实际播放的应用信息不变，只是对车机隐藏真实来源

---

## 六、媒体控制流程 (DesktopCardReceiver)

### 6.1 接收控制命令
```
车机发送媒体控制广播
  ↓
DesktopCardReceiver.onReceive()
  ├─ 亿连协议 (ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER)
  │   ├─ action=0 → 播放
  │   ├─ action=1 → 暂停
  │   ├─ action=2 → 播放/暂停
  │   ├─ action=3 → 下一曲
  │   └─ action=4 → 上一曲
  │
  ├─ 极氪/几何协议
  │   ├─ ACTION_WIDGET_TOGGLE_PLAY → 播放/暂停
  │   ├─ ACTION_WIDGET_NEXT → 下一曲
  │   └─ ACTION_WIDGET_PREV → 上一曲
  │
  └─ 内部广播
      ├─ PLAY_PAUSE → 播放/暂停
      ├─ NEXT → 下一曲
      └─ PREV → 上一曲
```

### 6.2 执行控制
```
接收到控制命令
  ↓
sendMediaKey(context, streamType, keyCode)
  ├─ 获取 AudioManager
  ├─ 创建 KeyEvent (ACTION_DOWN)
  ├─ 发送按键事件
  ├─ 创建 KeyEvent (ACTION_UP)
  ├─ 发送按键事件
  └─ 媒体应用响应按键，执行相应操作
```

**支持的媒体按键：**
- KEYCODE_MEDIA_PLAY - 播放
- KEYCODE_MEDIA_PAUSE - 暂停
- KEYCODE_MEDIA_PLAY_PAUSE - 播放/暂停
- KEYCODE_MEDIA_NEXT - 下一曲
- KEYCODE_MEDIA_PREVIOUS - 上一曲

---

## 七、无障碍服务流程 (DBToolAccessibilityService)

### 7.1 服务连接
```
用户启用无障碍服务
  ↓
DBToolAccessibilityService.onServiceConnected()
  ├─ 配置服务信息
  │   ├─ 监听窗口状态变化
  │   ├─ 监听窗口内容变化
  │   ├─ 监听通知状态变化
  │   └─ 设置反馈类型和标志
  └─ 服务准备就绪
```

### 7.2 事件处理
```
系统发送无障碍事件
  ↓
DBToolAccessibilityService.onAccessibilityEvent()
  ├─ 记录事件信息
  ├─ 可用于监听特定应用的窗口变化
  └─ 为未来的功能扩展预留接口
```

### 7.3 媒体按键发送
```
需要控制媒体应用
  ↓
DBToolAccessibilityService.sendMediaKey(keyCode)
  ├─ 获取 AudioManager
  ├─ 创建并发送 KeyEvent (ACTION_DOWN)
  ├─ 创建并发送 KeyEvent (ACTION_UP)
  └─ 媒体应用响应按键
```

---

## 八、启动接收器流程 (BootReceiver)

### 8.1 监听事件
```
系统事件发生
  ↓
BootReceiver.onReceive()
  ├─ 设备启动完成 (BOOT_COMPLETED)
  ├─ 锁屏启动完成 (LOCKED_BOOT_COMPLETED)
  ├─ 快速启动 (QUICKBOOT_POWERON)
  ├─ 系统重启 (REBOOT)
  ├─ 电源连接 (ACTION_POWER_CONNECTED)
  ├─ 电源断开 (ACTION_POWER_DISCONNECTED)
  ├─ 屏幕打开 (SCREEN_ON)
  └─ 用户解锁 (USER_PRESENT)
```

### 8.2 启动服务
```
检测到相关事件
  ↓
启动 VehicleCoreService
  ├─ Android 8.0+ 使用 startForegroundService()
  └─ 旧版本使用 startService()
  ↓
服务启动，开始监听媒体通知
```

---

## 九、完整的数据流

```
┌─────────────────────────────────────────────────────────────┐
│                    用户播放音乐                              │
│              (网易云、QQ音乐等媒体应用)                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────┐
        │  系统发送媒体通知           │
        │  (Notification)            │
        └────────────┬───────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  MediaNotificationListener             │
        │  - 监听通知                            │
        │  - 解析媒体信息                        │
        │  - 保存到 currentMedia                 │
        │  - 发送本地广播                        │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  NowPlayingProvider                    │
        │  - 车机查询媒体信息                    │
        │  - 伪装成帆书应用                      │
        │  - 返回媒体数据                        │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  车机系统                              │
        │  - 显示媒体信息                        │
        │  - 显示播放控制按钮                    │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  用户点击车机上的控制按钮              │
        │  (播放/暂停/下一曲/上一曲)             │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  DesktopCardReceiver                   │
        │  - 接收控制广播                        │
        │  - 转换为媒体按键                      │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  AudioManager                          │
        │  - 发送媒体按键事件                    │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  媒体应用                              │
        │  - 接收按键事件                        │
        │  - 执行相应操作                        │
        │  (播放/暂停/下一曲/上一曲)             │
        └────────────┬───────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────────────────┐
        │  系统发送新的媒体通知                  │
        │  (循环回到第一步)                      │
        └────────────────────────────────────────┘
```

---

## 十、权限和安全机制

### 10.1 所需权限
```
基础权限
├─ INTERNET - 网络访问
├─ ACCESS_NETWORK_STATE - 网络状态
├─ WAKE_LOCK - 唤醒锁
└─ FOREGROUND_SERVICE - 前台服务

存储权限
├─ READ_EXTERNAL_STORAGE - 读取存储
└─ WRITE_EXTERNAL_STORAGE - 写入存储

系统权限
├─ BIND_NOTIFICATION_LISTENER_SERVICE - 通知监听
├─ BIND_ACCESSIBILITY_SERVICE - 无障碍服务
└─ FOREGROUND_SERVICE_DATA_SYNC - 前台服务数据同步
```

### 10.2 自定义权限
```
ACCESS_MEDIA_INFO (signature 级别)
├─ 用途：访问媒体信息
├─ 保护级别：signature (只有签名相同的应用才能使用)
└─ 应用：NowPlayingProvider

MEDIA_CONTROL (signature 级别)
├─ 用途：媒体控制
├─ 保护级别：signature
└─ 应用：DesktopCardReceiver

DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION (signature 级别)
├─ 用途：动态广播接收器
├─ 保护级别：signature
└─ 应用：内部使用
```

### 10.3 组件保护
```
ContentProvider (NowPlayingProvider)
├─ android:exported="true" - 允许其他应用访问
├─ android:grantUriPermissions="true" - 授予 URI 权限
└─ android:permission="com.dtool.permission.ACCESS_MEDIA_INFO" - 权限检查

BroadcastReceiver (DesktopCardReceiver)
├─ android:exported="true" - 允许接收外部广播
└─ android:permission="com.dtool.permission.MEDIA_CONTROL" - 权限检查

Service (MediaNotificationListener, DBToolAccessibilityService)
├─ android:exported="true" - 允许系统绑定
└─ android:permission - 系统权限检查
```

---

## 十一、线程安全机制

### 11.1 同步锁
```
MediaNotificationListener.currentMedia
├─ 使用 synchronized(lock) 保护
├─ 防止多线程并发访问
└─ 确保数据一致性
```

### 11.2 弱引用
```
DBToolAccessibilityService.instanceRef
├─ 使用 WeakReference<DBToolAccessibilityService>
├─ 防止内存泄漏
└─ 允许垃圾回收
```

---

## 十二、错误处理

### 12.1 全局异常处理
```
Thread.setDefaultUncaughtExceptionHandler()
├─ 捕获未捕获的异常
├─ 记录异常日志
└─ 可选：上报异常或重启应用
```

### 12.2 本地异常处理
```
各个组件中的 try-catch
├─ 打开设置失败
├─ 启动服务失败
├─ 发送媒体按键失败
└─ 查询权限失败
```

---

## 十三、关键特性总结

| 特性 | 说明 |
|------|------|
| **媒体监听** | 实时监听系统媒体通知，支持11种媒体应用 |
| **信息伪装** | 将媒体信息伪装成帆书应用，兼容特定车机系统 |
| **媒体控制** | 支持亿连和极氪/几何两种车机协议 |
| **前台服务** | 使用前台服务确保后台运行不被杀死 |
| **权限管理** | 使用 signature 级别权限保护敏感操作 |
| **线程安全** | 使用同步锁和弱引用保证数据安全 |
| **自动启动** | 监听系统事件自动启动服务 |
| **无障碍服务** | 支持通过无障碍服务发送媒体按键 |

---

## 十四、应用场景

DBTOOL 主要用于：

1. **车机媒体集成**
   - 将手机上的媒体信息同步到车机
   - 通过车机控制手机媒体播放

2. **多媒体应用支持**
   - 支持网易云、QQ音乐等多种应用
   - 统一的媒体信息接口

3. **车机协议兼容**
   - 支持亿连 (ecarx) 协议
   - 支持极氪/几何 (geely) 协议
   - 伪装成帆书应用确保兼容性

4. **后台持久运行**
   - 前台服务确保不被系统杀死
   - 自动启动机制确保服务持续运行
