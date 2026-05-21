# DBTOOL发布指南

## 📦 APK文件位置

### 本地构建
编译后的APK文件位置：
```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk          # Debug版本
└── release/
    └── app-release.apk        # Release版本
```

### GitHub Actions构建
自动构建的APK文件可在以下位置获取：

1. **Actions Artifacts**（临时存储）
   - 访问: https://github.com/xbrooke/DBTOOL/actions
   - 点击最新的工作流运行
   - 在"Artifacts"部分下载APK

2. **GitHub Releases**（正式发布）
   - 访问: https://github.com/xbrooke/DBTOOL/releases
   - 下载对应版本的APK

---

## 🚀 发布流程

### 方式1：自动发布到Releases（推荐）

#### 步骤1：更新版本号
编辑 `app/build.gradle`：
```gradle
android {
    defaultConfig {
        versionCode 2          # 递增
        versionName "1.0.2"    # 更新版本号
    }
}
```

#### 步骤2：更新CHANGELOG
编辑 `CHANGELOG.md`（如果存在）或在提交信息中说明：
```markdown
## v1.0.2 (2026-05-21)
- 修复内存泄漏
- 改进安全性
- 完善文档
```

#### 步骤3：创建Git标签
```bash
# 提交更改
git add app/build.gradle CHANGELOG.md
git commit -m "chore: bump version to 1.0.2"

# 创建标签（触发自动发布）
git tag -a v1.0.2 -m "Release version 1.0.2"

# 推送标签到远程
git push origin v1.0.2
```

#### 步骤4：等待自动构建
- GitHub Actions会自动触发构建
- 构建完成后自动创建Release
- APK文件会自动上传到Release

#### 步骤5：验证发布
- 访问 https://github.com/xbrooke/DBTOOL/releases
- 检查最新版本是否已发布
- 下载APK文件验证

---

### 方式2：手动发布到Releases

#### 步骤1：本地编译
```bash
# 编译Release版本
./gradlew.bat assembleRelease

# 编译Debug版本（可选）
./gradlew.bat assembleDebug
```

#### 步骤2：创建Release
访问 https://github.com/xbrooke/DBTOOL/releases，点击"Create a new release"

#### 步骤3：填写发布信息
- **Tag version**: v1.0.2
- **Release title**: Release v1.0.2
- **Description**: 填写更新说明

#### 步骤4：上传APK文件
- 点击"Attach binaries"
- 选择本地编译的APK文件
- 上传 `app-release.apk` 和 `app-debug.apk`

#### 步骤5：发布
- 点击"Publish release"
- 等待发布完成

---

## 📋 发布检查清单

发布前请确保：

### 代码检查
- [ ] 所有代码已提交
- [ ] 没有未跟踪的文件
- [ ] 没有本地修改

### 版本检查
- [ ] 版本号已更新
- [ ] versionCode已递增
- [ ] versionName已更新

### 文档检查
- [ ] README.md已更新
- [ ] CHANGELOG.md已更新
- [ ] 发布说明已准备

### 构建检查
- [ ] 本地编译成功
- [ ] 没有编译警告
- [ ] APK文件已生成

### 测试检查
- [ ] 功能测试已通过
- [ ] 安全测试已通过
- [ ] 性能测试已通过

### 发布检查
- [ ] Git标签已创建
- [ ] 标签已推送到远程
- [ ] GitHub Actions已触发
- [ ] Release已自动创建

---

## 📝 版本管理

### 版本号规则

采用语义化版本（Semantic Versioning）：
```
v主版本.次版本.修订版本
v1.0.2
 │ │ └─ 修订版本（bug修复）
 │ └─── 次版本（新功能）
 └───── 主版本（重大变更）
```

### 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-05-20 | 初始版本 |
| v1.0.1 | 2026-05-21 | 质量改进 |
| v1.0.2 | 待发布 | 功能完善 |

### 发布计划

| 版本 | 计划日期 | 主要内容 |
|------|---------|---------|
| v1.1.0 | 2026-06-21 | 功能完善 |
| v1.2.0 | 2026-07-21 | 性能优化 |
| v2.0.0 | 2026-09-21 | 架构重构 |

---

## 🔧 自动化发布配置

### GitHub Actions工作流

