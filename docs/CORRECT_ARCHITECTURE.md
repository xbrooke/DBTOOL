# DBTOOL 正确架构 - 车机媒体中转站

## 应用定位（正确理解）

**DBTOOL 是安装在车机平板上的应用，用于：**
1. **监听** 车机上第三方音乐应用的媒体广播
2. **伪装** 成帆书应用，向车机系统报告媒体信息
3. **接收** 车机方控按键和仪表盘的控制命令
4. **转发** 控制命令给第三方音乐应用

---

## 完整的数据流

```
┌─────────────────────────────────────────────────────────────┐
│                      车机平板设备                            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  第三方音乐应用 (网易云、QQ音乐等)                   │  │
│  │  - 播放音乐                                          │  │
│  │  - 发送媒体广播                                      │  │
│  │    (标题、艺术家、专辑、封面、歌词等)                │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↓                                 │
│                    (系统广播)                                │
│                            ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           DBTOOL 应用                               │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  1. 监听模块                                │  │  │
│  │  │  - 监听第三方应用的媒体广播                 │  │  │
│  │  │  - 解析媒体信息                             │  │  │
│  │  │  - 提取专辑封面和歌词                       │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  2. 伪装模块                                │  │  │
│  │  │  - 将媒体信息伪装成帆书应用                 │  │  │
│  │  │  - 通过 ContentProvider 提供给车机系统      │  │  │
│  │  │  - 通过广播发送给车机系统                   │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  3. 控制模块                                │  │  │
│  │  │  - 接收车机方控按键命令                     │  │  │
│  │  │  - 接收仪表盘控制命令                       │  │  │
│  │  │  - 转发给第三方音乐应用                     │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  4. UI 模块                                 │  │  │
│  │  │  - 显示媒体信息                             │  │  │
│  │  │  - 显示专辑封面                             │  │  │
│  │  │  - 显示歌词                                 │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↕                                 │
│                    (系统广播)                                │
│                            ↕                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  车机系统 (方控、仪表盘、中控屏等)                   │  │
│  │  - 显示媒体信息                                      │  │
│  │  - 显示专辑封面和歌词                                │  │
│  │  - 提供方控按键                                      │  │
│  │  - 提供仪表盘控制                                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↓                                 │
│                    (系统广播)                                │
│                            ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  第三方音乐应用                                      │  │
│  │  - 接收控制命令                                      │  │
│  │  - 执行播放/暂停/下一曲/上一曲等操作                │  │
│  │  - 发送新的媒体广播 (循环)                           │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 核心功能详解

### 1. 监听第三方应用广播

**监听的广播类型：**
```
android.intent.action.MEDIA_BUTTON
android.media.METADATA_CHANGED
android.media.PLAYBACK_STATE_CHANGED
com.android.music.metachanged
com.android.music.playstatechanged
com.netease.cloudmusic.METADATA_CHANGED
com.tencent.qqmusic.METADATA_CHANGED
... (其他应用的自定义广播)
```

**解析的媒体信息：**
```
- 歌曲标题 (title)
- 艺术家 (artist)
- 专辑名称 (album)
- 专辑封面 (album_art)
- 歌词 (lyrics)
- 播放状态 (playing/paused)
- 播放进度 (position/duration)
```

**实现方式：**
```java
// 注册广播接收器
IntentFilter filter = new IntentFilter();
filter.addAction("android.intent.action.MEDIA_BUTTON");
filter.addAction("android.media.METADATA_CHANGED");
// ... 添加其他广播

registerReceiver(mediaReceiver, filter);

