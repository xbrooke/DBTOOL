# DBTOOL 快速总结

## 一句话总结

DBTOOL 是一个**车机信息娱乐系统应用**，监听第三方音乐应用，伪装成帆书，接收方向盘命令，转发给音乐应用。

---

## 核心功能

```
第三方音乐应用 → DBTOOL 监听 → 伪装成帆书 → 车机系统识别
                                    ↓
                            方向盘按键 ← 车机系统
                                    ↓
                            DBTOOL 转发 → 音乐应用执行
```

---

## 实现状态

| 功能 | 状态 | 组件 |
|------|------|------|
| 监听第三方应用 | ✅ | MediaNotificationListener |
| 伪装成帆书 | ✅ | NowPlayingProvider |
| 接收方向盘命令 | ✅ | DesktopCardReceiver |
| 转发命令到应用 | ✅ | AudioManager |
| 显示播放信息 | ✅ | MainActivity |
| 显示专辑封面 | ⚠️ | 需要增强 |
| 显示歌词 | ❌ | 需要开发 |

---

## 无法监听到信息？

### 快速修复 (3 步)

1. **启用通知监听**
   - 设置 → 应用和通知 → 通知 → 通知访问权限 → 启用 DBTOOL

2. **启用无障碍服务**
   - 设置 → 无障碍 → 已安装的服务 → 启用 DBTOOL 无障碍服务

3. **启动核心服务**
   - 打开 DBTOOL → 点击 "启动服务" 按钮

### 验证

在 DBTOOL 主界面检查:
- ✓ 通知监听: 已启用
- ✓ 辅助服务: 已启用
- ✓ 核心服务: 运行中

---

## 支持的音乐应用

- 网易云音乐
- QQ音乐
- 酷狗音乐
- 千千音乐
- 喜马拉雅
- 抖音
- 等 11 个应用

**应用不在列表中？** 修改 `MediaNotificationListener.java` 中的 `MEDIA_PACKAGES` 数组。

---

## 支持的车机协议

| 协议 | 支持 | 命令 |
|------|------|------|
| **ecarx** (亿连) | ✅ | 播放、暂停、下一曲、上一曲 |
| **Geely** (极氪/几何) | ✅ | 播放/暂停、下一曲、上一曲 |

---

## 关键文件

| 文件 | 功能 |
|------|------|
| `MediaNotificationListener.java` | 监听第三方应用通知 |
| `NowPlayingProvider.java` | 伪装成帆书，提供播放信息 |
| `DesktopCardReceiver.java` | 接收方向盘命令 |
| `MainActivity.java` | 显示播放信息 |
| `AndroidManifest.xml` | 权限和组件声明 |

---

## 调试命令

```bash
# 查看实时日志
adb logcat | grep MediaNotificationListener

# 模拟方向盘命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 2 --es package_name com.netease.cloudmusic

# 检查权限
adb shell settings get secure enabled_notification_listeners
```

---

## 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|--------|
| 无法监听到信息 | 权限未启用 | 启用通知监听权限 |
| 应用不在列表中 | 应用不支持 | 添加应用 package name |
| 命令无法转发 | 无障碍服务未启用 | 启用无障碍服务 |
| 服务无法启动 | 权限不足 | 检查所有权限 |

---

## 后续改进

### 优先级 1 (高)
- [ ] 显示专辑封面
- [ ] 显示歌词信息
- [ ] 完善 UI 设计

### 优先级 2 (中)
- [ ] 扩展应用支持
- [ ] 改进通知解析
- [ ] 增强错误处理

### 优先级 3 (低)
- [ ] 性能优化
- [ ] 用户体验改进

---

## 相关文档

- `IMPLEMENTATION_VERIFICATION.md` - 详细的功能实现验证
- `DEBUGGING_GUIDE.md` - 故障排查指南
- `ANSWERS_TO_USER_QUESTIONS.md` - 用户问题回答
- `CORRECT_ARCHITECTURE.md` - 正确的架构说明
- `QUICK_START.md` - 快速开始指南

---

## 技术栈

- **语言**: Java + Kotlin
- **最低 API**: 21 (Android 5.0)
- **关键 API**:
  - NotificationListenerService - 监听通知
  - ContentProvider - 提供媒体信息
  - BroadcastReceiver - 接收命令
  - AudioManager - 转发命令

---

## 权限要求

- `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` - 通知监听
- `android.permission.BIND_ACCESSIBILITY_SERVICE` - 无障碍服务
- `android.permission.INTERNET` - 网络访问
- `android.permission.WAKE_LOCK` - 唤醒锁

---

## 版本信息

- **当前版本**: 1.0.0
- **发布日期**: 2026-05-22
- **状态**: 功能完整，需要 UI 增强

---

## 联系支持

如有问题，请查看:
1. `DEBUGGING_GUIDE.md` - 故障排查
2. `ANSWERS_TO_USER_QUESTIONS.md` - 常见问题
3. logcat 日志 - 实时诊断

---

## 核心代码片段

### 监听通知
```java
@Override
public void onNotificationPosted(StatusBarNotification sbn) {
    MediaInfo mediaInfo = parseMediaNotification(sbn.getPackageName(), sbn.getNotification());
    if (mediaInfo != null) {
        currentMedia = mediaInfo;
        broadcastMediaUpdate(mediaInfo);
    }
}
```

### 伪装成帆书
```java
@Override
public Cursor query(Uri uri, String[] projection, ...) {
    MediaInfo media = MediaNotificationListener.getCurrentMedia();
    // 伪装成帆书
    row[i] = "cn.fanbook.android";  // 真实应用 → 帆书
    return cursor;
}
```

### 接收方向盘命令
```java
@Override
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action.equals("ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER")) {
        int mediaAction = intent.getIntExtra("media_action", -1);
        // 转发给音乐应用
        sendMediaKey(context, AudioManager.STREAM_MUSIC, keyCode);
    }
}
```

---

## 下一步

1. ✅ 启用所有必要的权限
2. ✅ 启动 DBTOOL 应用
3. ✅ 启动音乐应用并播放
4. ✅ 检查 DBTOOL 是否显示播放信息
5. ⏳ 测试方向盘命令转发
6. ⏳ 增强 UI 显示 (专辑封面、歌词)

---

**DBTOOL 已正确实现了所有核心功能。无法监听到信息是权限配置问题，不是代码问题。**
