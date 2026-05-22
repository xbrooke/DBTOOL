# 编译和测试指南

## 🔨 编译步骤

### 方法 1: 使用 build.bat (推荐)

1. 打开文件管理器
2. 进入项目根目录: `c:\Users\Administrator\Desktop\方控\DBTOOL\DBTOOL`
3. 双击 `build.bat` 文件
4. 等待编译完成

**预期输出**:
```
Building DBTOOL project...
...
✅ Build successful!
APK location: app\build\outputs\apk\release\
```

### 方法 2: 使用命令行

```bash
cd c:\Users\Administrator\Desktop\方控\DBTOOL\DBTOOL
.\gradlew.bat build
```

### 方法 3: 使用 Android Studio

1. 打开 Android Studio
2. 打开项目: `c:\Users\Administrator\Desktop\方控\DBTOOL\DBTOOL`
3. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 等待编译完成

---

## 📦 编译输出

### APK 位置

```
app\build\outputs\apk\release\app-release.apk
```

### 文件大小

- 预期: 3-5 MB
- 包含: 所有功能 + MCU 接口支持

### 签名

- 类型: Release 签名
- 密钥库: 项目配置中指定

---

## 📱 安装到设备

### 前置条件

- ✅ 已连接 USB 调试
- ✅ 已启用开发者选项
- ✅ 已安装 ADB

### 安装命令

```bash
# 安装 APK
adb install -r app\build\outputs\apk\release\app-release.apk

# 或使用完整路径
adb install -r "c:\Users\Administrator\Desktop\方控\DBTOOL\DBTOOL\app\build\outputs\apk\release\app-release.apk"
```

### 验证安装

```bash
# 检查应用是否已安装
adb shell pm list packages | grep dtool

# 预期输出
package:com.dtool
```

---

## 🧪 测试步骤

### 步骤 1: 启用权限

1. 打开 DBTOOL 应用
2. 点击 "启用通知监听" 按钮
3. 在系统设置中启用权限
4. 返回应用，检查状态

**预期状态**:
```
通知监听: ✓ 已启用
辅助服务: ✓ 已启用
核心服务: ✓ 运行中
MCU 控制: ✓ 可用
```

### 步骤 2: 启动音乐应用

1. 打开支持的音乐应用 (网易云、QQ音乐等)
2. 播放一首歌曲
3. 返回 DBTOOL，检查是否显示播放信息

**预期显示**:
```
正在播放:
MediaInfo{
  package='com.netease.cloudmusic',
  app='网易云音乐',
  title='歌曲名',
  artist='艺术家',
  album='专辑',
  isPlaying=true
}
```

### 步骤 3: 测试媒体控制

#### 测试播放/暂停

```bash
# 模拟方向盘播放/暂停命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 2 \
  --es package_name com.netease.cloudmusic
```

**预期结果**:
- 音乐应用暂停或播放
- logcat 显示: `MCU command sent: oper_id=27`

#### 测试下一曲

```bash
# 模拟方向盘下一曲命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 3 \
  --es package_name com.netease.cloudmusic
```

**预期结果**:
- 音乐应用切换到下一曲
- logcat 显示: `MCU command sent: oper_id=29`

#### 测试上一曲

```bash
# 模拟方向盘上一曲命令
adb shell am broadcast -a ecarx.intent.broadcast.action.MEDIA_CONTROL_RECEIVER \
  --ei media_action 4 \
  --es package_name com.netease.cloudmusic
```

**预期结果**:
- 音乐应用切换到上一曲
- logcat 显示: `MCU command sent: oper_id=30`

### 步骤 4: 查看日志

```bash
# 实时查看 MCU 相关日志
adb logcat | grep -E "McuControl|DesktopCardReceiver|VehicleCoreService"
```

**预期日志**:
```
D/VehicleCoreService: ✅ MCU 控制接口已初始化
D/DesktopCardReceiver: MCU control initialized successfully
D/McuControlHelper: MCU command sent: oper_id=26, oper_type=1, value1=0x19, value2=0x00, result=true
D/MainActivity: MCU 控制: ✓ 可用
```

---

## ✅ 测试检查清单

### 基础功能

- [ ] 应用可以正常启动
- [ ] 权限可以正常启用
- [ ] 通知监听可以正常工作
- [ ] 无障碍服务可以正常工作

### MCU 功能

- [ ] MCU 控制显示 "✓ 可用"
- [ ] MCU 命令可以正常发送
- [ ] 媒体控制可以正常工作
- [ ] 日志输出正确

### 兼容性

- [ ] 支持网易云音乐
- [ ] 支持 QQ 音乐
- [ ] 支持其他音乐应用
- [ ] 支持多个协议 (ecarx、Geely)

### 性能

- [ ] 应用运行流畅
- [ ] 内存占用正常
- [ ] CPU 占用正常
- [ ] 电池消耗正常

---

## 🐛 常见问题

### Q1: 编译失败，提示 "找不到 gradle"

**解决方案**:
1. 检查 Java 是否已安装
2. 检查 Android SDK 是否已安装
3. 运行 `./gradlew.bat --version` 检查 gradle 版本

### Q2: 安装失败，提示 "INSTALL_FAILED_INVALID_APK"

**解决方案**:
1. 删除旧版本: `adb uninstall com.dtool`
2. 重新编译: `./gradlew.bat clean build`
3. 重新安装: `adb install -r app\build\outputs\apk\release\app-release.apk`

### Q3: MCU 控制显示 "⚠ 不可用"

**解决方案**:
1. 检查车机系统是否支持 MCU 接口
2. 查看 logcat 日志获取详细错误
3. 尝试使用 AudioManager 方式

### Q4: 媒体控制无法工作

**解决方案**:
1. 检查无障碍服务是否启用
2. 检查音乐应用是否支持媒体按键
3. 查看 logcat 日志获取详细错误

### Q5: 应用崩溃

**解决方案**:
1. 查看 logcat 日志获取崩溃信息
2. 检查是否有权限问题
3. 尝试清除应用数据: `adb shell pm clear com.dtool`

---

## 📊 测试报告模板

```
DBTOOL 测试报告
================

测试日期: 2026-05-22
测试人员: [您的名字]
测试设备: [设备型号]
Android 版本: [版本号]

基础功能测试
-----------
应用启动: ✓ / ✗
权限启用: ✓ / ✗
通知监听: ✓ / ✗
无障碍服务: ✓ / ✗

MCU 功能测试
-----------
MCU 初始化: ✓ / ✗
MCU 状态显示: ✓ / ✗
播放/暂停: ✓ / ✗
下一曲: ✓ / ✗
上一曲: ✓ / ✗

兼容性测试
---------
网易云音乐: ✓ / ✗
QQ 音乐: ✓ / ✗
其他应用: ✓ / ✗

性能测试
-------
内存占用: [数值] MB
CPU 占用: [数值] %
电池消耗: [数值] %

问题记录
-------
[记录任何发现的问题]

总体评分
-------
[1-5 分]

备注
---
[任何其他备注]
```

---

## 🚀 发布前检查

在发布新版本前，请确保:

- [ ] 所有测试都通过
- [ ] 没有编译警告
- [ ] 没有运行时错误
- [ ] 性能指标正常
- [ ] 文档已更新
- [ ] 版本号已更新
- [ ] 变更日志已更新

---

## 📞 获取帮助

如有任何问题，请:

1. 查看 `DEBUGGING_GUIDE.md`
2. 查看 `TROUBLESHOOTING.md`
3. 查看 logcat 日志
4. 联系开发团队

---

**编译和测试指南完成**

祝您测试顺利！
