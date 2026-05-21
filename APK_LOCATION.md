# DBTOOL APK 文件位置和发布指南

## 📍 APK文件位置

### 本地编译后的位置
```
DBTOOL/
└── app/
    └── build/
        └── outputs/
            └── apk/
                ├── debug/
                │   └── app-debug.apk          ← Debug版本
                └── release/
                    └── app-release.apk        ← Release版本
```

### GitHub上的位置

#### 1. GitHub Releases（正式发布）
**URL**: https://github.com/xbrooke/DBTOOL/releases

**特点**:
- ✅ 正式发布版本
- ✅ 永久保存
- ✅ 可下载历史版本
- ✅ 包含发布说明

**获取方式**:
```
1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 选择要下载的版本
3. 点击APK文件下载
```

#### 2. GitHub Actions Artifacts（临时构建）
**URL**: https://github.com/xbrooke/DBTOOL/actions

**特点**:
- ⚠️ 临时存储（90天后删除）
- ✅ 每次push都会构建
- ✅ 包含debug和release版本
- ✅ 可用于测试最新代码

**获取方式**:
```
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击最新的 "Build APK" 工作流
3. 在 "Artifacts" 部分下载
4. 解压后使用
```

---

## 🚀 发布流程

### 自动发布到Releases（推荐）

#### 工作原理
当创建以 `v*` 开头的Git标签时，GitHub Actions会自动：
1. 编译Release和Debug版本
2. 创建GitHub Release
3. 上传APK文件到Release

#### 发布步骤

**步骤1：更新版本号**
```bash
# 编辑 app/build.gradle
# 更新 versionCode 和 versionName
```

**步骤2：提交更改**
```bash
git add app/build.gradle
git commit -m "chore: bump version to 1.0.2"
```

**步骤3：创建标签**
```bash
# 创建标签（触发自动发布）
git tag -a v1.0.2 -m "Release version 1.0.2"

# 推送标签到远程（触发GitHub Actions）
git push origin v1.0.2
```

**步骤4：等待自动构建**
- GitHub Actions会自动触发
- 构建通常需要2-5分钟
- 完成后自动创建Release

**步骤5：验证发布**
```
访问 https://github.com/xbrooke/DBTOOL/releases
检查最新版本是否已发布
```

---

## 📦 APK版本说明

### Debug版本 (app-debug.apk)
**用途**: 开发和测试
**特点**:
- ✅ 包含调试信息
- ✅ 可以使用logcat查看日志
- ✅ 文件较大
- ✅ 性能略低

**安装**:
```bash
adb install app-debug.apk
```

### Release版本 (app-release.apk)
**用途**: 正式发布
**特点**:
- ✅ 已优化
- ✅ 文件较小
- ✅ 性能更好
- ✅ 可以签名

**安装**:
```bash
adb install app-release.apk
```

---

## 🔄 GitHub Actions工作流

### 工作流配置
**文件**: `.github/workflows/build.yml`

### 触发条件
```yaml
on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'              # 匹配v开头的标签 → 自动发布
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:      # 手动触发
```

### 构建步骤
1. 检出代码
2. 设置Java 17
3. 设置Gradle
4. 编译Release APK
5. 编译Debug APK
6. 上传到Artifacts
7. 如果是标签，自动创建Release

### 自动发布配置
```yaml
- name: Create Release
  if: startsWith(github.ref, 'refs/tags/v')
  uses: softprops/action-gh-release@v1
  with:
    files: |
      app/build/outputs/apk/release/*.apk
      app/build/outputs/apk/debug/*.apk
```

---

## 📥 下载和安装

### 从Releases下载

**方式1：网页下载**
```
1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 选择版本
3. 点击APK文件下载
4. 使用adb或Android Studio安装
```

**方式2：命令行下载**
```bash
# 下载最新Release
curl -L https://github.com/xbrooke/DBTOOL/releases/download/v1.0.1/app-release.apk -o app-release.apk

# 安装
adb install app-release.apk
```

### 从Actions下载

**方式1：网页下载**
```
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击最新工作流
3. 下载Artifacts
4. 解压并安装
```

### 安装APK

**使用ADB**
```bash
# 安装
adb install app-release.apk

# 卸载
adb uninstall com.dtool

# 查看已安装应用
adb shell pm list packages | grep dtool
```

