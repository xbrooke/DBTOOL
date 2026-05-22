# DBTOOL 通信协议分析

## 当前实现的通信方式

### 1. ContentProvider 查询
**用途：** 车机系统查询媒体信息

```
车机系统
  ↓
query(content://com.dtool.media/nowplaying)
  ↓
NowPlayingProvider.query()
  ↓
返回 Cursor (媒体信息)
```

**数据格式：**
```
package_name: "cn.fanbook.android" (伪装)
app_name: "帆书"
title: "歌曲名称"
artist: "艺术家"
album: "专辑"
state: 1 (播放) 或 2 (暂停)
duration: 播放时长
position: 当前位置
album_art: 专辑封面 URI
```

**问题：**
- ❌ 这是被动查询，不是主动推送
- ❌ 需要车机系统主动查询
- ❌ 实时性差

### 2. 广播接收
**用途：** 接收车机的控制命令

```
车机系统
  ↓
发送广播
  ├─ ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER
  └─ com.geely.mediawidget.ACTION_WIDGET_*
  ↓
DesktopCardReceiver.onReceive()
  ↓
转换为媒体按键
  ↓
发送给媒体应用
```

**支持的命令：**
```
播放/暂停
下一曲
上一曲
音量调节 (可扩展)
```

**问题：**
- ❌ 这是接收命令，不是发送信息
- ❌ 需要车机系统主动发送广播
- ❌ 不支持主动推送媒体信息

---

## 实际的通信流程

### 场景：车机显示手机音乐

**当前实现的问题：**
```
手机播放音乐
  ↓
手机发送通知 (系统内部)
  ↓
??? 如何传输到车机？
  ↓
车机 DBTOOL 接收
  ↓
显示在屏幕上
```

**缺失的部分：** 手机和车机之间的通信方式

---

## 可能的通信方式

### 方式 1：WiFi 网络通信

**实现方式：**
```
手机端
  ├─ 启动 HTTP 服务器
  ├─ 监听媒体通知
  └─ 通过 HTTP 发送媒体信息

车机端
  ├─ 连接到手机 WiFi
  ├─ 定期轮询或接收推送
  └─ 显示媒体信息
```

**优点：**
- ✅ 支持远距离通信
- ✅ 支持多个设备
- ✅ 易于扩展

**缺点：**
- ❌ 需要网络连接
- ❌ 延迟较大
- ❌ 功耗较高

### 方式 2：蓝牙通信

**实现方式：**
```
手机端
  ├─ 启动蓝牙服务
  ├─ 监听媒体通知
  └─ 通过蓝牙发送媒体信息

车机端
  ├─ 连接到手机蓝牙
  ├─ 接收媒体信息
  └─ 显示媒体信息
```

**优点：**
- ✅ 功耗低
- ✅ 距离适中
- ✅ 易于配对

**缺点：**
- ❌ 带宽有限
- ❌ 需要配对
- ❌ 连接不稳定

### 方式 3：USB 通信

**实现方式：**
```
手机端
  ├─ 启动 USB 服务
  ├─ 监听媒体通知
  └─ 通过 USB 发送媒体信息

车机端
  ├─ 连接到手机 USB
  ├─ 接收媒体信息
  └─ 显示媒体信息
```

**优点：**
- ✅ 连接稳定
- ✅ 带宽充足
- ✅ 功耗低

**缺点：**
- ❌ 需要物理连接
- ❌ 不支持无线
- ❌ 灵活性差

### 方式 4：车机系统集成

**实现方式：**
```
手机端
  ├─ 通过车机系统 API
  ├─ 发送媒体信息
  └─ 接收控制命令

车机端
  ├─ 车机系统提供接口
  ├─ 接收媒体信息
  └─ 发送控制命令
```

**优点：**
- ✅ 集成度高
- ✅ 性能最优
- ✅ 用户体验最好

**缺点：**
- ❌ 依赖车机系统
- ❌ 需要系统级权限
- ❌ 难以移植

---

## 当前代码中的通信方式

### 1. NowPlayingProvider - 被动查询

**代码位置：** `app/src/main/java/com/dtool/provider/NowPlayingProvider.java`

