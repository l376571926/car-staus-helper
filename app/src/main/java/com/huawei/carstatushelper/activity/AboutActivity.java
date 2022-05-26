package com.huawei.carstatushelper.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.huawei.carstatushelper.BuildConfig;
import com.huawei.carstatushelper.R;

public class AboutActivity extends AppCompatActivity {

    private TextView mVersionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mVersionTv = (TextView) findViewById(R.id.version_tv);
        mVersionTv.setText(BuildConfig.VERSION_NAME);
    }
}