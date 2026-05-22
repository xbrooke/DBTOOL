package com.dtool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dtool.R;

/**
 * 激活页面
 *
 * 用于引导用户激活NotificationListener和AccessibilityService
 */
public class ActivationActivity extends AppCompatActivity {

    private static final String TAG = "ActivationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        initViews();
    }

    private void initViews() {
        try {
            Button btnNotificationListener = findViewById(R.id.btn_notification_listener);
            Button btnAccessibility = findViewById(R.id.btn_accessibility);
            Button btnContinue = findViewById(R.id.btn_continue);

            if (btnNotificationListener != null) {
                btnNotificationListener.setOnClickListener(v -> openNotificationListenerSettings());
            }

            if (btnAccessibility != null) {
                btnAccessibility.setOnClickListener(v -> openAccessibilitySettings());
            }

            if (btnContinue != null) {
                btnContinue.setOnClickListener(v -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openNotificationListenerSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开通知监听设置失败", e);
            Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAccessibilitySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开无障碍设置失败", e);
            Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }
}
