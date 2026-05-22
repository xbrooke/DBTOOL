package com.dtool.activity;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dtool.R;
import com.dtool.mcu.McuControlHelper;
import com.dtool.service.DBToolAccessibilityService;
import com.dtool.service.MediaNotificationListener;
import com.dtool.service.VehicleCoreService;

/**
 * 主界面
 *
 * 功能：
 * 1. 显示当前状态
 * 2. 激活NotificationListenerService
 * 3. 激活AccessibilityService
 * 4. 显示当前播放信息
 * 5. 显示 MCU 控制状态
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvStatus;
    private TextView tvNowPlaying;
    private Button btnNotificationListener;
    private Button btnAccessibility;
    private Button btnStartService;

    private McuControlHelper mcuControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 MCU 控制
        mcuControl = new McuControlHelper(this);

        initViews();
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void initViews() {
        try {
            tvStatus = findViewById(R.id.tv_status);
            tvNowPlaying = findViewById(R.id.tv_now_playing);
            btnNotificationListener = findViewById(R.id.btn_notification_listener);
            btnAccessibility = findViewById(R.id.btn_accessibility);
            btnStartService = findViewById(R.id.btn_start_service);

            // 添加null检查
            if (btnNotificationListener != null) {
                btnNotificationListener.setOnClickListener(v -> openNotificationListenerSettings());
            }

            if (btnAccessibility != null) {
                btnAccessibility.setOnClickListener(v -> openAccessibilitySettings());
            }

            if (btnStartService != null) {
                btnStartService.setOnClickListener(v -> startCoreService());
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新状态显示
     */
    private void updateStatus() {
        StringBuilder status = new StringBuilder();

        // 检查NotificationListenerService
        boolean nlsEnabled = isNotificationListenerEnabled();
        status.append("通知监听: ").append(nlsEnabled ? "✓ 已启用" : "✗ 未启用").append("\n");

        // 检查AccessibilityService
        boolean asEnabled = isAccessibilityServiceEnabled();
        status.append("辅助服务: ").append(asEnabled ? "✓ 已启用" : "✗ 未启用").append("\n");

        // 检查服务状态
        boolean serviceRunning = isServiceRunning();
        status.append("核心服务: ").append(serviceRunning ? "✓ 运行中" : "✗ 未运行").append("\n");

        // 检查 MCU 控制状态
        boolean mcuAvailable = mcuControl != null && mcuControl.isMcuAvailable();
        status.append("MCU 控制: ").append(mcuAvailable ? "✓ 可用" : "⚠ 不可用").append("\n");

        tvStatus.setText(status.toString());

        // 显示当前播放
        MediaNotificationListener.MediaInfo media = MediaNotificationListener.getCurrentMedia();
        if (media != null) {
            tvNowPlaying.setText("正在播放:\n" + media.toString());
        } else {
            tvNowPlaying.setText("暂无播放\n(请确保已启用通知监听)");
        }
    }

    /**
     * 打开NotificationListener设置
     */
    private void openNotificationListenerSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开通知监听设置失败", e);
            Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开无障碍服务设置
     */
    private void openAccessibilitySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开无障碍设置失败", e);
            Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动核心服务
     */
    private void startCoreService() {
        try {
            Intent intent = new Intent(this, com.dtool.service.VehicleCoreService.class);
            startService(intent);
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "启动服务失败", e);
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查NotificationListener是否已启用
     */
    private boolean isNotificationListenerEnabled() {
        try {
            String flat = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
            );
            if (flat != null) {
                return flat.contains(getPackageName());
            }
        } catch (Exception e) {
            Log.e(TAG, "检查通知监听失败", e);
        }
        return false;
    }

    /**
     * 检查AccessibilityService是否已启用
     */
    private boolean isAccessibilityServiceEnabled() {
        try {
            String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (enabledServices != null) {
                ComponentName component = new ComponentName(this, DBToolAccessibilityService.class);
                return enabledServices.contains(component.flattenToString());
            }
        } catch (Exception e) {
            Log.e(TAG, "检查无障碍服务失败", e);
        }
        return false;
    }

    /**
     * 检查核心服务是否运行
     */
    private boolean isServiceRunning() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (VehicleCoreService.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查服务状态失败", e);
        }
        return false;
    }
}
