# GitHub Actions工作流权限修复

## 🔴 问题

GitHub Actions工作流在创建Release时失败，错误信息：
```
⚠️ GitHub release failed with status: 403
❌ Too many retries. Aborting...
Error: Too many retries.
```

### 原因
工作流缺少创建Release所需的权限。

---

## ✅ 解决方案

### 修复内容

在工作流文件中添加权限配置：

```yaml
permissions:
  contents: write
  packages: write

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      packages: write
```

### 权限说明

| 权限 | 说明 |
|------|------|
| `contents: write` | 允许创建和修改Release |
| `packages: write` | 允许发布包 |

---

## 🚀 修复后的工作流

### 工作流现在支持

✅ **自动构建**
- 每次push自动编译APK
- 上传到Artifacts

✅ **自动创建预发布版本**
- 每次push到main/master创建预发布Release
- 自动上传APK

✅ **自动创建正式Release**
- 创建版本标签时自动创建正式Release
- 自动上传APK和生成发布说明

---

## 📥 获取APK

### 现在可以从以下位置获取APK

1. **GitHub Releases**
   - 访问: https://github.com/xbrooke/DBTOOL/releases
   - 查看所有版本和预发布版本

2. **GitHub Actions Artifacts**
   - 访问: https://github.com/xbrooke/DBTOOL/actions
   - 查看临时构建的APK

---

## 🔄 重新触发工作流

### 已创建的新版本标签

**v1.0.3** - 包含工作流权限修复

### 工作流现在应该能够

✅ 自动构建APK  
✅ 自动创建Release  
✅ 自动上传APK文件  
✅ 自动生成发布说明  

---

## 📊 工作流状态

### 查看构建进度

访问: https://github.com/xbrooke/DBTOOL/actions

**查看内容**:
- 工作流运行状态
- 构建日志
- Artifacts
- Release创建状态

---

## ✨ 后续步骤

### 1. 等待工作流完成
- 访问Actions页面
- 查看最新工作流运行
- 等待构建完成（5-6分钟）

### 2. 验证Release创建
- 访问 https://github.com/xbrooke/DBTOOL/releases
- 检查v1.0.3是否已创建
- 检查APK文件是否已上传

### 3. 下载APK
- 从Release页面下载app-release.apk
- 或从Actions Artifacts下载

### 4. 安装测试
```bash
adb install app-release.apk
```

---

## 🎯 今后的发布流程

### 发布新版本

```bash
# 1. 更新版本号
# 编辑 app/build.gradle
# 更新 versionCode 和 versionName

# 2. 更新CHANGELOG
# 编辑 CHANGELOG.md

# 3. 提交更改
git add app/build.gradle CHANGELOG.md
git commit -m "chore: bump version to 1.0.4"

# 4. 创建标签
git tag -a v1.0.4 -m "Release v1.0.4"

# 5. 推送
git push origin main
git push origin v1.0.4

# 工作流自动：
# ✅ 构建APK
# ✅ 创建Release
# ✅ 上传APK
# ✅ 生成发布说明
```

---

## 📝 修复提交

**提交信息**: `fix: add permissions to workflow for GitHub release creation`

**修改文件**: `.github/workflows/build.yml`

**修改内容**:
- 添加全局permissions配置
- 添加job级别permissions配置
- 确保工作流有权创建Release

---

## 🔗 相关文档

- [WORKFLOW_GUIDE.md](./WORKFLOW_GUIDE.md) - 工作流完整指南
- [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) - 发布指南
- [CHANGELOG.md](./CHANGELOG.md) - 更新日志

---

## ✅ 验证清单

- [x] 添加工作流权限
- [x] 提交修复
- [x] 创建新版本标签
- [x] 推送标签触发工作流
- [ ] 等待工作流完成
- [ ] 验证Release创建
- [ ] 验证APK上传
- [ ] 下载并测试APK

---

**修复日期**: 2026-05-21  
**修复版本**: v1.0.3  
**状态**: ✅ 已完成
