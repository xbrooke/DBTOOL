# GitHub Actions工作流指南

## 📋 工作流概述

DBTOOL项目配置了自动化的GitHub Actions工作流，可以自动构建APK并发布到Releases。

**工作流文件**: `.github/workflows/build.yml`

---

## 🚀 工作流功能

### 1. 自动构建（每次push）
- ✅ 自动编译Release和Debug版本
- ✅ 自动上传到Artifacts
- ✅ 自动创建预发布版本

### 2. 自动发布（创建版本标签）
- ✅ 自动创建正式Release
- ✅ 自动上传APK文件
- ✅ 自动生成发布说明

### 3. 手动触发
- ✅ 支持workflow_dispatch手动触发
- ✅ 可以随时手动构建

---

## 📊 工作流触发条件

### 触发事件

| 事件 | 说明 | 行为 |
|------|------|------|
| `push main/master` | 推送到主分支 | 自动构建，创建预发布版本 |
| `push tag v*` | 创建版本标签 | 自动构建，创建正式Release |
| `pull_request` | 提交PR | 自动构建验证 |
| `workflow_dispatch` | 手动触发 | 自动构建 |

### 工作流配置

```yaml
on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'              # 匹配v开头的标签
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:      # 支持手动触发
```

---

## 🔄 工作流步骤

### 构建步骤

1. **检出代码** (Checkout)
   - 获取最新的代码

2. **设置Java** (Set up JDK)
   - 安装Java 17

3. **设置Gradle** (Setup Gradle)
   - 配置Gradle构建工具

4. **编译Release** (Build Release APK)
   - 编译优化版本

5. **编译Debug** (Build Debug APK)
   - 编译调试版本

6. **上传Artifacts** (Upload to Artifacts)
   - 上传到GitHub Actions Artifacts

7. **创建Release** (Create Release)
   - 自动创建GitHub Release
   - 上传APK文件

---

## 📥 获取APK的方式

### 方式1：从Releases下载（推荐）

**正式版本** (创建版本标签时)
```
1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 选择版本（如v1.0.2）
3. 下载APK文件
```

**预发布版本** (每次push时)
```
1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 查看预发布版本（如Build 123）
3. 下载APK文件
```

### 方式2：从Artifacts下载（临时）

```
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击最新的工作流运行
3. 在"Artifacts"部分下载
4. 注意：90天后自动删除
```

---

## 🎯 使用场景

### 场景1：日常开发

**流程**:
```
1. 修改代码
2. git add .
3. git commit -m "message"
4. git push origin main
```

**结果**:
- ✅ 自动构建
- ✅ 创建预发布版本
- ✅ APK上传到Releases

**获取APK**:
- 访问Releases查看预发布版本
- 或访问Actions查看Artifacts

---

### 场景2：发布正式版本

**流程**:
```
1. 更新版本号 (app/build.gradle)
2. 更新CHANGELOG.md
3. git add .
4. git commit -m "chore: bump version to 1.0.3"
5. git tag -a v1.0.3 -m "Release v1.0.3"
6. git push origin main
7. git push origin v1.0.3
```

**结果**:
- ✅ 自动构建
- ✅ 创建正式Release
- ✅ APK上传到Releases
- ✅ 生成发布说明

**获取APK**:
- 访问Releases查看正式版本
- 下载app-release.apk

---

### 场景3：手动触发构建

**流程**:
```
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 选择 "Build APK" 工作流
3. 点击 "Run workflow"
4. 选择分支
5. 点击 "Run workflow"
```

**结果**:
- ✅ 立即开始构建
- ✅ 创建预发布版本
- ✅ APK上传到Releases

---

## 📝 Release说明

### 预发布版本说明

```markdown
自动构建版本 - Build 123

**构建信息**:
- 提交: abc123def456
- 分支: main
- 时间: 2026-05-21T18:45:00Z

**包含文件**:
- app-release.apk (Release版本)
- app-debug.apk (Debug版本)

**安装方法**:
adb install app-release.apk
```

### 正式版本说明

