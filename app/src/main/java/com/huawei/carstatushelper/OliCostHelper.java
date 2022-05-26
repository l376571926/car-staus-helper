package com.huawei.carstatushelper;

import android.content.Context;
import android.hardware.bydauto.statistic.AbsBYDAutoStatisticListener;
import android.hardware.bydauto.statistic.BYDAutoStatisticDevice;

import java.util.Timer;
import java.util.TimerTask;

public class OliCostHelper {
    private final BYDAutoStatisticDevice statisticDevice;
    private double lastFuelValue;
    private double lastElecValue;
    private long lastFuelUpdate;
    private long lastElecUpdate;

    public OliCostHelper(Context context) {
        statisticDevice = BYDAutoStatisticDevice.getInstance(context);
        lastFuelValue = statisticDevice.getTotalFuelConValue();
        lastElecValue = statisticDevice.getTotalElecConValue();
//        init();
    }

    private void init() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int totalMileage = statisticDevice.getTotalMileageValue();
                int evMileage = statisticDevice.getEVMileageValue();
                int hevMileage = totalMileage - evMileage;

                //燃油消耗总量
                double totalFuelConValue = statisticDevice.getTotalFuelConValue();
                //第1秒106.3L，第2秒106.7L，第3秒106.7L，第4秒106.7L，第5秒106.7L，第6秒107.5L


                //电消耗总量
                double totalElecConValue = statisticDevice.getTotalElecConValue();
                //第1秒295.8KWH，第2秒
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void start() {
        statisticDevice.registerListener(absBYDAutoStatisticListener);
    }

    public void stop() {
        statisticDevice.unregisterListener(absBYDAutoStatisticListener);
    }

    private AbsBYDAutoStatisticListener absBYDAutoStatisticListener = new AbsBYDAutoStatisticListener() {

        /**
         * 燃油总消耗量
         * @param value
         */
        @Override
        public void onTotalFuelConChanged(double value) {
            super.onTotalFuelConChanged(value);
            if (System.currentTimeMillis() - lastFuelUpdate > 1000) {
                double current = statisticDevice.getTotalFuelConValue();
                if (listener != null) {
                    listener.onRealFuelCost(current - lastFuelValue);
                    lastFuelValue = current;
                    lastFuelUpdate = System.currentTimeMillis();
                }
            }
        }

        /**
         * 电总消耗量
         * @param value
         */
        @Override
        public void onTotalElecConChanged(double value) {
            super.onTotalElecConChanged(value);
            if (System.currentTimeMillis() - lastElecUpdate > 1000) {
                double current = statisticDevice.getTotalElecConValue();
                if (listener != null) {
                    listener.onRealElecCost(current - lastElecValue);
                    lastElecValue = current;
                    lastElecUpdate = System.currentTimeMillis();
                }
            }
        }
    };

    OnRealEnergyCostListener listener;

    public void setListener(OnRealEnergyCostListener listener) {
        this.listener = listener;
    }

    public interface OnRealEnergyCostListener {
        void onRealFuelCost(double value);

        void onRealElecCost(double value);
    }
}
