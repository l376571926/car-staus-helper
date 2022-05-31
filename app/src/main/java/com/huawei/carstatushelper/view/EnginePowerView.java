package com.huawei.carstatushelper.view;

import android.content.Context;
import android.util.AttributeSet;

import com.xw.sample.dashboardviewdemo.DashboardView4;

public class EnginePowerView extends DashboardView4 {
    public EnginePowerView(Context context) {
        this(context, null);
    }

    public EnginePowerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EnginePowerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int setMin() {
        return -50;
    }

    @Override
    protected int setMax() {
        return 150;
    }

    @Override
    protected int setSection() {
        return 20;
    }

    @Override
    protected int setPortion() {
        return 2;
    }

    @Override
    protected String setHeaderText() {
        return " kw";
    }
}