// 在 onReceive 中解析媒体信息
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    
    // 解析媒体信息
    String title = intent.getStringExtra("title");
    String artist = intent.getStringExtra("artist");
    String album = intent.getStringExtra("album");
    Bitmap albumArt = intent.getParcelableExtra("album_art");
    String lyrics = intent.getStringExtra("lyrics");
    
    // 保存到 currentMedia
    currentMedia = new MediaInfo(title, artist, album, albumArt, lyrics);
}
```

### 2. 伪装成帆书应用

**为什么要伪装？**
- 某些车机系统只识别特定应用的媒体信息
- 伪装成帆书可以确保车机正确识别和显示
- 用户实际播放的应用信息不变，只是对车机隐藏真实来源

**伪装方式：**
```java
// 在 NowPlayingProvider 中伪装
private Cursor queryNowPlaying(String[] projection) {
    MediaInfo media = getCurrentMedia();
    
    MatrixCursor cursor = new MatrixCursor(columns);
    
    if (media != null) {
        Object[] row = new Object[columns.length];
        
        for (int i = 0; i < columns.length; i++) {
            String col = columns[i];
            switch (col) {
                case COLUMN_PACKAGE_NAME:
                    // 伪装成帆书
                    row[i] = "cn.fanbook.android";
                    break;
                case COLUMN_APP_NAME:
                    // 伪装成帆书
                    row[i] = "帆书";
                    break;
                case COLUMN_TITLE:
                    // 真实的歌曲标题
                    row[i] = media.title;
                    break;
                case COLUMN_ARTIST:
                    // 真实的艺术家
                    row[i] = media.artist;
                    break;
                // ... 其他字段
            }
        }
        
        cursor.addRow(row);
    }
    
    return cursor;
}
```

### 3. 接收车机控制命令

**接收的命令来源：**
- 方控按键 (方向盘上的按键)
- 仪表盘控制 (仪表盘屏幕上的按钮)
- 中控屏控制 (中控屏幕上的按钮)

**接收的命令类型：**
```
播放/暂停
下一曲
上一曲
音量调节
应用切换
```

**实现方式：**
```java
// 接收车机系统的广播
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    
    switch (action) {
        case "com.android.car.media.PLAY_PAUSE":
            // 播放/暂停
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            break;
        case "com.android.car.media.NEXT":
            // 下一曲
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT);
            break;
        case "com.android.car.media.PREV":
            // 上一曲
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            break;
        // ... 其他命令
    }
}
```

### 4. 转发控制命令给第三方应用

**转发方式：**
```java
// 方式 1: 发送媒体按键事件
private void sendMediaKey(int keyCode) {
    AudioManager audioManager = (AudioManager) getSystemService(AudioManager.class);
    
    KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
    audioManager.dispatchMediaKeyEvent(downEvent);
    
    KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
    audioManager.dispatchMediaKeyEvent(upEvent);
}

// 方式 2: 发送广播
private void sendControlBroadcast(String action) {
    Intent intent = new Intent(action);
    intent.setPackage(currentMediaPackage);
    sendBroadcast(intent);
}

// 方式 3: 发送 Intent
private void sendControlIntent(String action) {
    Intent intent = new Intent(action);
    intent.setComponent(new ComponentName(currentMediaPackage, currentMediaClass));
    startService(intent);
}
```

---

## 组件职责重新定义

| 组件 | 职责 | 实现状态 |
|------|------|---------|
| **MediaNotificationListener** | 监听第三方应用的媒体广播 | ✅ 已实现 |
| **NowPlayingProvider** | 伪装成帆书，提供媒体信息给车机系统 | ✅ 已实现 |
| **DesktopCardReceiver** | 接收车机系统的控制命令 | ✅ 已实现 |
| **VehicleCoreService** | 后台服务，保持应用运行 | ✅ 已实现 |
| **MainActivity** | 显示媒体信息、专辑封面、歌词 | ⚠️ 需要增强 |
| **DBToolAccessibilityService** | 转发控制命令给第三方应用 | ✅ 已实现 |

---

## 支持的第三方应用

**当前支持的媒体应用：**
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

**如何添加新应用：**
1. 在 `MediaNotificationListener.MEDIA_PACKAGES` 中添加包名
2. 测试该应用的广播格式
3. 调整解析逻辑以支持该应用

---

## 车机系统集成

### 方控按键集成
```
方向盘按键
  ↓
车机系统捕获
  ↓
发送广播给 DBTOOL
  ↓
DBTOOL 转发给第三方应用
  ↓
第三方应用执行操作
```

### 仪表盘集成
```
仪表盘屏幕
  ↓
显示 DBTOOL 提供的媒体信息
  ├─ 专辑封面
  ├─ 歌曲标题
  ├─ 艺术家
  └─ 歌词
  ↓
用户点击控制按钮
  ↓
发送广播给 DBTOOL
  ↓
DBTOOL 转发给第三方应用
```

### 中控屏集成
```
中控屏幕
  ↓
显示 DBTOOL 提供的媒体信息
  ↓
用户点击控制按钮
  ↓
发送广播给 DBTOOL
  ↓
