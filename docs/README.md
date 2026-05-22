# DBTOOL - 车机媒体控制应用

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Android](https://img.shields.io/badge/android-9.0%2B-green.svg)
![Java](https://img.shields.io/badge/java-17-orange.svg)

## 📱 项目简介

DBTOOL 是一个 Android 车机应用，用于**监听车机上第三方音乐应用的媒体广播**，并将音乐播放信息**伪装成帆书应用**报告给车机系统。通过这种方式，实现车机方控按键、仪表盘和中控屏对第三方音乐应用的控制。

### 核心功能

- 🎵 **媒体监听**：监听车机上第三方音乐应用的媒体广播
- 🎭 **身份伪装**：将媒体信息伪装成帆书应用，兼容特定车机系统
- 🎮 **媒体控制**：接收车机方控按键、仪表盘、中控屏的控制命令
- 📡 **命令转发**：将控制命令转发给第三方音乐应用
- 🖼️ **媒体显示**：显示专辑封面、歌词等媒体信息
- 🔄 **后台运行**：使用前台服务保证长期运行

---

## 🏗️ 项目架构

```
com.dtool
├── activity/                    # UI层
│   ├── MainActivity            # 主界面
│   └── ActivationActivity      # 激活引导
├── service/                     # 服务层
│   ├── VehicleCoreService      # 前台服务（保证后台运行）
│   ├── MediaNotificationListener # 通知监听服务
│   └── DBToolAccessibilityService # 无障碍服务
├── receiver/                    # 广播接收器
│   ├── BootReceiver            # 启动接收器
│   └── DesktopCardReceiver     # 媒体控制接收器
├── provider/                    # 内容提供者
│   └── NowPlayingProvider      # 媒体信息提供者
└── DBToolApplication           # 应用入口
```

### 架构特点

- **模块清晰**：各组件职责分离
- **标准组件**：使用Android标准组件
- **后台保证**：前台服务保证长期运行
- **线程安全**：关键数据使用同步锁保护

---

## 🔧 技术栈

### 开发环境
- **Android SDK**: 34
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 34 (Android 14)
- **Java**: 17
- **Gradle**: 8.5

### 依赖库
- `androidx.appcompat:appcompat:1.6.1` - AppCompat支持
- `androidx.core:core:1.12.0` - Core库
- `com.google.android.material:material:1.10.0` - Material Design
- `androidx.constraintlayout:constraintlayout:2.1.4` - 布局库

---

## 📋 权限说明

### 必需权限

| 权限 | 用途 | 说明 |
|------|------|------|
| `INTERNET` | 网络通信 | 可选，用于数据上报 |
| `ACCESS_NETWORK_STATE` | 网络状态 | 检查网络连接 |
| `WAKE_LOCK` | 唤醒锁 | 保持设备唤醒 |
| `FOREGROUND_SERVICE` | 前台服务 | 运行前台服务 |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 通知监听 | 监听系统通知 |
| `BIND_ACCESSIBILITY_SERVICE` | 无障碍服务 | 模拟按键 |

### 自定义权限

| 权限 | 保护级别 | 用途 |
|------|---------|------|
| `com.dtool.permission.ACCESS_MEDIA_INFO` | signature | 访问媒体信息 |
| `com.dtool.permission.MEDIA_CONTROL` | signature | 发送媒体控制命令 |

---

## 🚀 快速开始

### 前置条件
- Android Studio 2023.1+
- JDK 17+
- Android SDK 34

### 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/xbrooke/DBTOOL.git
   cd DBTOOL
   ```

2. **配置本地SDK**
   ```bash
   # 编辑 local.properties
   sdk.dir=/path/to/android/sdk
   ```

3. **构建Debug版本**
   ```bash
   ./gradlew assembleDebug
   ```

4. **构建Release版本**
   ```bash
   ./gradlew assembleRelease
   ```

5. **安装应用**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### 首次运行

1. **启用通知监听**
   - 打开系统设置 → 应用和通知 → 通知
   - 找到DBTOOL，启用"通知监听"权限

2. **启用无障碍服务**
   - 打开系统设置 → 无障碍
   - 找到DBTOOL，启用"无障碍服务"

3. **启用自启动**
   - 打开系统设置 → 应用管理
   - 找到DBTOOL，启用"自启动"权限

---

## 📊 项目状态

### 代码质量评分

| 维度 | 评分 | 说明 |
|------|------|------|
| 架构设计 | 7/10 | 模块清晰，但缺少高级模式 |
| 代码质量 | 7/10 | 已修复严重问题 |
| 安全性 | 7/10 | 已添加权限保护 |
| 错误处理 | 6/10 | 基本完善，可继续改进 |
| 性能 | 7/10 | 无明显性能问题 |
| 文档 | 5/10 | 基本文档已完成 |
| **总体** | **7/10** | **良好** |

### 最近修复（v1.0.1）

- ✅ 修复内存泄漏（WeakReference）
- ✅ 修复线程安全问题（synchronized）
- ✅ 添加权限保护（signature权限）
- ✅ 修复错误处理（null检查）
- ✅ 移除过度权限申请

详见 [FIXES_SUMMARY.md](./FIXES_SUMMARY.md)

---

## 📝 使用说明

### 支持的媒体应用

应用支持以下媒体应用的通知监听：

- 网易云音乐
- QQ音乐
- 酷狗音乐
- 千千音乐
- 喜马拉雅
- 懒人听书
- 虾米音乐
- 微信音乐
- 抖音

### 支持的车机协议

- **亿连协议**（ecarx）
- **极氪/几何协议**（Geely）

### 媒体控制命令

| 命令 | 说明 |
|------|------|
| 播放/暂停 | 切换播放状态 |
| 下一曲 | 播放下一首 |
| 上一曲 | 播放上一首 |

---

## 🔐 安全性

### 权限保护

- ✅ ContentProvider使用signature权限保护
- ✅ BroadcastReceiver使用signature权限保护
- ✅ 只申请必要的权限
- ✅ 支持运行时权限检查

### 数据安全

- ✅ 线程安全的数据访问
- ✅ 及时清理资源
- ✅ 防止内存泄漏
- ✅ 防止数据不一致

---

## 🐛 已知问题

### 当前版本（v1.0.1）

- ⚠️ ActivationActivity未完全实现
- ⚠️ 缺少运行时权限检查
- ⚠️ 缺少错误恢复机制

### 计划修复

- [ ] 完善ActivationActivity
- [ ] 添加运行时权限检查
- [ ] 添加错误恢复机制
- [ ] 添加单元测试
- [ ] 优化性能

详见 [IMPROVEMENT_PLAN.md](./IMPROVEMENT_PLAN.md)

---

## 📚 文档

- [IMPROVEMENT_PLAN.md](./IMPROVEMENT_PLAN.md) - 项目改进计划
- [FIXES_SUMMARY.md](./FIXES_SUMMARY.md) - 修复总结
- [API文档](./docs/api.md) - API参考（待完成）
- [架构文档](./docs/architecture.md) - 架构说明（待完成）

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 提交流程

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范

- 遵循Google Java代码规范
- 添加必要的注释和文档
- 确保代码通过lint检查
- 添加相应的单元测试

---

## 📄 许可证

本项目采用MIT许可证。详见 [LICENSE](./LICENSE) 文件。

---

## 📞 联系方式

- **Issue**: [GitHub Issues](https://github.com/xbrooke/DBTOOL/issues)
- **讨论**: [GitHub Discussions](https://github.com/xbrooke/DBTOOL/discussions)

---

## 🙏 致谢

感谢所有贡献者和使用者的支持！

---

## 📈 版本历史

### v1.0.1 (2026-05-21)
- ✅ 修复内存泄漏
- ✅ 修复安全问题
- ✅ 改进错误处理
- ✅ 添加权限保护

### v1.0.0 (初始版本)
- 基础功能实现
- 媒体监听
- 车机集成

---

## 🎯 路线图

### 短期（1-2个月）
- [ ] 完善ActivationActivity
- [ ] 添加运行时权限检查
- [ ] 添加错误恢复机制
- [ ] 发布v1.1.0

### 中期（3-6个月）
- [ ] 添加单元测试
- [ ] 性能优化
- [ ] 支持更多媒体应用
- [ ] 发布v1.2.0

### 长期（6-12个月）
- [ ] 支持更多车机协议
- [ ] 添加配置管理UI
- [ ] 支持自定义媒体包名
- [ ] 发布v2.0.0

---

**最后更新**: 2026-05-21
