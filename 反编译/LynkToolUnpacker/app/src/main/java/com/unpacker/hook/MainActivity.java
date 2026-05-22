package com.unpacker.hook;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.util.Log;

import java.io.File;

/**
 * 模块主界面
 */
public class MainActivity extends Activity {

    private static final String TAG = "JiaguUnpacker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 创建简单的UI
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("360加固DEX解密模块\n\n目标应用: cn.navitool\n输出目录: /sdcard/Download/dex_unpacked/\n\n使用方法:\n1. 在LSPosed中激活本模块\n2. 勾选目标应用 cn.navitool\n3. 重启设备\n4. 运行目标应用\n5. 查看输出的DEX文件");
        title.setTextSize(16);
        layout.addView(title);

        Button btnClear = new Button(this);
        btnClear.setText("清除DEX缓存");
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDexCache();
            }
        });
        layout.addView(btnClear);

        Button btnOpenDir = new Button(this);
        btnOpenDir.setText("打开输出目录");
        btnOpenDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOutputDir();
            }
        });
        layout.addView(btnOpenDir);

        setContentView(layout);

        Log.d(TAG, "模块界面已打开");
    }

    private void clearDexCache() {
        try {
            File dir = new File("/sdcard/Download/dex_unpacked/");
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
            Log.d(TAG, "DEX缓存已清除");
        } catch (Exception e) {
            Log.e(TAG, "清除缓存失败: " + e.getMessage());
        }
    }

    private void openOutputDir() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("file:///sdcard/Download/dex_unpacked/"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开目录失败: " + e.getMessage());
        }
    }
}
