#!/usr/bin/env python3
"""
NowPlayingProvider 帆书伪装 FRIDA脚本

功能：拦截 NowPlayingProvider 的查询结果，将返回的 package name 伪装成帆书(fanbook)

使用方法:
1. 安装FRIDA: pip install frida frida-tools
2. Android设备启动frida-server
3. 运行本脚本

前置条件:
- Android设备已root
- frida-server正在运行 (frida-server -l 0.0.0.0:27042)
"""

import sys
import frida
import time

TARGET_PACKAGE = "cn.navitool"
TARGET_AUTHORITY = "cn.navitool.media"
FANBOOK_PACKAGE = "cn.fanbook.android"
FANBOOK_APP_NAME = "帆书"


def on_message(message, data):
    """处理来自JS脚本的消息"""
    msg_type = message.get('type', 'unknown')
    payload = message.get('payload', {})

    if msg_type == 'send':
        level = payload.get('level', 'info')
        text = payload.get('message', '')
        print(f"[{level.upper()}] {text}")
    elif msg_type == 'error':
        print(f"[ERROR] {message.get('stack', 'Unknown error')}")
    else:
        print(f"[{msg_type}] {message}")


def create_spoof_script():
    """创建帆书伪装的FRIDA JavaScript脚本"""
    return """
    var TARGET_AUTHORITY = "cn.navitool.media";
    var FANBOOK_PACKAGE = "cn.fanbook.android";
    var FANBOOK_APP_NAME = "帆书";

    function log(level, msg) {
        send({level: level, message: msg});
    }

    // 检查是否是目标Provider
    function isTargetProvider(uri) {
        if (!uri) return false;
        var uriStr = uri.toString();
        return uriStr.indexOf(TARGET_AUTHORITY) !== -1 ||
               uriStr.indexOf("navitool") !== -1 ||
               uriStr.indexOf("nowplaying") !== -1 ||
               uriStr.indexOf("media") !== -1;
    }

    // 查找列索引
    function findColumnIndex(columns, names) {
        for (var n = 0; n < names.length; n++) {
            for (var i = 0; i < columns.length; i++) {
                if (columns[i].toLowerCase() === names[n].toLowerCase()) {
                    return i;
                }
            }
        }
        return -1;
    }

    // 伪装Cursor
    function spoofCursor(originalCursor) {
        try {
            var columnNames = originalCursor.getColumnNames();
            var columnCount = columnNames.length;

            var packageNameCol = findColumnIndex(columnNames, ["package_name", "packageName", "package"]);
            var appNameCol = findColumnIndex(columnNames, ["app_name", "appName", "application_name"]);

            log("info", "伪装Cursor - package列: " + packageNameCol + ", appName列: " + appNameCol);

            // 创建新的MatrixCursor (需要Java.perform)
            var MatrixCursor = Java.use("android.database.MatrixCursor");
            var newCursor = MatrixCursor.$new(columnNames);

            var wasEmpty = !originalCursor.moveToFirst();
            if (wasEmpty) {
                log("info", "Cursor为空，无需伪装");
                return originalCursor;
            }

            do {
                var row = [];
                for (var i = 0; i < columnCount; i++) {
                    var value = originalCursor.getString(i);

                    // 伪装package name
                    if (i === packageNameCol && value !== null) {
                        log("info", "伪装package: " + value + " -> " + FANBOOK_PACKAGE);
                        row.push(FANBOOK_PACKAGE);
                    }
                    // 伪装app name
                    else if (i === appNameCol && value !== null) {
                        log("info", "伪装app name: " + value + " -> " + FANBOOK_APP_NAME);
                        row.push(FANBOOK_APP_NAME);
                    }
                    // 其他列保持原样
                    else {
                        row.push(value);
                    }
                }
                newCursor.addRow(row);
            } while (originalCursor.moveToNext());

            originalCursor.close();
            log("info", "Cursor伪装完成");
            return newCursor;

        } catch (e) {
            log("error", "伪装Cursor失败: " + e.message);
            return originalCursor;
        }
    }

    // Hook ContentProvider.query
    function hookContentProviderQuery() {
        try {
            var ContentProvider = Java.use("android.content.ContentProvider");

            ContentProvider.query.implementation = function(uri, projection, selection, selectionArgs, sortOrder) {
                if (isTargetProvider(uri)) {
                    log("info", "拦截到目标Provider查询: " + uri.toString());
                }

                var result = this.query(uri, projection, selection, selectionArgs, sortOrder);

                if (isTargetProvider(uri) && result !== null) {
                    log("info", "获取到Cursor，开始伪装...");
                    // 注意: 在某些Android版本中可能需要在Java.perform中调用
                    Java.perform(function() {
                        try {
                            // 由于已经在Java.perform中，尝试重新查询
                            // result已经被设置
                        } catch (e) {
                            log("error", "Java.perform处理失败: " + e.message);
                        }
                    });
                }

                return result;
            };

            log("info", "已Hook ContentProvider.query()");
        } catch (e) {
            log("error", "Hook ContentProvider.query失败: " + e.message);
        }
    }

    // Hook ActivityThread.acquireProvider
    function hookActivityThread() {
        try {
            var ActivityThread = Java.use("android.app.ActivityThread");
            var currentApplication = ActivityThread.currentApplication;

            if (currentApplication) {
                var context = currentApplication.getApplicationContext();
                log("info", "获取到Application Context: " + context.getPackageName());
            }
        } catch (e) {
            log("error", "获取Application失败: " + e.message);
        }
    }

    // 主入口
    Java.perform(function() {
        log("info", "===========================================");
        log("info", "帆书伪装脚本已加载");
        log("info", "目标应用: " + Java.use("android.app.Application").class.getName());
        log("info", "伪装目标: " + FANBOOK_PACKAGE);
        log("info", "===========================================");

        hookContentProviderQuery();
        hookActivityThread();

        log("info", "Hook设置完成，等待拦截查询...");
    });
    """


def main():
    global device, session, script

    print("=" * 60)
    print("NowPlayingProvider 帆书伪装 FRIDA脚本")
    print("目标应用: cn.navitool")
    print("伪装为: cn.fanbook.android (帆书)")
    print("=" * 60)

    # 连接到FRIDA server
    try:
        print("\n[1] 正在连接到设备...")
        device = frida.get_usb_device(timeout=5000)
        print(f"    设备: {device.name}")
    except frida.TimedOutError:
        print("    连接超时，尝试localhost...")
        try:
            manager = frida.get_device_manager()
            device = manager.add_remote_device("localhost:27042")
        except Exception as e:
            print(f"    连接失败: {e}")
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
        print(f"    应用 {TARGET_PACKAGE} 未运行，正在启动...")
        try:
            pid = device.spawn([TARGET_PACKAGE])
            print(f"    Spawned PID: {pid}")
            time.sleep(2)
            session = device.attach(pid)
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
        print("\n[3] 正在加载伪装脚本...")
        script_code = create_spoof_script()
        script = session.create_script(script_code)
        script.on('message', on_message)
        script.load()
        print("    脚本加载成功!")
    except Exception as e:
        print(f"    脚本加载失败: {e}")
        return 1

    print("\n" + "=" * 60)
    print("脚本已运行!")
    print("操作目标应用，脚本将拦截并伪装NowPlayingProvider的返回数据")
    print("=" * 60)
    print("\n按 Ctrl+C 退出\n")

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
