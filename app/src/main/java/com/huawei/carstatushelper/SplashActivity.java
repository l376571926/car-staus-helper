package com.huawei.carstatushelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.test_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String[] permissions = {
                Manifest.permission.BYDAUTO_BODYWORK_COMMON,
                Manifest.permission.BYDAUTO_AC_COMMON
        };
        boolean need = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                need = true;
                break;
            }
        }
        if (need) {
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}