# DBTOOL 快速开始指南

## 🚀 快速获取APK

### 方式1：从GitHub Releases下载（推荐）
```
1. 访问: https://github.com/xbrooke/DBTOOL/releases
2. 选择最新版本
3. 下载 app-release.apk 或 app-debug.apk
4. 使用 adb install 安装
```

### 方式2：从GitHub Actions下载（临时）
```
1. 访问: https://github.com/xbrooke/DBTOOL/actions
2. 点击最新的 "Build APK" 工作流
3. 在 "Artifacts" 部分下载 DBTOOL-release 或 DBTOOL-debug
4. 解压并安装
```

### 方式3：本地编译
```bash
# 需要Java 17和Android SDK
./gradlew.bat assembleRelease    # 编译Release版本
./gradlew.bat assembleDebug      # 编译Debug版本

# APK位置
# app/build/outputs/apk/release/app-release.apk
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 发布新版本

### 一键发布（3步）

#### 步骤1：更新版本号
编辑 `app/build.gradle`：
```gradle
defaultConfig {
    versionCode 2          # 递增
    versionName "1.0.2"    # 更新版本
}
```

#### 步骤2：提交并创建标签
```bash
git add app/build.gradle
git commit -m "chore: bump version to 1.0.2"
git tag -a v1.0.2 -m "Release version 1.0.2"
git push origin v1.0.2
```

#### 步骤3：等待自动发布
- GitHub Actions自动构建
- 自动创建Release
- APK自动上传

**完成！** 访问 https://github.com/xbrooke/DBTOOL/releases 查看

---

## 📥 安装APK

### 使用ADB安装
```bash
# 安装Debug版本
adb install app-debug.apk

# 安装Release版本
adb install app-release.apk

# 卸载应用
adb uninstall com.dtool
```

### 使用Android Studio安装
1. 打开Android Studio
2. 点击 "Run" → "Run 'app'"
3. 选择设备
4. 等待安装完成

---

## ⚙️ 首次运行配置

### 启用必要权限

#### 1. 通知监听权限
```
设置 → 应用和通知 → 通知 → 高级 → 通知监听
找到DBTOOL，启用"通知监听"
```

#### 2. 无障碍服务权限
```
设置 → 无障碍 → 无障碍服务
找到DBTOOL，启用"无障碍服务"
```

#### 3. 自启动权限（可选）
```
设置 → 应用管理 → 权限
找到DBTOOL，启用"自启动"
```

---

## 🧪 测试功能

### 测试媒体监听
1. 打开音乐应用（如网易云）
2. 播放音乐
3. 检查DBTOOL是否能监听到通知

### 测试媒体控制
1. 启用无障碍服务
2. 播放音乐
3. 使用DBTOOL控制播放/暂停、下一曲、上一曲

### 测试后台运行
1. 启动DBTOOL
2. 按Home键返回桌面
3. 打开音乐应用播放
4. 检查DBTOOL是否继续运行

---

## 📊 项目信息

### 当前版本
- **版本**: v1.0.1
- **发布日期**: 2026-05-21
- **状态**: ✅ 稳定版

### 支持的应用
- 网易云音乐
- QQ音乐
- 酷狗音乐
- 千千音乐
- 喜马拉雅
- 懒人听书
- 虾米音乐
- 微信音乐
- 抖音

### 支持的协议
- 亿连协议（ecarx）
- 极氪/几何协议（Geely）

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [README.md](./README.md) | 项目说明和功能介绍 |
| [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) | 详细发布指南 |
| [IMPROVEMENT_PLAN.md](./IMPROVEMENT_PLAN.md) | 改进计划 |
| [FIXES_SUMMARY.md](./FIXES_SUMMARY.md) | 修复总结 |
| [TEST_REPORT.md](./TEST_REPORT.md) | 测试报告 |
| [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md) | 项目总结 |

---

## 🆘 常见问题

### Q: 应用无法启动？
A: 检查是否启用了通知监听和无障碍服务权限

### Q: 无法监听音乐？
A: 确保目标应用在支持列表中，检查通知监听权限

### Q: 媒体控制不工作？
A: 检查无障碍服务是否启用

### Q: 应用后台被杀死？
A: 启用自启动权限，或在电池优化中排除DBTOOL

---

## 🔗 链接

- 📦 [GitHub Releases](https://github.com/xbrooke/DBTOOL/releases)
- 🔨 [GitHub Actions](https://github.com/xbrooke/DBTOOL/actions)
- 📝 [GitHub Issues](https://github.com/xbrooke/DBTOOL/issues)
- 💬 [GitHub Discussions](https://github.com/xbrooke/DBTOOL/discussions)

---

**最后更新**: 2026-05-21
