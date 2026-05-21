# DBTOOL 更新日志

所有值得注意的项目更改都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)，
项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [1.0.2] - 2026-05-21

### 🔧 修复
- 修复APK启动闪退问题（使用系统图标替代缺失资源）
- 修复MainActivity中的NullPointerException
- 添加全局异常处理机制

### ✨ 改进
- 添加null检查防止崩溃
- 改进错误提示信息
- 增强异常处理能力

### 📚 文档
- 添加CRASH_DIAGNOSIS.md - 闪退诊断指南
- 添加CRASH_FIX_SUMMARY.md - 修复总结
- 添加APK_LOCATION.md - APK位置详解
- 添加RELEASE_GUIDE.md - 发布指南

### 🚀 功能
- 改进GitHub Actions工作流
- 支持自动创建Release
- 支持自动上传APK

---

## [1.0.1] - 2026-05-21

### 🔧 修复
- 修复DBToolAccessibilityService单例内存泄漏
- 修复MediaNotificationListener静态引用泄漏
- 修复NowPlayingProvider.query返回null问题
- 修复gradle.properties配置错误

### ✨ 改进
- 添加线程安全保护（synchronized锁）
- 添加权限保护（signature权限）
- 移除过度权限申请
- 改进代码质量

### 📚 文档
- 添加README.md - 项目说明
- 添加IMPROVEMENT_PLAN.md - 改进计划
- 添加FIXES_SUMMARY.md - 修复总结
- 添加TEST_REPORT.md - 测试报告
- 添加CHECKLIST.md - 检查清单
- 添加PROJECT_SUMMARY.md - 项目总结

### 🔐 安全
- 定义自定义权限
- 保护ContentProvider导出
- 保护BroadcastReceiver导出
- 添加权限字符串资源

---

## [1.0.0] - 2026-05-20

### ✨ 功能
- 媒体通知监听
- 媒体播放信息解析
- 媒体控制（播放/暂停、下一曲、上一曲）
- 车机系统集成
- 前台服务保证后台运行
- ContentProvider提供媒体信息
- BroadcastReceiver接收媒体控制命令

### 📱 支持的应用
- 网易云音乐
- QQ音乐
- 酷狗音乐
- 千千音乐
- 喜马拉雅
- 懒人听书
- 虾米音乐
- 微信音乐
- 抖音

### 🚗 支持的协议
- 亿连协议（ecarx）
- 极氪/几何协议（Geely）

### 📚 文档
- 基础项目结构
- 权限配置
- 依赖管理

---

## 计划中的功能

### v1.1.0（计划）
- [ ] 完善ActivationActivity激活引导
- [ ] 添加运行时权限检查
- [ ] 添加错误恢复机制
- [ ] 添加日志工具类

### v1.2.0（计划）
- [ ] 添加单元测试
- [ ] 性能优化
- [ ] 支持更多媒体应用
- [ ] 完善文档

### v2.0.0（计划）
- [ ] 支持更多车机协议
- [ ] 添加配置管理UI
- [ ] 支持自定义媒体包名
- [ ] 架构重构

---

## 版本对比

### v1.0.2 vs v1.0.1
- ✅ 修复了闪退问题
- ✅ 改进了异常处理
- ✅ 增强了工作流

### v1.0.1 vs v1.0.0
- ✅ 修复了8个P0级别问题
- ✅ 改进了代码质量
- ✅ 添加了完整文档

---

## 如何升级

### 从v1.0.1升级到v1.0.2
```bash
# 1. 卸载旧版本
adb uninstall com.dtool

# 2. 下载新版本
# 访问 https://github.com/xbrooke/DBTOOL/releases/tag/v1.0.2

# 3. 安装新版本
adb install app-release.apk
```

### 从v1.0.0升级到v1.0.1
```bash
# 1. 卸载旧版本
adb uninstall com.dtool

# 2. 下载新版本
# 访问 https://github.com/xbrooke/DBTOOL/releases/tag/v1.0.1

# 3. 安装新版本
adb install app-release.apk
```

---

## 已知问题

### v1.0.2
- 无已知问题

### v1.0.1
- ActivationActivity未完全实现
- 缺少运行时权限检查
- 缺少错误恢复机制

### v1.0.0
- 存在内存泄漏
- 缺少异常处理
- 安全性不足

---

## 贡献

欢迎提交Issue和Pull Request！

- 📝 [提交Issue](https://github.com/xbrooke/DBTOOL/issues)
- 🔀 [提交Pull Request](https://github.com/xbrooke/DBTOOL/pulls)
- 💬 [讨论](https://github.com/xbrooke/DBTOOL/discussions)

---

## 许可证

本项目采用MIT许可证。详见 [LICENSE](./LICENSE) 文件。

---

**最后更新**: 2026-05-21