工作流文件：`.github/workflows/build.yml`

**触发条件**：
- ✅ 推送到main/master分支
- ✅ 创建v*标签（自动发布到Releases）
- ✅ Pull Request到main/master分支
- ✅ 手动触发（workflow_dispatch）

**构建步骤**：
1. 检出代码
2. 设置Java 17
3. 设置Gradle
4. 编译Release APK
5. 编译Debug APK
6. 上传到Artifacts
7. 如果是标签，自动创建Release

### 配置说明

```yaml
# 触发条件
on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'              # 匹配v开头的标签
  workflow_dispatch:      # 支持手动触发

# 自动发布到Releases
- name: Create Release
  if: startsWith(github.ref, 'refs/tags/v')  # 只在标签时执行
  uses: softprops/action-gh-release@v1
  with:
    files: |
      app/build/outputs/apk/release/*.apk
      app/build/outputs/apk/debug/*.apk
```

---

## 📥 下载APK

### 从Releases下载

1. 访问 https://github.com/xbrooke/DBTOOL/releases
2. 选择要下载的版本
3. 点击APK文件下载

### 从Actions下载（临时）

1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击最新的工作流运行
3. 在"Artifacts"部分下载APK
4. 注意：Artifacts会在90天后自动删除

---

## 🔐 签名配置

### Release签名

Release版本应该使用签名密钥进行签名。

#### 生成签名密钥

```bash
# 生成密钥库
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias release

# 输入密钥库密码、密钥密码等信息
```

#### 配置签名

编辑 `app/build.gradle`：

```gradle
android {
    signingConfigs {
        release {
            storeFile file('release.keystore')
            storePassword 'your_keystore_password'
            keyAlias 'release'
            keyPassword 'your_key_password'
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**注意**：不要将密钥库文件和密码提交到版本控制系统！

---

## 📊 发布统计

### 下载统计

可在GitHub Release页面查看下载统计：
- 总下载次数
- 版本下载次数
- 文件下载次数

### 发布历史

| 版本 | 发布日期 | 下载次数 | 说明 |
|------|---------|---------|------|
| v1.0.0 | 2026-05-20 | - | 初始版本 |
| v1.0.1 | 2026-05-21 | - | 质量改进 |

---

## 🆘 常见问题

### Q: 如何查看构建日志？
A: 访问 https://github.com/xbrooke/DBTOOL/actions，点击工作流运行查看详细日志。

### Q: 构建失败怎么办？
A: 
1. 查看Actions日志找出错误原因
2. 修复代码或配置
3. 重新推送或手动触发工作流

### Q: 如何手动触发构建？
A: 
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 选择"Build APK"工作流
3. 点击"Run workflow"
4. 选择分支并运行

### Q: APK文件在哪里下载？
A: 
- 正式发布：https://github.com/xbrooke/DBTOOL/releases
- 临时构建：Actions页面的Artifacts

### Q: 如何创建Release？
A: 
1. 创建Git标签：`git tag -a v1.0.2 -m "Release v1.0.2"`
2. 推送标签：`git push origin v1.0.2`
3. GitHub Actions自动创建Release

---

## 📞 发布支持

### 文档
- 📖 [README.md](./README.md) - 项目说明
- 📖 [IMPROVEMENT_PLAN.md](./IMPROVEMENT_PLAN.md) - 改进计划
- 📖 [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md) - 项目总结

### 问题反馈
- 🐛 [GitHub Issues](https://github.com/xbrooke/DBTOOL/issues)
- 💬 [GitHub Discussions](https://github.com/xbrooke/DBTOOL/discussions)

---

## ✅ 快速发布步骤

### 一键发布（推荐）

```bash
# 1. 更新版本号
# 编辑 app/build.gradle，更新 versionCode 和 versionName

# 2. 提交更改
git add app/build.gradle
git commit -m "chore: bump version to 1.0.2"

# 3. 创建标签并推送（自动触发发布）
git tag -a v1.0.2 -m "Release version 1.0.2"
git push origin v1.0.2

# 4. 等待GitHub Actions完成
# 5. 访问 https://github.com/xbrooke/DBTOOL/releases 查看发布
```

---

**最后更新**: 2026-05-21  
**发布指南版本**: v1.0
