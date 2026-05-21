# GitHub Actions工作流权限配置指南

## 🔐 权限问题诊断

### 错误信息
```
⚠️ GitHub release failed with status: 403
❌ Too many retries. Aborting...
```

### 原因
GitHub Actions工作流缺少必要的权限来创建Release。

---

## ✅ 解决方案

### 方案1：配置仓库权限（推荐）

#### 步骤1：访问仓库设置
1. 打开 https://github.com/xbrooke/DBTOOL
2. 点击 "Settings" 标签
3. 在左侧菜单找到 "Actions" → "General"

#### 步骤2：配置Workflow权限

**找到 "Workflow permissions" 部分**:

```
☑ Read and write permissions
☑ Allow GitHub Actions to create and approve pull requests
```

**具体步骤**:
1. 选择 "Read and write permissions"
2. 勾选 "Allow GitHub Actions to create and approve pull requests"
3. 点击 "Save"

#### 步骤3：验证配置
- ✅ Workflow permissions: Read and write
- ✅ Allow pull requests: Enabled

---

### 方案2：在工作流文件中配置权限

**文件**: `.github/workflows/build.yml`

```yaml
permissions:
  contents: write        # 允许写入仓库内容（创建Release）
  pull-requests: write   # 允许创建PR
  packages: write        # 允许发布包
```

**详细权限说明**:

| 权限 | 说明 | 用途 |
|------|------|------|
| `contents: write` | 写入仓库内容 | 创建Release、上传文件 |
| `pull-requests: write` | 写入PR | 创建和修改PR |
| `packages: write` | 发布包 | 发布到GitHub Packages |
| `issues: write` | 写入Issue | 创建和修改Issue |

---

## 🔧 完整的权限配置

### 工作流文件配置

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

# 全局权限配置
permissions:
  contents: write
  pull-requests: write
  packages: write