DBTOOL 转发给第三方应用
```

---

## 关键设计特点

### 1. 伪装机制
**目的：** 兼容特定车机系统
```
真实应用：网易云音乐 (com.netease.cloudmusic)
  ↓
DBTOOL 伪装
  ↓
车机看到：帆书 (cn.fanbook.android)
  ↓
车机正确识别和显示媒体信息
```

### 2. 广播监听
**目的：** 实时获取媒体信息
```
第三方应用播放音乐
  ↓
发送媒体广播
  ↓
DBTOOL 监听并解析
  ↓
保存到 currentMedia
  ↓
车机系统查询时返回
```

### 3. 命令转发
**目的：** 实现车机控制
```
车机系统发送控制命令
  ↓
DBTOOL 接收
  ↓
转换为媒体按键或广播
  ↓
转发给第三方应用
  ↓
第三方应用执行操作
```

---

## 当前实现的问题

### 1. 无法监听到信息
**原因：**
- MediaNotificationListener 需要在系统设置中启用
- 第三方应用的广播格式可能不同
- 权限不足

**解决方案：**
- 在系统设置中启用通知监听
- 添加更多广播类型的监听
- 确保有足够的权限

### 2. 专辑封面和歌词显示
**当前状态：** 已在代码中支持，但 UI 需要增强
```java
// 已支持的字段
info.hasAlbumArt = true;  // 专辑封面
// 需要添加的字段
info.lyrics = lyrics;      // 歌词
```

**改进方向：**
- 在 MainActivity 中显示专辑封面
- 在 MainActivity 中显示歌词
- 优化 UI 布局以适配大屏幕

### 3. 方控按键响应
**当前状态：** 已实现基础功能
```java
// 已支持的按键
KEYCODE_MEDIA_PLAY_PAUSE
KEYCODE_MEDIA_NEXT
KEYCODE_MEDIA_PREVIOUS
```

**改进方向：**
- 添加音量调节支持
- 添加应用切换支持
- 优化按键响应速度

---

## 与 lynktool.apk 的对比

### 功能对比
| 功能 | lynktool | DBTOOL |
|------|----------|--------|
| 监听第三方应用 | ✅ | ✅ |
| 伪装成帆书 | ✅ | ✅ |
| 方控按键支持 | ✅ | ✅ |
| 仪表盘集成 | ✅ | ⚠️ 需要增强 |
| 专辑封面显示 | ✅ | ⚠️ 需要增强 |
| 歌词显示 | ✅ | ⚠️ 需要增强 |
| UI 设计 | ✅ 高级 | ⚠️ 基础 |
| 性能优化 | ✅ | ⚠️ |

### 改进方向
1. **增强 UI** - 显示专辑封面和歌词
2. **优化性能** - 减少延迟和功耗
3. **完善功能** - 支持更多控制方式
4. **提高稳定性** - 完整的错误处理

---

## 快速诊断

### 检查清单
- [ ] MediaNotificationListener 是否在系统设置中启用？
- [ ] 第三方应用是否正在播放音乐？
- [ ] DBTOOL 是否收到媒体广播？
- [ ] NowPlayingProvider 是否被车机系统查询？
- [ ] 车机系统是否发送控制命令？
- [ ] 第三方应用是否收到控制命令？

### 调试命令
```bash
# 查看日志
adb logcat | grep -E "DBTOOL|MediaNotificationListener"

# 查看广播
adb shell dumpsys notification | grep -A 10 "com.netease.cloudmusic"

# 测试 ContentProvider
adb shell content query --uri content://com.dtool.media/nowplaying

# 发送测试广播
adb shell am broadcast -a android.intent.action.MEDIA_BUTTON \
  --ei keycode 79 \
  -n com.dtool/.receiver.DesktopCardReceiver
```

---

## 总结

DBTOOL 是一个**车机媒体中转站**：

1. **监听** 车机上第三方音乐应用的媒体广播
2. **伪装** 成帆书应用，向车机系统报告媒体信息
3. **接收** 车机方控按键和仪表盘的控制命令
4. **转发** 控制命令给第三方音乐应用

**核心价值：**
- 让车机系统能够识别和控制第三方音乐应用
- 在仪表盘和中控屏上显示媒体信息
- 通过方控按键控制音乐播放

**主要改进方向：**
1. 增强 UI 显示（专辑封面、歌词）
2. 优化性能和稳定性
3. 支持更多第三方应用
4. 完善错误处理和日志