```markdown
# DBTOOL v1.0.3

## 📦 发布信息
- **版本**: v1.0.3
- **发布时间**: 2026-05-21T18:45:00Z
- **提交**: abc123def456

## 📥 下载
- `app-release.apk` - Release版本（推荐）
- `app-debug.apk` - Debug版本

## 🚀 安装
adb install app-release.apk

## 📝 更新日志
查看 CHANGELOG.md 了解详细更新内容
```

---

## 🔧 工作流配置详解

### 自动创建预发布版本

```yaml
- name: Create Release on Push
  if: github.event_name == 'push' && !startsWith(github.ref, 'refs/tags/')
  uses: softprops/action-gh-release@v1
  with:
    tag_name: v${{ github.run_number }}
    name: Build ${{ github.run_number }}
    files: |
      app/build/outputs/apk/release/*.apk
      app/build/outputs/apk/debug/*.apk
    draft: false
    prerelease: true
```

**说明**:
- `if`: 只在push到分支时执行（不是标签）
- `tag_name`: 使用构建号作为标签
- `prerelease: true`: 标记为预发布版本

### 自动创建正式Release

```yaml
- name: Create Release on Tag
  if: startsWith(github.ref, 'refs/tags/v')
  uses: softprops/action-gh-release@v1
  with:
    files: |
      app/build/outputs/apk/release/*.apk
      app/build/outputs/apk/debug/*.apk
    draft: false
    prerelease: false
```

**说明**:
- `if`: 只在创建v*标签时执行
- `prerelease: false`: 标记为正式版本
- 自动使用标签名作为版本号

---

## 📊 工作流状态

### 查看工作流状态

**访问**: https://github.com/xbrooke/DBTOOL/actions

**查看内容**:
- ✅ 工作流运行历史
- ✅ 构建状态（成功/失败）
- ✅ 构建时间
- ✅ 构建日志
- ✅ Artifacts

### 工作流徽章

在README中添加工作流徽章：

```markdown
![Build APK](https://github.com/xbrooke/DBTOOL/workflows/Build%20APK/badge.svg)
```

---

## 🆘 常见问题

### Q: 为什么没有自动创建Release？
A: 
1. 检查是否push到了main/master分支
2. 检查是否创建了版本标签
3. 查看Actions日志查看错误

### Q: 如何查看构建日志？
A:
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击最新的工作流运行
3. 点击"Build"查看详细日志

### Q: 构建失败怎么办？
A:
1. 查看Actions日志找出错误
2. 修复代码或配置
3. 重新push或手动触发

### Q: 如何手动触发构建？
A:
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 选择"Build APK"工作流
3. 点击"Run workflow"
4. 选择分支并运行

### Q: Artifacts会保存多久？
A: GitHub Actions Artifacts默认保存90天，之后自动删除。正式版本应该发布到Releases。

---

## 🚀 快速参考

### 日常开发
```bash
git add .
git commit -m "message"
git push origin main
# 自动构建，创建预发布版本
```

### 发布正式版本
```bash
# 1. 更新版本号和CHANGELOG
# 2. 提交更改
git add app/build.gradle CHANGELOG.md
git commit -m "chore: bump version to 1.0.3"

# 3. 创建标签并推送
git tag -a v1.0.3 -m "Release v1.0.3"
git push origin main
git push origin v1.0.3
# 自动构建，创建正式Release
```

### 手动触发
```
访问 https://github.com/xbrooke/DBTOOL/actions
选择"Build APK"工作流
点击"Run workflow"
```

---

## 📞 获取帮助

### 文档
- 📖 [README.md](./README.md) - 项目说明
- 📖 [CHANGELOG.md](./CHANGELOG.md) - 更新日志
- 📖 [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) - 发布指南

### 问题反馈
- 🐛 [GitHub Issues](https://github.com/xbrooke/DBTOOL/issues)
- 💬 [GitHub Discussions](https://github.com/xbrooke/DBTOOL/discussions)

---

**最后更新**: 2026-05-21  
**工作流版本**: v2.0
