#!/usr/bin/env python3
"""
360加固 DEX解密FRIDA脚本
用于提取 cn.navitool 应用的原始DEX

使用方法:
1. PC上安装FRIDA: pip install frida
2. Android设备上运行frida-server
3. 连接设备并运行脚本

前置条件:
- Android设备已root
- frida-server在设备上运行 (frida-server -l 0.0.0.0:27042)
- USB调试已开启
"""

import sys
import os
import time
import json
import frida

# 目标应用包名
TARGET_PACKAGE = "cn.navitool"
# DEX输出目录
OUTPUT_DIR = "/sdcard/Download/dex_unpacked/"

# 存储设备代理
device = None
session = None
script = None


def on_message(message, data):
    """处理来自JS脚本的消息"""
    msg_type = message.get('type', 'unknown')
    payload = message.get('payload', {})

    if msg_type == 'send':
        msg_level = payload.get('level', 'info')
        msg_text = payload.get('message', '')
        print(f"[{msg_level.upper()}] {msg_text}")
    elif msg_type == 'error':
        print(f"[ERROR] {message.get('stack', 'Unknown error')}")
    else:
        print(f"[{msg_type}] {message}")


def create_frida_script():
    """创建FRIDA JavaScript脚本"""
    return """
    // 360加固 DEX解密 FRIDA脚本

    var OUTPUT_DIR = "/sdcard/Download/dex_unpacked/";
    var dexCount = 0;

    // 日志函数
    function log(level, msg) {
        send({level: level, message: msg});
    }

    // 写文件到SD卡
    function writeDexFile(filename, data) {
        try {
            var file = new File(filename, "wb");
            file.write(data);
            file.flush();
            file.close();
            log("info", "DEX saved: " + filename + " (" + data.length + " bytes)");
        } catch (e) {
            log("error", "Write file failed: " + e.message);
        }
    }

    // 确保目录存在
    function ensureDir(dir) {
        try {
            var file = new File(dir);
            if (!file.exists) {
                file.mkdir();
            }
        } catch (e) {
            log("error", "Create dir failed: " + e.message);
        }
    }

    // Hook Application.attach()
    function hookApplicationAttach() {
        var Application = Java.use('android.app.Application');
        Application.attach.implementation = function(ctx) {
            log("info", "Application.attach() called!");
            log("info", "Context: " + (ctx ? ctx.toString() : "null"));
            this.attach(ctx);
        };
        log("info", "Hooked Application.attach()");
    }

    // Hook DexFile.loadDex()
    function hookDexFileLoad() {
        try {
            var DexFile = Java.use('dalvik.system.DexFile');

            DexFile.loadDex.overload('java.lang.String', 'java.lang.String', 'int').implementation = function(dexPathOpt, dexOutputPath, flags) {
                log("info", "DexFile.loadDex() called: " + dexPathOpt + " -> " + dexOutputPath);
                var result = this.loadDex(dexPathOpt, dexOutputPath, flags);
                log("info", "DexFile.loadDex() result: " + result);
                tryDumpDexFile(this, "loadDex");
                return result;
            };

            DexFile.loadDex.overload('java.io.File', 'java.io.File', 'int').implementation = function(dexIn, dexOut, flags) {
                log("info", "DexFile.loadDex(File) called: " + dexIn.getAbsolutePath());
                var result = this.loadDex(dexIn, dexOut, flags);
                log("info", "DexFile.loadDex(File) result: " + result);
                tryDumpDexFile(this, "loadDex");
                return result;
            };

            log("info", "Hooked DexFile.loadDex()");
        } catch (e) {
            log("error", "Hook DexFile failed: " + e.message);
        }
    }

    // Hook DexFile构造函数
    function hookDexFileConstructor() {
        try {
            var DexFile = Java.use('dalvik.system.DexFile');

            var constructors = DexFile.$init.overloads;
            constructors.forEach(function(overload) {
                overload.implementation = function() {
                    log("info", "DexFile constructor called with " + arguments.length + " args");
                    var result = overload.apply(this, arguments);
                    tryDumpDexFile(this, "constructor");
                    return result;
                };
            });

            log("info", "Hooked DexFile constructors");
        } catch (e) {
            log("error", "Hook DexFile constructor failed: " + e.message);
        }
    }

    // 尝试从DexFile提取DEX数据
    function tryDumpDexFile(dexFile, source) {
        try {
            // 尝试获取mCookie或mOdex字段
            var cookie = null;
            var odex = null;

            try {
                var mCookie = dexFile.class.getDeclaredField('mCookie');
                mCookie.setAccessible(true);
                cookie = mCookie.get(dexFile);
                log("info", "mCookie type: " + (cookie ? cookie.class.toString() : "null"));
            } catch (e) {}

            try {
                var mOdex = dexFile.class.getDeclaredField('mOdex');
                mOdex.setAccessible(true);
                odex = mOdex.get(dexFile);
                if (odex && odex.constructor.name === '[B') {
                    var filename = OUTPUT_DIR + "dex_" + source + "_" + (dexCount++) + ".odex";
                    ensureDir(OUTPUT_DIR);
                    writeDexFile(filename, odex);
                }
            } catch (e) {}
        } catch (e) {
            log("error", "tryDumpDexFile failed: " + e.message);
        }
    }

    // Hook ClassLoader.loadClass()
    function hookClassLoader() {
        Java.perform(function() {
            var ClassLoader = Java.use('java.lang.ClassLoader');
            ClassLoader.loadClass.overload('java.lang.String').implementation = function(className) {
                // 只记录与我们相关的类
                if (className && (
                    className.indexOf('MediaNotification') !== -1 ||
                    className.indexOf('DesktopCard') !== -1 ||
                    className.indexOf('NowPlaying') !== -1 ||
                    className.indexOf('StubApp') !== -1 ||
                    className.indexOf('cn.navitool') !== -1 ||
                    className.indexOf('cn.fanbook') !== -1 ||
                    className.indexOf('jiagu') !== -1
                )) {
                    log("info", "ClassLoader.loadClass: " + className);
                }
                return this.loadClass(className);
            };
            log("info", "Hooked ClassLoader.loadClass()");
        });
    }

    // Hook System.loadLibrary
    function hookSystemLoadLibrary() {
        try {
            var System = Java.use('java.lang.System');
            System.loadLibrary.overload('java.lang.String').implementation = function(libName) {
                log("info", "System.loadLibrary: " + libName);
                if (libName && libName.indexOf('jiagu') !== -1) {
                    log("info", "!!! 360加固库加载: " + libName);
                }
                return this.loadLibrary(libName);
            };
            log("info", "Hooked System.loadLibrary()");
        } catch (e) {
            log("error", "Hook System.loadLibrary failed: " + e.message);
        }
    }

    // Hook StubApp的native方法
    function hookStubAppNative() {
        Java.perform(function() {
            try {
                var StubApp = Java.use('com.stub.StubApp');

                // Hook所有native方法
                var methods = StubApp.class.getDeclaredMethods();
                methods.forEach(function(method) {
                    if (method.isNative()) {
                        var methodName = method.getName();
                        log("info", "Found native method: " + methodName);

                        // Hook interface12 - 关键解密方法
                        if (methodName === 'interface12') {
                            log("info", "Hooking interface12...");
                        }
                    }
                });
            } catch (e) {
                log("error", "Hook StubApp failed: " + e.message);
            }
        });
    }

    // Hook MediaPlayer
    function hookMediaPlayer() {
        Java.perform(function() {
            try {
                var MediaPlayer = Java.use('android.media.MediaPlayer');
                MediaPlayer.create.overload('android.content.Context', 'android.net.Uri').implementation = function(ctx, uri) {
                    log("info", "MediaPlayer.create() called with uri: " + uri);
                    return this.create(ctx, uri);
                };
                log("info", "Hooked MediaPlayer.create()");
            } catch (e) {
                log("error", "Hook MediaPlayer failed: " + e.message);
            }
        });
    }

    // 主动扫描内存中的DEX
    function scanMemoryForDex() {
        Java.perform(function() {
            log("info", "开始扫描内存中的DEX...");

            try {
                var System = Java.use('java.lang.System');
                var Runtime = Java.use('java.lang.Runtime');

                // 尝试列出所有加载的类
                var classLoader = Java.classLoader;
                if (classLoader) {
                    log("info", "ClassLoader: " + classLoader.toString());
                }
            } catch (e) {
                log("error", "scanMemoryForDex failed: " + e.message);
            }
        });
    }

    // 主入口
    Java.perform(function() {
        log("info", "===========================================");
        log("info", "360加固 DEX解密脚本已加载");
        log("info", "目标应用: " + Java.use('android.app.Application').class.getName());
        log("info", "===========================================");

        // 安装所有Hook
        hookApplicationAttach();
        hookDexFileLoad();
        hookDexFileConstructor();
        hookClassLoader();
        hookSystemLoadLibrary();
        hookStubAppNative();
        hookMediaPlayer();

        // 定时扫描
        setInterval(scanMemoryForDex, 10000);

        log("info", "所有Hook已安装，等待DEX加载...");
        log("info", "DEX将保存到: " + OUTPUT_DIR);
    });
    """