**使用Android Studio**
```
1. 打开Android Studio
2. 点击 Run → Run 'app'
3. 选择设备
4. 等待安装完成
```

**使用文件管理器**
```
1. 将APK文件复制到设备
2. 打开文件管理器
3. 点击APK文件
4. 按照提示安装
```

---

## 📊 发布历史

### 已发布版本

| 版本 | 发布日期 | 下载链接 | 说明 |
|------|---------|---------|------|
| v1.0.1 | 2026-05-21 | [下载](https://github.com/xbrooke/DBTOOL/releases/tag/v1.0.1) | 质量改进 |
| v1.0.0 | 2026-05-20 | [下载](https://github.com/xbrooke/DBTOOL/releases/tag/v1.0.0) | 初始版本 |

### 计划发布版本

| 版本 | 计划日期 | 主要内容 |
|------|---------|---------|
| v1.1.0 | 2026-06-21 | 功能完善 |
| v1.2.0 | 2026-07-21 | 性能优化 |
| v2.0.0 | 2026-09-21 | 架构重构 |

---

## 🔐 签名和安全

### Release签名

Release版本应该使用签名密钥进行签名。

**生成签名密钥**:
```bash
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release
```

**配置签名** (app/build.gradle):
```gradle
signingConfigs {
    release {
        storeFile file('release.keystore')
        storePassword 'password'
        keyAlias 'release'
        keyPassword 'password'
    }
}

buildTypes {
    release {
        signingConfig signingConfigs.release
    }
}
```

**注意**: 不要将密钥库文件提交到版本控制系统！

---

## 🆘 常见问题

### Q: 如何获取最新的APK？
A: 
1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 下载最新版本的APK
3. 或访问Actions页面下载最新构建

### Q: Release和Debug版本有什么区别？
A:
- **Debug**: 用于开发测试，包含调试信息，文件较大
- **Release**: 用于正式发布，已优化，文件较小

### Q: 如何发布新版本？
A:
1. 更新版本号 (app/build.gradle)
2. 提交更改 (git commit)
3. 创建标签 (git tag -a v1.0.2)
4. 推送标签 (git push origin v1.0.2)
5. GitHub Actions自动发布

### Q: 构建失败怎么办？
A:
1. 查看Actions日志
2. 检查错误信息
3. 修复代码或配置
4. 重新推送或手动触发

### Q: 如何手动触发构建？
A:
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 选择 "Build APK" 工作流
3. 点击 "Run workflow"
4. 选择分支并运行

### Q: Artifacts会保存多久？
A: GitHub Actions Artifacts默认保存90天，之后自动删除。正式版本应该发布到Releases。

---

## 📞 获取帮助

### 文档
- 📖 [README.md](./README.md) - 项目说明
- 📖 [QUICK_START.md](./QUICK_START.md) - 快速开始
- 📖 [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) - 详细发布指南

### 问题反馈
- 🐛 [GitHub Issues](https://github.com/xbrooke/DBTOOL/issues)
- 💬 [GitHub Discussions](https://github.com/xbrooke/DBTOOL/discussions)

### 链接
- 📦 [GitHub Releases](https://github.com/xbrooke/DBTOOL/releases)
- 🔨 [GitHub Actions](https://github.com/xbrooke/DBTOOL/actions)
- 📝 [GitHub Repository](https://github.com/xbrooke/DBTOOL)

---

## ✅ 快速参考

### 获取APK
```bash
# 方式1：从Releases下载
# https://github.com/xbrooke/DBTOOL/releases

# 方式2：从Actions下载
# https://github.com/xbrooke/DBTOOL/actions

# 方式3：本地编译
./gradlew.bat assembleRelease
```

### 发布新版本
```bash
# 1. 更新版本号 (app/build.gradle)
# 2. 提交更改
git add app/build.gradle
git commit -m "chore: bump version to 1.0.2"

# 3. 创建标签并推送（自动发布）
git tag -a v1.0.2 -m "Release version 1.0.2"
git push origin v1.0.2

# 4. 等待GitHub Actions完成
# 5. 访问 https://github.com/xbrooke/DBTOOL/releases 查看
```

### 安装APK
```bash
adb install app-release.apk
```

---

**最后更新**: 2026-05-21  
**文档版本**: v1.0