jobs:
  build:
    runs-on: ubuntu-latest
    
    # Job级别权限配置
    permissions:
      contents: write
      pull-requests: write
      packages: write

    steps:
      # ... 其他步骤 ...
      
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: |
            app/build/outputs/apk/release/*.apk
            app/build/outputs/apk/debug/*.apk
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 📋 权限配置检查清单

### 仓库级别权限

- [ ] 访问 Settings → Actions → General
- [ ] 选择 "Read and write permissions"
- [ ] 勾选 "Allow GitHub Actions to create and approve pull requests"
- [ ] 点击 "Save"

### 工作流文件权限

- [ ] 添加全局 `permissions` 配置
- [ ] 添加 Job 级别 `permissions` 配置
- [ ] 包含 `contents: write`
- [ ] 包含 `pull-requests: write`
- [ ] 包含 `packages: write`

### 环境变量

- [ ] 使用 `${{ secrets.GITHUB_TOKEN }}`
- [ ] 不要硬编码token

---

## 🚀 权限配置后的步骤

### 1. 提交工作流更新
```bash
git add .github/workflows/build.yml
git commit -m "fix: add proper permissions to workflow"
git push origin main
```

### 2. 创建新版本标签
```bash
git tag -a v1.0.4 -m "Release v1.0.4"
git push origin v1.0.4
```

### 3. 等待工作流完成
- 访问 https://github.com/xbrooke/DBTOOL/actions
- 查看最新工作流运行
- 等待完成（5-6分钟）

### 4. 验证Release创建
- 访问 https://github.com/xbrooke/DBTOOL/releases
- 检查新版本是否已创建
- 检查APK文件是否已上传

---

## 🔍 权限问题排查

### 如果仍然出现403错误

#### 检查1：仓库权限
```
Settings → Actions → General
→ Workflow permissions
→ 确保选择 "Read and write permissions"
```

#### 检查2：工作流权限
```yaml
permissions:
  contents: write
  pull-requests: write
```

#### 检查3：Token配置
```yaml
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

#### 检查4：工作流日志
1. 访问 https://github.com/xbrooke/DBTOOL/actions
2. 点击失败的工作流
3. 查看详细日志
4. 搜索 "permission" 或 "403"

---

## 📊 权限级别说明

### 最小权限（只读）
```yaml
permissions:
  contents: read
```
- 只能读取仓库内容
- 不能创建Release

### 标准权限（读写）
```yaml
permissions:
  contents: write
```
- 可以创建和修改Release
- 可以上传文件

### 完整权限（推荐）
```yaml
permissions:
  contents: write
  pull-requests: write
  packages: write
```
- 可以创建Release
- 可以创建PR
- 可以发布包

---

## 🎯 GitHub Actions权限详解

### contents权限

| 权限 | 说明 |
|------|------|
| `contents: read` | 只读仓库内容 |
| `contents: write` | 读写仓库内容（创建Release、上传文件） |

### pull-requests权限

| 权限 | 说明 |
|------|------|
| `pull-requests: read` | 只读PR |
| `pull-requests: write` | 读写PR（创建、修改PR） |

### packages权限

| 权限 | 说明 |
|------|------|
| `packages: read` | 只读包 |
| `packages: write` | 读写包（发布包） |

### issues权限

| 权限 | 说明 |
|------|------|
| `issues: read` | 只读Issue |
| `issues: write` | 读写Issue（创建、修改Issue） |

---

## 🔐 安全最佳实践

### 1. 使用最小权限原则
```yaml
# ✅ 推荐：只给需要的权限
permissions:
  contents: write

# ❌ 不推荐：给所有权限
permissions:
  contents: write
  pull-requests: write
  packages: write
  issues: write
```

### 2. 使用GITHUB_TOKEN
```yaml
# ✅ 推荐：使用内置token
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

# ❌ 不推荐：使用个人token
env:
  GITHUB_TOKEN: ghp_xxxxxxxxxxxx
```

### 3. 限制工作流触发
```yaml
# ✅ 推荐：只在特定分支和标签触发
on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'

# ❌ 不推荐：在所有push上触发
on:
  push:
```

---

## 📞 常见问题

### Q: 为什么还是403错误？
A: 
1. 检查仓库权限设置
2. 检查工作流文件权限配置
3. 查看工作流日志找出具体错误
4. 确保使用了 `${{ secrets.GITHUB_TOKEN }}`

### Q: 如何查看当前权限？
A:
1. 访问 Settings → Actions → General
2. 查看 "Workflow permissions" 部分
3. 应该显示 "Read and write permissions"

### Q: 权限配置后需要重新运行吗？
A: 是的，需要：
1. 提交权限配置更改
2. 创建新的版本标签
3. 推送标签触发新的工作流运行

### Q: 可以给特定工作流不同的权限吗？
A: 可以，在Job级别配置：
```yaml
jobs:
  build:
    permissions:
      contents: write
```

---

## ✅ 完整配置示例

### .github/workflows/build.yml

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
    tags:
      - 'v*'
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

# 全局权限
permissions:
  contents: write
  pull-requests: write

jobs:
  build:
    runs-on: ubuntu-latest
    
    # Job级别权限
    permissions:
      contents: write
      pull-requests: write

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2

      - name: Build Release APK
        run: gradle assembleRelease

      - name: Build Debug APK
        run: gradle assembleDebug

      - name: Upload Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: DBTOOL-release
          path: app/build/outputs/apk/release/*.apk

      - name: Create Release
        if: startsWith(github.ref, 'refs/tags/v')
        uses: softprops/action-gh-release@v1
        with:
          files: |
            app/build/outputs/apk/release/*.apk
            app/build/outputs/apk/debug/*.apk
          draft: false
          prerelease: false
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 🚀 快速配置步骤

### 1分钟快速配置

```
1. 访问 https://github.com/xbrooke/DBTOOL/settings/actions
2. 找到 "Workflow permissions"
3. 选择 "Read and write permissions"
4. 勾选 "Allow GitHub Actions to create and approve pull requests"
5. 点击 "Save"
6. 完成！
```

---

## 📚 相关文档

- [WORKFLOW_GUIDE.md](./WORKFLOW_GUIDE.md) - 工作流完整指南
- [WORKFLOW_FIX.md](./WORKFLOW_FIX.md) - 工作流修复说明
- [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) - 发布指南

---

## 🔗 GitHub官方文档

- [GitHub Actions权限](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#permissions)
- [GITHUB_TOKEN](https://docs.github.com/en/actions/security-guides/automatic-token-authentication)
- [创建Release](https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release)

---

**最后更新**: 2026-05-21  
**权限配置版本**: v1.0  
**状态**: ✅ 完成
