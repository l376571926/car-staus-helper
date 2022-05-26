package com.huawei.carstatushelper.view;

import android.content.Context;
import android.util.AttributeSet;

import com.xw.sample.dashboardviewdemo.DashboardView4;

public class EngineSpeedView extends DashboardView4 {
    public EngineSpeedView(Context context) {
        this(context, null);
    }

    public EngineSpeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EngineSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int setMax() {
        return 6000;
    }

    @Override
    protected int setSection() {
        return 12;
    }

    @Override
    protected int setPortion() {
        return 5;
    }

    @Override
    protected String setHeaderText() {
        return " rpm";
    }
}
