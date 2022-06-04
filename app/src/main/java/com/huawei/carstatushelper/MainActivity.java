package com.huawei.carstatushelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.bydauto.ac.AbsBYDAutoAcListener;
import android.hardware.bydauto.ac.BYDAutoAcDevice;
import android.hardware.bydauto.bodywork.BYDAutoBodyworkDevice;
import android.hardware.bydauto.energy.AbsBYDAutoEnergyListener;
import android.hardware.bydauto.energy.BYDAutoEnergyDevice;
import android.hardware.bydauto.engine.AbsBYDAutoEngineListener;
import android.hardware.bydauto.engine.BYDAutoEngineDevice;
import android.hardware.bydauto.gearbox.AbsBYDAutoGearboxListener;
import android.hardware.bydauto.gearbox.BYDAutoGearboxDevice;
import android.hardware.bydauto.speed.AbsBYDAutoSpeedListener;
import android.hardware.bydauto.speed.BYDAutoSpeedDevice;
import android.hardware.bydauto.statistic.AbsBYDAutoStatisticListener;
import android.hardware.bydauto.statistic.BYDAutoStatisticDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huawei.carstatushelper.activity.AboutActivity;
import com.huawei.carstatushelper.databinding.ActivityMainBinding;
import com.socks.library.KLog;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION = 1024;
    private static final DecimalFormat format = new DecimalFormat("###.###");

    private BYDAutoEngineDevice engineDevice;
    private BYDAutoSpeedDevice speedDevice;
    private BYDAutoStatisticDevice statisticDevice;
    private BYDAutoEnergyDevice energyDevice;
    private BYDAutoGearboxDevice gearboxDevice;

    private boolean initSuccess;

    private OliCostHelper oliCostHelper;
    private ActivityMainBinding binding;

    private int totalMileageValue;//总里程

    private String totalFuelConPHM;//累计平均油耗
    private String totalElecConPHM;//累计平均电耗

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        findViewById(R.id.test_btn).setOnClickListener(this);

        String[] permissions = {
                Manifest.permission.BYDAUTO_BODYWORK_COMMON,
//                Manifest.permission.BYDAUTO_AC_COMMON
        };
        boolean need = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                need = true;
                break;
            }
        }
        if (need) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION);
            return;
        }

        initDevice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregister();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean ret = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    ret = false;
                    break;
                }
            }
            if (ret) {
                initDevice();
                register();
            } else {
                finish();
            }
        }
    }

    private void initDevice() {
        statisticDevice = BYDAutoStatisticDevice.getInstance(this);
        speedDevice = BYDAutoSpeedDevice.getInstance(this);
        energyDevice = BYDAutoEnergyDevice.getInstance(this);
        engineDevice = BYDAutoEngineDevice.getInstance(this);
//        bydAutoAcDevice = BYDAutoAcDevice.getInstance(this);
        gearboxDevice = BYDAutoGearboxDevice.getInstance(this);
        oliCostHelper = new OliCostHelper(this);
        oliCostHelper.setListener(onRealEnergyCostListener);
        initSuccess = true;
        initData();

    }

    private void register() {
        if (!initSuccess) {
            return;
        }
        statisticDevice.registerListener(absBYDAutoStatisticListener);
        speedDevice.registerListener(absBYDAutoSpeedListener);
        engineDevice.registerListener(absBYDAutoEngineListener);
        energyDevice.registerListener(absBYDAutoEnergyListener);
//        bydAutoAcDevice.registerListener(absBYDAutoAcListener);
        gearboxDevice.registerListener(absBYDAutoGearboxListener);
        oliCostHelper.start();
    }

    private void unregister() {
        if (!initSuccess) {
            return;
        }
        statisticDevice.unregisterListener(absBYDAutoStatisticListener);
        speedDevice.unregisterListener(absBYDAutoSpeedListener);
        engineDevice.unregisterListener(absBYDAutoEngineListener);
        energyDevice.unregisterListener(absBYDAutoEnergyListener);
//        bydAutoAcDevice.unregisterListener(absBYDAutoAcListener);
        gearboxDevice.unregisterListener(absBYDAutoGearboxListener);
        oliCostHelper.stop();
    }

    private final OliCostHelper.OnRealEnergyCostListener onRealEnergyCostListener = new OliCostHelper.OnRealEnergyCostListener() {
        @Override
        public void onRealFuelCost(double value) {
//            binding.realFuelPbTv.setText(value + "L");
//            binding.realFuelPb.setMax(100);
//            binding.realFuelPb.setProgress(((int) (value * 100)));
        }

        @Override
        public void onRealElecCost(double value) {
//            binding.realElecPbTv.setText(value + "KWH");
//            binding.realElecPb.setMax(100);
//            binding.realElecPb.setProgress(((int) (value * 100)));
        }
    };

    private final AbsBYDAutoGearboxListener absBYDAutoGearboxListener = new AbsBYDAutoGearboxListener() {
        /**
         * 监听自动变速箱档位变化
         * @param level
         */
        @Override
        public void onGearboxAutoModeTypeChanged(int level) {
            super.onGearboxAutoModeTypeChanged(level);
            String gearboxLevelName = getGearboxLevelName(level);
            KLog.e("当前档位：" + level + " " + gearboxLevelName);
            binding.currentGearboxLevelTv.setText(gearboxLevelName);
        }
    };

    private String getGearboxLevelName(int level) {
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_P) {
            return "P";
        }
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_R) {
            return "R";
        }
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_N) {
            return "N";
        }
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_D) {
            return "D";
        }
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_M) {
            return "M";
        }
        if (level == BYDAutoGearboxDevice.GEARBOX_AUTO_MODE_S) {
            return "S";
        }
        return "level:" + level;
    }


    private final AbsBYDAutoAcListener absBYDAutoAcListener = new AbsBYDAutoAcListener() {
        /**
         * 监听空调开启状态
         */
        @Override
        public void onAcStarted() {
            super.onAcStarted();
        }

        /**
         * 监听空调关闭状态
         */
        @Override
        public void onAcStoped() {
            super.onAcStoped();
        }

        /**
         * 监听后空调开启状态
         */
        @Override
        public void onAcRearStarted() {
            super.onAcRearStarted();
        }

        /**
         * 监听后空调关闭状态
         */
        @Override
        public void onAcRearStoped() {
            super.onAcRearStoped();
        }

        /**
         * 监听控制方式的变化
         * @param mode
         */
        @Override
        public void onAcCtrlModeChanged(int mode) {
            super.onAcCtrlModeChanged(mode);
        }

        /**
         * 监听循环方式的变化
         * @param mode
         */
        @Override
        public void onAcCycleModeChanged(int mode) {
            super.onAcCycleModeChanged(mode);
        }

        /**
         * 监听通风功能设置的变化
         * @param state
         */
        @Override
        public void onAcVentilationStateChanged(int state) {
            super.onAcVentilationStateChanged(state);
        }

        /**
         * 监听空调除霜方式的变化
         * @param area
         * @param state
         */
        @Override
        public void onAcDefrostStateChanged(int area, int state) {
            super.onAcDefrostStateChanged(area, state);
        }

        /**
         * 监听A/C（压缩机）手动标志的变化
         * @param sign
         */
        @Override
        public void onAcCompressorManualSignChanged(int sign) {
            super.onAcCompressorManualSignChanged(sign);
        }

        /**
         * 监听A/C（压缩机）状态的变化
         * @param mode
         */
        @Override
        public void onAcCompressorModeChanged(int mode) {
            super.onAcCompressorModeChanged(mode);
        }

        /**
         * 监听出风模式手动标志的变化
         * @param sign
         */
        @Override
        public void onAcWindModeManualSignChanged(int sign) {
            super.onAcWindModeManualSignChanged(sign);
        }

        /**
         * 监听出风模式的变化
         * @param mode
         */
        @Override
        public void onAcWindModeChanged(int mode) {
            super.onAcWindModeChanged(mode);
        }

        /**
         * 监听风量手动标志的变化
         * @param sign
         */
        @Override
        public void onAcWindLevelManualSignChanged(int sign) {
            super.onAcWindLevelManualSignChanged(sign);
        }

        /**
         * 监听风量档位的变化
         * @param level
         */
        @Override
        public void onAcWindLevelChanged(int level) {
            super.onAcWindLevelChanged(level);
        }

        /**
         * 监听温度单位的变化
         * @param unit
         */
        @Override
        public void onTemperatureUnitChanged(int unit) {
            super.onTemperatureUnitChanged(unit);
        }

        /**
         * 监听各区域温度的变化
         * @param area
         * @param value
         */
        @Override
        public void onTemperatureChanged(int area, int value) {
            super.onTemperatureChanged(area, value);
            KLog.e("各区域温度：" + area + " " + value);
            if (area == BYDAutoAcDevice.AC_DEFROST_AREA_FRONT) {//前排空调

            } else if (area == BYDAutoAcDevice.AC_DEFROST_AREA_REAR) {//后排空调

            }
        }

        /**
         * 监听空调出风模式显示状态
         * @param state
         */
        @Override
        public void onAcWindModeShownStateChanged(int state) {
            super.onAcWindModeShownStateChanged(state);
        }
    };

    private final AbsBYDAutoEnergyListener absBYDAutoEnergyListener = new AbsBYDAutoEnergyListener() {
        /**
         * 监听整车工作模式（EV/强制EV/HEV）
         * @param energyMode
         */
        @Override
        public void onEnergyModeChanged(int energyMode) {
            super.onEnergyModeChanged(energyMode);
            String energyModeName = getEnergyModeName(energyMode);
            KLog.e("当前能耗模式：" + energyMode + " " + energyModeName);
            binding.energyModeTv.setText(energyModeName);
        }

        /**
         * 监听整车运行模式（经济模式，运动模式）
         * @param operationMode
         */
        @Override
        public void onOperationModeChanged(int operationMode) {
            super.onOperationModeChanged(operationMode);
            String operationModeName = getOperationModeName(operationMode);
            KLog.e("当前行车模式：" + operationMode + " " + operationModeName);
            binding.operationModeTv.setText(operationModeName);
        }

        /**
         * 监听原地踩油门发电状态
         * @param mode
         */
        @Override
        public void onPowerGenerationStateChanged(int mode) {
            super.onPowerGenerationStateChanged(mode);
        }

        /**
         * 监听原地踩油门发电功率
         * @param value
         */
        @Override
        public void onPowerGenerationValueChanged(int value) {
            super.onPowerGenerationValueChanged(value);
        }

        /**
         * 监听路面模式（保持在线，普通模式，草地/砂砾地/雪地，泥泞地面/车辙地，沙地）
         * @param type
         */
        @Override
        public void onRoadSurfaceChanged(int type) {
            super.onRoadSurfaceChanged(type);
        }

    };

    private final AbsBYDAutoStatisticListener absBYDAutoStatisticListener = new AbsBYDAutoStatisticListener() {
        /**
         * 监听总里程变化
         * @param totalMileageValue
         */
        @Override
        public void onTotalMileageValueChanged(int totalMileageValue) {
            super.onTotalMileageValueChanged(totalMileageValue);
            KLog.e("总行驶里程：" + totalMileageValue + " km");
            MainActivity.this.totalMileageValue = totalMileageValue;
            binding.totalMileageTv.setText(totalMileageValue + " km");
            binding.totalHevMileageTv.setText((totalMileageValue - statisticDevice.getEVMileageValue()) + "km");
        }

        /**
         * 监听燃油消耗总量变化
         * @param value
         */
        @Override
        public void onTotalFuelConChanged(double value) {
            super.onTotalFuelConChanged(value);
            KLog.e("总燃油消耗：" + value + " L");
            binding.totalFuelCostTv.setText(format.format(value) + "L");
        }

        /**
         * 监听电消耗总量的变化
         * @param value
         */
        @Override
        public void onTotalElecConChanged(double value) {
            super.onTotalElecConChanged(value);
            KLog.e("总电量消耗：" + value + "KW•H");
            binding.totalElecCostTv.setText(format.format(value) + "KW•H");
        }

        /**
         * 监听总行驶时间变化
         * @param value {0,9999.9}h
         */
        @Override
        public void onDrivingTimeChanged(double value) {
            super.onDrivingTimeChanged(value);
            binding.drivingTimeTv.setText(format.format(value) + " h");
        }

        /**
         * 监听最近百公里油耗变化
         * @param value {0,51.1}L/100KM
         */
        @Override
        public void onLastFuelConPHMChanged(double value) {
            super.onLastFuelConPHMChanged(value);
            binding.lastFuelConPhmTv.setText(format.format(value) + "L/100KM");
        }

        /**
         * 监听累计平均油耗变化
         * @param value
         */
        @Override
        public void onTotalFuelConPHMChanged(double value) {
            super.onTotalFuelConPHMChanged(value);
            String s = format.format(value) + "(监听值)";
            totalFuelConPHM = s;
//            binding.totalFuelConPhmTv.setText(s);
        }


        /**
         * 监听最近百公里电耗变化
         * @param value {-99.9,99.9}KWH/100KM
         */
        @Override
        public void onLastElecConPHMChanged(double value) {
            super.onLastElecConPHMChanged(value);
            binding.lastElecConPhmTv.setText(format.format(value) + "KWH/100KM");
        }

        /**
         * 监听累计平均电耗变化
         * @param value
         */
        @Override
        public void onTotalElecConPHMChanged(double value) {
            super.onTotalElecConPHMChanged(value);
            String s = format.format(value) + "(监听值)";
            totalElecConPHM = s;
//            binding.totalElecConPhmTv.setText(s);
        }

        /**
         * 监听电续航里程变化
         * @param value
         */
        @Override
        public void onElecDrivingRangeChanged(int value) {
            super.onElecDrivingRangeChanged(value);
            KLog.e("电量续航里程：" + value + "km");
            updateElecDrivingRange(value);
        }


        /**
         * 监听燃油续航里程变化
         * @param value
         */
        @Override
        public void onFuelDrivingRangeChanged(int value) {
            super.onFuelDrivingRangeChanged(value);
            KLog.e("燃油续航里程：" + value + "km");
            updateFuelDrivingRange(value);
        }

        /**
         * 监听燃油百分比变化
         * @param fuelPercentageValue
         */
        @Override
        public void onFuelPercentageChanged(int fuelPercentageValue) {
            super.onFuelPercentageChanged(fuelPercentageValue);
            KLog.e("燃油续航百分比：" + fuelPercentageValue);
            updateFuelPercent(fuelPercentageValue);
        }

        /**
         * 监听电量百分比变化
         * @param value
         */
        @Override
        public void onElecPercentageChanged(double value) {
            super.onElecPercentageChanged(value);
            updateElecPercent(value);
        }

        /**
         * 监听钥匙电池电量变化
         * @param value
         */
        @Override
        public void onKeyBatteryLevelChanged(int value) {
            super.onKeyBatteryLevelChanged(value);
        }

        /**
         * 监听EV里程的变化
         * @param value
         */
        @Override
        public void onEVMileageValueChanged(int value) {
            super.onEVMileageValueChanged(value);
            binding.totalEvMileageTv.setText(value + "km");
        }
    };

    private final AbsBYDAutoSpeedListener absBYDAutoSpeedListener = new AbsBYDAutoSpeedListener() {
        /**
         * 监听车速变化[0-282]km/h
         * @param currentSpeed
         */
        @Override
        public void onSpeedChanged(double currentSpeed) {
            super.onSpeedChanged(currentSpeed);
//            KLog.e("当前车速：" + currentSpeed);
            binding.carSpeedTv.setText(format.format(currentSpeed) + "km/h");
            binding.carSpeedCsv.setVelocity(((int) currentSpeed));

            //行车模式
            updateEnginePower();
        }

        /**
         * 监听油门深度变化[0-100]%
         * @param value
         */
        @Override
        public void onAccelerateDeepnessChanged(int value) {
            super.onAccelerateDeepnessChanged(value);
        }

        /**
         * 监听制动深度变化[0-100]%
         * @param value
         */
        @Override
        public void onBrakeDeepnessChanged(int value) {
            super.onBrakeDeepnessChanged(value);
        }

    };
    private final AbsBYDAutoEngineListener absBYDAutoEngineListener = new AbsBYDAutoEngineListener() {
        /**
         * 监听发动机转速变化
         * @param engineSpeed
         */
        @Override
        public void onEngineSpeedChanged(int engineSpeed) {
            super.onEngineSpeedChanged(engineSpeed);
//            KLog.e("当前发动机转速: " + engineSpeed);
            if (engineSpeed == 8191) {
                engineSpeed = 0;
            }
            binding.engineSpeedTv.setText(engineSpeed + " rpm");
            binding.engineSpeedEsv.setVelocity(engineSpeed);
        }

        /**
         * 监听冷却液位变化（过低，正常）
         * @param state
         */
        @Override
        public void onEngineCoolantLevelChanged(int state) {
            super.onEngineCoolantLevelChanged(state);
        }

        /**
         * 监听油位信号变化
         * @param value
         */
        @Override
        public void onOilLevelChanged(int value) {
            super.onOilLevelChanged(value);
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about_us) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (item.getItemId() == R.id.version_update) {
            Uri uri = Uri.parse("https://gitee.com/liyiwei1032/car-staus-helper/apks");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.test_btn) {
            initData();
        }
    }

    private void initData() {
        //6.1.2 车架号
        BYDAutoBodyworkDevice bodyworkDevice = BYDAutoBodyworkDevice.getInstance(MainActivity.this);
        //LGXC76C44N0131100,LGXC76C44N0131101
        binding.textTv.setText(bodyworkDevice.getAutoVIN());

        //总里程
        int totalMileageValue = statisticDevice.getTotalMileageValue();
        absBYDAutoStatisticListener.onTotalMileageValueChanged(totalMileageValue);

        //总EV里程
        int evMileageValue = statisticDevice.getEVMileageValue();
        absBYDAutoStatisticListener.onEVMileageValueChanged(evMileageValue);

        //车速
        double currentSpeed = speedDevice.getCurrentSpeed();
        absBYDAutoSpeedListener.onSpeedChanged(currentSpeed);

        //工作模式
        int energyMode = energyDevice.getEnergyMode();
        absBYDAutoEnergyListener.onEnergyModeChanged(energyMode);

        //运行模式
        int operationMode = energyDevice.getOperationMode();
        absBYDAutoEnergyListener.onOperationModeChanged(operationMode);

        updateEnginePower();

        //累计燃油消耗
        double totalFuelConValue = statisticDevice.getTotalFuelConValue();
        absBYDAutoStatisticListener.onTotalFuelConChanged(totalFuelConValue);

        //累计电量消耗
        double totalElecConValue = statisticDevice.getTotalElecConValue();
        absBYDAutoStatisticListener.onTotalElecConChanged(totalElecConValue);

        //总行驶时间
        double drivingTimeValue = statisticDevice.getDrivingTimeValue();
        absBYDAutoStatisticListener.onDrivingTimeChanged(drivingTimeValue);

        //最近50公里电耗
        double lastElecConPHMValue = statisticDevice.getLastElecConPHMValue();
        absBYDAutoStatisticListener.onLastElecConPHMChanged(lastElecConPHMValue);
        //最近50公里油耗
        double lastFuelConPHMValue = statisticDevice.getLastFuelConPHMValue();
        absBYDAutoStatisticListener.onLastFuelConPHMChanged(lastFuelConPHMValue);
        //累计平均电耗
        double totalElecConPHMValue = statisticDevice.getTotalElecConPHMValue();
        absBYDAutoStatisticListener.onTotalElecConPHMChanged(totalElecConPHMValue);
        //累计平均油耗
        double totalFuelConPHMValue = statisticDevice.getTotalFuelConPHMValue();
        absBYDAutoStatisticListener.onTotalFuelConPHMChanged(totalFuelConPHMValue);

        //当前档位
        int gearboxAutoModeType = gearboxDevice.getGearboxAutoModeType();
        absBYDAutoGearboxListener.onGearboxAutoModeTypeChanged(gearboxAutoModeType);

        //剩余电量百分比
        updateElecPercent(statisticDevice.getElecPercentageValue());
        //剩余电续航里程
        updateElecDrivingRange(statisticDevice.getElecDrivingRangeValue());
        //剩余燃油百分比
        updateFuelPercent(statisticDevice.getFuelPercentageValue());
        //剩余燃油续航里程
        updateFuelDrivingRange(statisticDevice.getFuelDrivingRangeValue());
    }

    /**
     * 电量续航里程
     *
     * @param value
     */
    private void updateElecDrivingRange(int value) {
        binding.powerMileageTv.setText(value + "km");
    }

    /**
     * 燃油续航里程
     *
     * @param value
     */
    private void updateFuelDrivingRange(int value) {
        binding.fuelMileageTv.setText(value + "km");
    }

    /**
     * 燃油百分比
     *
     * @param fuelPercentageValue
     */
    private void updateFuelPercent(int fuelPercentageValue) {
        binding.fuelPercentPb.setMax(100);
        binding.fuelPercentPb.setProgress(fuelPercentageValue);
        binding.fuelPbTv.setText(fuelPercentageValue + "%");
    }

    /**
     * 电量百分比
     *
     * @param value
     */
    private void updateElecPercent(double value) {
        double ret;
        if (value <= 1) {
            ret = value * 100;
        } else {
            ret = value;
        }
        KLog.e("电量续航百分比：" + ret);
        binding.elecPercentPb.setMax(100);
        binding.elecPercentPb.setProgress(((int) ret));
        binding.elecPbTv.setText(format.format(ret) + "%");
    }

    private void updateEnginePower() {
        int enginePower = engineDevice.getEnginePower();
        KLog.e("当前功率：" + enginePower + " kw");
        binding.enginePowerTv.setText(enginePower + " kw");
        binding.enginePowerEpv.setVelocity(enginePower);

        updateEnergyCost();
    }

    private void updateEnergyCost() {
        //燃油消耗总量
        double totalFuelConValue = statisticDevice.getTotalFuelConValue();
        //电消耗总量
        double totalElecConValue = statisticDevice.getTotalElecConValue();

        String elec_listener_and_cacu = totalElecConPHM + " " + format.format((totalElecConValue * 100.0f / totalMileageValue)) + "(计算值)";
        String fuel_listener_and_cacu = totalFuelConPHM + " " + format.format((totalFuelConValue * 100.0f / totalMileageValue)) + "(计算值)";
        binding.totalElecConPhmTv.setText(elec_listener_and_cacu);
        binding.totalFuelConPhmTv.setText(fuel_listener_and_cacu);
    }

    private String getEnergyModeName(int energyMode) {
        String modeName;
        switch (energyMode) {
            case BYDAutoEnergyDevice.ENERGY_MODE_STOP:
                modeName = "STOP";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_EV://有效
                modeName = "EV";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_FORCE_EV:
                modeName = "FORCE EV";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_HEV://有效
                modeName = "HEV";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_FUEL:
                modeName = "FUEL";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_KEEP:
                modeName = "KEEP";
                break;
            default:
                modeName = "mode:" + energyMode;
                break;
        }
        return modeName;
    }

    private String getOperationModeName(int operationMode) {
        String modeName;
        switch (operationMode) {
            case BYDAutoEnergyDevice.ENERGY_OPERATION_ECONOMY:
                modeName = "ECO";
                break;
            case BYDAutoEnergyDevice.ENERGY_OPERATION_SPORT:
                modeName = "SPORT";
                break;
            default:
                modeName = "mode:" + operationMode;
                break;
        }
        return modeName;
    }
}