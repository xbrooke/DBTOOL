package com.dtool.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 激活页面
 *
 * 用于引导用户激活NotificationListener和AccessibilityService
 */
public class ActivationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 实现激活引导逻辑
        finish();
    }
}