```java
@Override
public Cursor query(Uri uri, String[] projection, String selection,
                    String[] selectionArgs, String sortOrder) {
    // 车机系统查询媒体信息
    MediaInfo media = MediaNotificationListener.getCurrentMedia();
    
    // 构建并返回 Cursor
    MatrixCursor cursor = new MatrixCursor(columns);
    // ... 填充数据
    return cursor;
}
```

**问题：**
- 这是被动接口，需要车机系统主动查询
- 没有实时推送机制
- 依赖于 MediaNotificationListener 的数据

### 2. DesktopCardReceiver - 接收命令

**代码位置：** `app/src/main/java/com/dtool/receiver/DesktopCardReceiver.java`

```java
@Override
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    
    // 处理不同的广播
    switch (action) {
        case ECARX_ACTION:
            handleEcarxAction(context, intent);
            break;
        case GEELY_PLAY:
            handlePlayPause(context);
            break;
        // ...
    }
}
```

**问题：**
- 这是接收端，不是发送端
- 只能接收车机的控制命令
- 不能主动发送媒体信息

---

## 需要实现的通信模块

### 1. 媒体信息推送模块

```java
class MediaPushService {
    // 监听媒体信息变化
    // 主动推送给车机
    // 支持多种传输方式
}
```

### 2. 网络通信模块

```java
class NetworkCommunication {
    // WiFi 通信
    // 蓝牙通信
    // USB 通信
}
```

### 3. 协议转换模块

```java
class ProtocolConverter {
    // 转换为亿连协议
    // 转换为极氪协议
    // 支持自定义协议
}
```

### 4. 数据同步模块

```java
class DataSync {
    // 定期轮询
    // 事件驱动
    // 缓存管理
}
```

---

## 推荐的实现方案

### 短期方案（基于当前代码）

**使用 ContentProvider + 轮询：**
```
车机系统
  ↓
定期查询 content://com.dtool.media/nowplaying
  ↓
DBTOOL 返回当前媒体信息
  ↓
车机显示
```

**优点：**
- ✅ 最小改动
- ✅ 利用现有代码
- ✅ 易于实现

**缺点：**
- ❌ 实时性差
- ❌ 功耗高
- ❌ 不够灵活

### 中期方案（添加推送机制）

**使用广播推送：**
```
DBTOOL
  ↓
媒体信息变化
  ↓
发送广播
  ↓
车机系统接收
  ↓
显示媒体信息
```

**优点：**
- ✅ 实时性好
- ✅ 功耗低
- ✅ 灵活性高

**缺点：**
- ❌ 需要修改车机系统
- ❌ 需要定义新的广播
- ❌ 需要权限

### 长期方案（完整的网络通信）

**使用 HTTP/WebSocket：**
```
手机端
  ├─ 启动 HTTP 服务器
  ├─ 监听媒体通知
  └─ 通过 HTTP/WebSocket 推送

车机端
  ├─ 连接到手机
  ├─ 接收媒体信息
  └─ 显示媒体信息
```

**优点：**
- ✅ 完全独立
- ✅ 支持多设备
- ✅ 易于扩展

**缺点：**
- ❌ 需要完整重写
- ❌ 需要网络连接
- ❌ 复杂度高

---

## 关键问题

### Q1: 当前代码如何工作？
**A:** 
1. MediaNotificationListener 监听系统通知（但在车机上不适用）
2. NowPlayingProvider 提供查询接口
3. DesktopCardReceiver 接收控制命令
4. 车机系统需要主动查询和发送命令

### Q2: 为什么无法监听到信息？
**A:** 
1. MediaNotificationListener 需要在系统设置中启用
2. 车机系统可能没有其他应用的通知
3. 通信方式可能不匹配

### Q3: 如何改进？
**A:**
1. 确认实际的通信方式
2. 实现相应的通信模块
3. 测试和优化

---

## 总结

DBTOOL 当前的通信方式是：
- **接收端：** ContentProvider (被动查询)
- **发送端：** 广播接收 (接收命令)

这是一个**被动-被动**的模式，需要车机系统主动查询和发送命令。

为了实现完整的功能，需要：
1. 确认手机和车机的通信方式
2. 实现相应的通信模块
3. 添加主动推送机制
4. 优化性能和稳定性
