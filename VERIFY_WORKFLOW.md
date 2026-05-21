# 验证GitHub Actions工作流

## ✅ 权限配置已完成

你已经配置了方案一（仓库权限），现在工作流应该能够正常创建Release。

---

## 🔍 验证权限配置

### 检查仓库权限设置

1. 访问 https://github.com/xbrooke/DBTOOL/settings/actions
2. 查看 "Workflow permissions" 部分
3. 确认显示：
   - ✅ "Read and write permissions" 已选中
   - ✅ "Allow GitHub Actions to create and approve pull requests" 已勾选

---

## 🚀 测试工作流

### 方式1：创建新版本标签（推荐）

```bash
# 1. 创建新版本标签
git tag -a v1.0.4 -m "Release v1.0.4 - Test workflow"

# 2. 推送标签到远程
git push origin v1.0.4

# 3. 等待工作流完成（5-6分钟）
```

### 方式2：手动触发工作流

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 选择 "Build APK" 工作流
3. 点击 "Run workflow"
4. 选择分支（main）
5. 点击 "Run workflow"

### 方式3：推送到main分支

```bash
# 1. 提交更改
git add .
git commit -m "test: trigger workflow"

# 2. 推送到main
git push origin main

# 3. 等待工作流完成
```

---

## 📊 查看工作流执行

### 实时查看构建进度

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 查看最新的 "Build APK" 工作流
3. 点击工作流查看详细信息

### 工作流步骤

| 步骤 | 说明 | 预期时间 |
|------|------|---------|
| Checkout | 检出代码 | 10秒 |
| Set up JDK | 安装Java 17 | 30秒 |
| Setup Gradle | 配置Gradle | 1分钟 |
| Build Release APK | 编译Release版本 | 2分钟 |
| Build Debug APK | 编译Debug版本 | 1分钟 |
| Upload Artifacts | 上传到Artifacts | 30秒 |
| Create Release | 创建GitHub Release | 30秒 |
| **总计** | | **5-6分钟** |

---

## ✨ 验证Release创建

### 检查Release是否已创建

1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 查看最新版本
3. 确认以下内容：
   - ✅ 版本号正确（如v1.0.4）
   - ✅ 发布说明已生成
   - ✅ APK文件已上传
   - ✅ 包含app-release.apk
   - ✅ 包含app-debug.apk

### Release内容示例

```
# DBTOOL v1.0.4

## 📦 发布信息
- **版本**: v1.0.4
- **发布时间**: 2026-05-21T19:00:00Z
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

## 🐛 如果工作流仍然失败

### 检查工作流日志

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击失败的工作流
3. 点击 "Build" 查看详细日志
4. 搜索 "error" 或 "failed"

### 常见错误和解决方案

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `403 Forbidden` | 权限不足 | 检查仓库权限设置 |
| `Gradle build failed` | 编译错误 | 检查代码是否有语法错误 |
| `File not found` | 文件缺失 | 检查APK输出路径 |
| `Release already exists` | 版本已存在 | 使用新的版本号 |

### 重新运行工作流

如果工作流失败，可以重新运行：

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击失败的工作流
3. 点击 "Re-run jobs"
4. 选择 "Re-run all jobs"

---

## 📥 下载APK

### 从Release下载

1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 选择版本
3. 点击 "app-release.apk" 下载

### 从Artifacts下载（临时）

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击工作流
3. 在 "Artifacts" 部分下载

---

## 🔄 后续工作流运行

### 每次push时

```bash
git add .
git commit -m "message"
git push origin main
```

**结果**:
- ✅ 自动构建
- ✅ 创建预发布Release（Build #123）
- ✅ 上传APK

### 发布正式版本时

```bash
git tag -a v1.0.5 -m "Release v1.0.5"
git push origin v1.0.5
```

**结果**:
- ✅ 自动构建
- ✅ 创建正式Release（v1.0.5）
- ✅ 上传APK
- ✅ 生成发布说明

---

## ✅ 验证清单

### 权限配置
- [x] 访问仓库设置
- [x] 配置Workflow权限为"Read and write"
- [x] 勾选"Allow GitHub Actions to create and approve pull requests"

### 工作流测试
- [ ] 创建新版本标签或手动触发工作流
- [ ] 等待工作流完成（5-6分钟）
- [ ] 查看Actions页面确认构建成功
- [ ] 访问Releases页面确认Release已创建
- [ ] 验证APK文件已上传
- [ ] 下载APK并测试

### 功能验证
- [ ] Release页面显示新版本
- [ ] 发布说明已生成
- [ ] APK文件可下载
- [ ] 应用可正常安装
- [ ] 应用可正常运行

---

## 🎯 下一步

### 立即执行

1. **创建测试版本**
   ```bash
   git tag -a v1.0.4 -m "Release v1.0.4"
   git push origin v1.0.4
   ```

2. **等待工作流完成**
   - 访问 https://github.com/xbrooke/DBTOOL/actions
   - 查看最新工作流运行

3. **验证Release创建**
   - 访问 https://github.com/xbrooke/DBTOOL/releases
   - 检查v1.0.4是否已创建

4. **下载并测试APK**
   ```bash
   adb install app-release.apk
   ```

---

## 📞 获取帮助

### 如果遇到问题

1. **查看工作流日志**
   - 访问Actions页面
   - 查看详细日志

2. **查看相关文档**
   - [WORKFLOW_GUIDE.md](./WORKFLOW_GUIDE.md) - 工作流指南
   - [WORKFLOW_FIX.md](./WORKFLOW_FIX.md) - 修复说明
   - [GITHUB_PERMISSIONS_SETUP.md](./GITHUB_PERMISSIONS_SETUP.md) - 权限配置

3. **提交Issue**
   - 访问 https://github.com/xbrooke/DBTOOL/issues
   - 提供错误日志和截图

---

## 🎉 成功标志

当你看到以下内容时，说明工作流已成功配置：

✅ **Actions页面**
- 工作流显示 "✓ passed"
- 所有步骤都显示绿色勾号

✅ **Releases页面**
- 显示新的Release版本
- 包含app-release.apk和app-debug.apk
- 发布说明已生成

✅ **APK文件**
- 可以下载
- 文件大小正常（通常5-10MB）
- 可以安装到设备

---

**验证日期**: 2026-05-21  
**状态**: ✅ 权限已配置，等待工作流测试  
**下一步**: 创建版本标签触发工作流
