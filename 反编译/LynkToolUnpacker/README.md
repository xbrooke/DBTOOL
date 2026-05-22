# LynkTool DEX脱壳器

## 功能说明

这是一个Xposed模块，用于解密360加固保（libjiagu）保护的App的DEX文件。

目标应用：`cn.navitool` (领克车机助手)

## 使用前提

1. **Root设备** - 必须有root权限的Android设备
2. **Xposed框架** - 已安装LSPosed或传统Xposed框架
3. **目标应用** - 需要分析的加固App

## 安装步骤

### 方法1: Android Studio编译

1. 使用Android Studio打开项目
2. Sync Gradle
3. Build > Build APK
4. 安装生成的APK

### 方法2: 手动构建

```bash
cd LynkToolUnpacker
./gradlew assembleDebug
```

## 激活模块

1. 打开 LSPosed Manager / Xposed Installer
2. 进入 模块 页面
3. 找到 "LynkTool脱壳器"
4. 勾选启用
5. 在 作用域 中选择:
   - ☑ cn.navitool (目标应用)
   - ☑ 作用于所有版本
6. 重启设备

## 运行

1. 打开目标应用 `cn.navitool`
2. 触发音乐播放功能（让壳代码解密）
3. 查看日志输出

## 查看输出

DEX文件会保存到: `/sdcard/Download/dex_unpacked/`

```bash
adb shell
su
ls -la /sdcard/Download/dex_unpacked/
```

## 技术原理

### Hook关键点

1. **Application.attach()** - 360加固在此时初始化
2. **dalvik.system.DexFile** - DEX加载入口
3. **ClassLoader.loadClass()** - 类加载监控
4. **System.loadLibrary()** - native库加载
5. **Native方法 interface12** - 360核心解密回调

### 360加固流程

```
App启动
  ↓
StubApp.attach()
  ↓
加载 libjiagu.so
  ↓
调用 native 方法解密 DEX
  ↓
通过 DexFile.loadDex() 加载解密后的DEX
  ↓
真正的业务代码开始执行
```

### Hook策略

本模块在上述流程的关键点进行拦截，尝试获取解密后的DEX数据。

## 注意事项

1. **部分加固版本可能失效** - 360加固会持续更新，可能需要调整Hook点
2. **需要Android 8.0+** - 模块最低支持API 26
3. **输出目录需要SD卡权限** - Android 10+可能需要特殊处理

## 常见问题

### Q: 模块激活后没有输出
A: 检查LSPosed作用域是否正确选择了目标应用，尝试重启设备

### Q: 提示"无权限"
A: 确保设备已root，并授予LSPosed管理器超级用户权限

### Q: 360加固版本太新无法脱壳
A: 可以尝试更新Hook策略，或使用专门的脱壳工具如「佑民脱壳工具」

## 参考资料

- [LSPosed文档](https://github.com/LSPosed/LSPosed)
- [Xposed API文档](https://api.lsposed.org/)
- [360加固保分析](https://github.com/2Eagle/jiagu)