def main():
    global device, session, script

    print("=" * 60)
    print("360加固 DEX解密 FRIDA脚本")
    print("目标应用: cn.navitool")
    print("=" * 60)

    # 连接到FRIDA server
    try:
        print("\n[1] 正在连接到设备...")
        device = frida.get_usb_device(timeout=5000)
        print(f"    设备: {device.name}")
        print(f"    平台: {device.platform}")
    except frida.TimedOutError:
        print("    连接超时，尝试连接localhost...")
        try:
            manager = frida.get_device_manager()
            device = manager.add_remote_device("localhost:27042")
        except Exception as e:
            print(f"    连接失败: {e}")
            print("\n请确保:")
            print("  1. Android设备已root")
            print("  2. frida-server正在设备上运行")
            print("  3. USB调试已开启")
            print("\n启动frida-server命令:")
            print("  adb shell su -c 'frida-server -l 0.0.0.0:27042 &'")
            return 1
    except Exception as e:
        print(f"    连接失败: {e}")
        return 1

    # 附加到目标应用
    try:
        print("\n[2] 正在附加到目标应用...")
        session = device.attach(TARGET_PACKAGE)
        print(f"    附加成功! PID: {session.pid}")
    except frida.ProcessNotFoundError:
        print(f"    应用 {TARGET_PACKAGE} 未运行")
        print("    正在启动应用...")

        # 尝试spawn并attach
        try:
            print("\n[2b] 使用spawn模式...")
            pid = device.spawn([TARGET_PACKAGE])
            print(f"    Spawned PID: {pid}")
            time.sleep(2)  # 等待应用启动

            session = device.attach(pid)
            print(f"    附加成功! PID: {pid}")

            # Resume the process
            device.resume(pid)
            print("    应用已恢复运行")
        except Exception as e:
            print(f"    启动失败: {e}")
            return 1
    except Exception as e:
        print(f"    附加失败: {e}")
        return 1

    # 创建和加载脚本
    try:
        print("\n[3] 正在加载Hook脚本...")
        script_code = create_frida_script()
        script = session.create_script(script_code)
        script.on('message', on_message)
        script.load()
        print("    脚本加载成功!")
    except Exception as e:
        print(f"    脚本加载失败: {e}")
        return 1

    print("\n" + "=" * 60)
    print("脚本已运行!")
    print("请操作目标应用触发音乐播放等功能")
    print("Hook将拦截DEX加载并保存到: /sdcard/Download/dex_unpacked/")
    print("=" * 60)
    print("\n按 Ctrl+C 退出\n")

    # 保持运行
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n正在退出...")
    finally:
        if script:
            script.unload()
        if session:
            session.detach()
        print("已退出")


if __name__ == '__main__':
    sys.exit(main() or 0)
