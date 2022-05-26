package com.huawei.carstatushelper.view;

import android.content.Context;
import android.util.AttributeSet;

import com.xw.sample.dashboardviewdemo.DashboardView4;

public class CarSpeedView extends DashboardView4 {
    public CarSpeedView(Context context) {
        this(context, null);
    }

    public CarSpeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int setMax() {
        return 190;
    }

    @Override
    protected int setSection() {
        return 19;
    }

    @Override
    protected int setPortion() {
        return 4;
    }

    @Override
    protected String setHeaderText() {
        return "km/h";
    }
}
