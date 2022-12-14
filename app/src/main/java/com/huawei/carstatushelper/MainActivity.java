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

    private int totalMileageValue;//?????????

    private String totalFuelConPHM;//??????????????????
    private String totalElecConPHM;//??????????????????

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
         * ?????????????????????????????????
         * @param level
         */
        @Override
        public void onGearboxAutoModeTypeChanged(int level) {
            super.onGearboxAutoModeTypeChanged(level);
            String gearboxLevelName = getGearboxLevelName(level);
            KLog.e("???????????????" + level + " " + gearboxLevelName);
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
         * ????????????????????????
         */
        @Override
        public void onAcStarted() {
            super.onAcStarted();
        }

        /**
         * ????????????????????????
         */
        @Override
        public void onAcStoped() {
            super.onAcStoped();
        }

        /**
         * ???????????????????????????
         */
        @Override
        public void onAcRearStarted() {
            super.onAcRearStarted();
        }

        /**
         * ???????????????????????????
         */
        @Override
        public void onAcRearStoped() {
            super.onAcRearStoped();
        }

        /**
         * ???????????????????????????
         * @param mode
         */
        @Override
        public void onAcCtrlModeChanged(int mode) {
            super.onAcCtrlModeChanged(mode);
        }

        /**
         * ???????????????????????????
         * @param mode
         */
        @Override
        public void onAcCycleModeChanged(int mode) {
            super.onAcCycleModeChanged(mode);
        }

        /**
         * ?????????????????????????????????
         * @param state
         */
        @Override
        public void onAcVentilationStateChanged(int state) {
            super.onAcVentilationStateChanged(state);
        }

        /**
         * ?????????????????????????????????
         * @param area
         * @param state
         */
        @Override
        public void onAcDefrostStateChanged(int area, int state) {
            super.onAcDefrostStateChanged(area, state);
        }

        /**
         * ??????A/C????????????????????????????????????
         * @param sign
         */
        @Override
        public void onAcCompressorManualSignChanged(int sign) {
            super.onAcCompressorManualSignChanged(sign);
        }

        /**
         * ??????A/C??????????????????????????????
         * @param mode
         */
        @Override
        public void onAcCompressorModeChanged(int mode) {
            super.onAcCompressorModeChanged(mode);
        }

        /**
         * ???????????????????????????????????????
         * @param sign
         */
        @Override
        public void onAcWindModeManualSignChanged(int sign) {
            super.onAcWindModeManualSignChanged(sign);
        }

        /**
         * ???????????????????????????
         * @param mode
         */
        @Override
        public void onAcWindModeChanged(int mode) {
            super.onAcWindModeChanged(mode);
        }

        /**
         * ?????????????????????????????????
         * @param sign
         */
        @Override
        public void onAcWindLevelManualSignChanged(int sign) {
            super.onAcWindLevelManualSignChanged(sign);
        }

        /**
         * ???????????????????????????
         * @param level
         */
        @Override
        public void onAcWindLevelChanged(int level) {
            super.onAcWindLevelChanged(level);
        }

        /**
         * ???????????????????????????
         * @param unit
         */
        @Override
        public void onTemperatureUnitChanged(int unit) {
            super.onTemperatureUnitChanged(unit);
        }

        /**
         * ??????????????????????????????
         * @param area
         * @param value
         */
        @Override
        public void onTemperatureChanged(int area, int value) {
            super.onTemperatureChanged(area, value);
            KLog.e("??????????????????" + area + " " + value);
            if (area == BYDAutoAcDevice.AC_DEFROST_AREA_FRONT) {//????????????

            } else if (area == BYDAutoAcDevice.AC_DEFROST_AREA_REAR) {//????????????

            }
        }

        /**
         * ????????????????????????????????????
         * @param state
         */
        @Override
        public void onAcWindModeShownStateChanged(int state) {
            super.onAcWindModeShownStateChanged(state);
        }
    };

    private final AbsBYDAutoEnergyListener absBYDAutoEnergyListener = new AbsBYDAutoEnergyListener() {
        /**
         * ???????????????????????????EV/??????EV/HEV???
         * @param energyMode
         */
        @Override
        public void onEnergyModeChanged(int energyMode) {
            super.onEnergyModeChanged(energyMode);
            String energyModeName = getEnergyModeName(energyMode);
            KLog.e("?????????????????????" + energyMode + " " + energyModeName);
            binding.energyModeTv.setText(energyModeName);
        }

        /**
         * ?????????????????????????????????????????????????????????
         * @param operationMode
         */
        @Override
        public void onOperationModeChanged(int operationMode) {
            super.onOperationModeChanged(operationMode);
            String operationModeName = getOperationModeName(operationMode);
            KLog.e("?????????????????????" + operationMode + " " + operationModeName);
            binding.operationModeTv.setText(operationModeName);
        }

        /**
         * ?????????????????????????????????
         * @param mode
         */
        @Override
        public void onPowerGenerationStateChanged(int mode) {
            super.onPowerGenerationStateChanged(mode);
        }

        /**
         * ?????????????????????????????????
         * @param value
         */
        @Override
        public void onPowerGenerationValueChanged(int value) {
            super.onPowerGenerationValueChanged(value);
        }

        /**
         * ?????????????????????????????????????????????????????????/?????????/?????????????????????/?????????????????????
         * @param type
         */
        @Override
        public void onRoadSurfaceChanged(int type) {
            super.onRoadSurfaceChanged(type);
        }

    };

    private final AbsBYDAutoStatisticListener absBYDAutoStatisticListener = new AbsBYDAutoStatisticListener() {
        /**
         * ?????????????????????
         * @param totalMileageValue
         */
        @Override
        public void onTotalMileageValueChanged(int totalMileageValue) {
            super.onTotalMileageValueChanged(totalMileageValue);
            KLog.e("??????????????????" + totalMileageValue + " km");
            MainActivity.this.totalMileageValue = totalMileageValue;
            binding.totalMileageTv.setText(totalMileageValue + " km");
            binding.totalHevMileageTv.setText((totalMileageValue - statisticDevice.getEVMileageValue()) + "km");
        }

        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onTotalFuelConChanged(double value) {
            super.onTotalFuelConChanged(value);
            KLog.e("??????????????????" + value + " L");
            binding.totalFuelCostTv.setText(format.format(value) + "L");
        }

        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onTotalElecConChanged(double value) {
            super.onTotalElecConChanged(value);
            KLog.e("??????????????????" + value + "KW???H");
            binding.totalElecCostTv.setText(format.format(value) + "KW???H");
        }

        /**
         * ???????????????????????????
         * @param value {0,9999.9}h
         */
        @Override
        public void onDrivingTimeChanged(double value) {
            super.onDrivingTimeChanged(value);
            binding.drivingTimeTv.setText(format.format(value) + " h");
        }

        /**
         * ?????????????????????????????????
         * @param value {0,51.1}L/100KM
         */
        @Override
        public void onLastFuelConPHMChanged(double value) {
            super.onLastFuelConPHMChanged(value);
            binding.lastFuelConPhmTv.setText(format.format(value) + "L/100KM");
        }

        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onTotalFuelConPHMChanged(double value) {
            super.onTotalFuelConPHMChanged(value);
            String s = format.format(value) + "(?????????)";
            totalFuelConPHM = s;
//            binding.totalFuelConPhmTv.setText(s);
        }


        /**
         * ?????????????????????????????????
         * @param value {-99.9,99.9}KWH/100KM
         */
        @Override
        public void onLastElecConPHMChanged(double value) {
            super.onLastElecConPHMChanged(value);
            binding.lastElecConPhmTv.setText(format.format(value) + "KWH/100KM");
        }

        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onTotalElecConPHMChanged(double value) {
            super.onTotalElecConPHMChanged(value);
            String s = format.format(value) + "(?????????)";
            totalElecConPHM = s;
//            binding.totalElecConPhmTv.setText(s);
        }

        /**
         * ???????????????????????????
         * @param value
         */
        @Override
        public void onElecDrivingRangeChanged(int value) {
            super.onElecDrivingRangeChanged(value);
            KLog.e("?????????????????????" + value + "km");
            updateElecDrivingRange(value);
        }


        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onFuelDrivingRangeChanged(int value) {
            super.onFuelDrivingRangeChanged(value);
            KLog.e("?????????????????????" + value + "km");
            updateFuelDrivingRange(value);
        }

        /**
         * ???????????????????????????
         * @param fuelPercentageValue
         */
        @Override
        public void onFuelPercentageChanged(int fuelPercentageValue) {
            super.onFuelPercentageChanged(fuelPercentageValue);
            KLog.e("????????????????????????" + fuelPercentageValue);
            updateFuelPercent(fuelPercentageValue);
        }

        /**
         * ???????????????????????????
         * @param value
         */
        @Override
        public void onElecPercentageChanged(double value) {
            super.onElecPercentageChanged(value);
            updateElecPercent(value);
        }

        /**
         * ??????????????????????????????
         * @param value
         */
        @Override
        public void onKeyBatteryLevelChanged(int value) {
            super.onKeyBatteryLevelChanged(value);
        }

        /**
         * ??????EV???????????????
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
         * ??????????????????[0-282]km/h
         * @param currentSpeed
         */
        @Override
        public void onSpeedChanged(double currentSpeed) {
            super.onSpeedChanged(currentSpeed);
//            KLog.e("???????????????" + currentSpeed);
            binding.carSpeedTv.setText(format.format(currentSpeed) + "km/h");
            binding.carSpeedCsv.setVelocity(((int) currentSpeed));

            //????????????
            updateEnginePower();
        }

        /**
         * ????????????????????????[0-100]%
         * @param value
         */
        @Override
        public void onAccelerateDeepnessChanged(int value) {
            super.onAccelerateDeepnessChanged(value);
        }

        /**
         * ????????????????????????[0-100]%
         * @param value
         */
        @Override
        public void onBrakeDeepnessChanged(int value) {
            super.onBrakeDeepnessChanged(value);
        }

    };
    private final AbsBYDAutoEngineListener absBYDAutoEngineListener = new AbsBYDAutoEngineListener() {
        /**
         * ???????????????????????????
         * @param engineSpeed
         */
        @Override
        public void onEngineSpeedChanged(int engineSpeed) {
            super.onEngineSpeedChanged(engineSpeed);
//            KLog.e("?????????????????????: " + engineSpeed);
            if (engineSpeed == 8191) {
                engineSpeed = 0;
            }
            binding.engineSpeedTv.setText(engineSpeed + " rpm");
            binding.engineSpeedEsv.setVelocity(engineSpeed);
        }

        /**
         * ?????????????????????????????????????????????
         * @param state
         */
        @Override
        public void onEngineCoolantLevelChanged(int state) {
            super.onEngineCoolantLevelChanged(state);
        }

        /**
         * ????????????????????????
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
        //6.1.2 ?????????
        BYDAutoBodyworkDevice bodyworkDevice = BYDAutoBodyworkDevice.getInstance(MainActivity.this);
        //LGXC76C44N0131100,LGXC76C44N0131101
        binding.textTv.setText(bodyworkDevice.getAutoVIN());

        //?????????
        int totalMileageValue = statisticDevice.getTotalMileageValue();
        absBYDAutoStatisticListener.onTotalMileageValueChanged(totalMileageValue);

        //???EV??????
        int evMileageValue = statisticDevice.getEVMileageValue();
        absBYDAutoStatisticListener.onEVMileageValueChanged(evMileageValue);

        //??????
        double currentSpeed = speedDevice.getCurrentSpeed();
        absBYDAutoSpeedListener.onSpeedChanged(currentSpeed);

        //????????????
        int energyMode = energyDevice.getEnergyMode();
        absBYDAutoEnergyListener.onEnergyModeChanged(energyMode);

        //????????????
        int operationMode = energyDevice.getOperationMode();
        absBYDAutoEnergyListener.onOperationModeChanged(operationMode);

        updateEnginePower();

        //??????????????????
        double totalFuelConValue = statisticDevice.getTotalFuelConValue();
        absBYDAutoStatisticListener.onTotalFuelConChanged(totalFuelConValue);

        //??????????????????
        double totalElecConValue = statisticDevice.getTotalElecConValue();
        absBYDAutoStatisticListener.onTotalElecConChanged(totalElecConValue);

        //???????????????
        double drivingTimeValue = statisticDevice.getDrivingTimeValue();
        absBYDAutoStatisticListener.onDrivingTimeChanged(drivingTimeValue);

        //??????50????????????
        double lastElecConPHMValue = statisticDevice.getLastElecConPHMValue();
        absBYDAutoStatisticListener.onLastElecConPHMChanged(lastElecConPHMValue);
        //??????50????????????
        double lastFuelConPHMValue = statisticDevice.getLastFuelConPHMValue();
        absBYDAutoStatisticListener.onLastFuelConPHMChanged(lastFuelConPHMValue);
        //??????????????????
        double totalElecConPHMValue = statisticDevice.getTotalElecConPHMValue();
        absBYDAutoStatisticListener.onTotalElecConPHMChanged(totalElecConPHMValue);
        //??????????????????
        double totalFuelConPHMValue = statisticDevice.getTotalFuelConPHMValue();
        absBYDAutoStatisticListener.onTotalFuelConPHMChanged(totalFuelConPHMValue);

        //????????????
        int gearboxAutoModeType = gearboxDevice.getGearboxAutoModeType();
        absBYDAutoGearboxListener.onGearboxAutoModeTypeChanged(gearboxAutoModeType);

        //?????????????????????
        updateElecPercent(statisticDevice.getElecPercentageValue());
        //?????????????????????
        updateElecDrivingRange(statisticDevice.getElecDrivingRangeValue());
        //?????????????????????
        updateFuelPercent(statisticDevice.getFuelPercentageValue());
        //????????????????????????
        updateFuelDrivingRange(statisticDevice.getFuelDrivingRangeValue());
    }

    /**
     * ??????????????????
     *
     * @param value
     */
    private void updateElecDrivingRange(int value) {
        binding.powerMileageTv.setText(value + "km");
    }

    /**
     * ??????????????????
     *
     * @param value
     */
    private void updateFuelDrivingRange(int value) {
        binding.fuelMileageTv.setText(value + "km");
    }

    /**
     * ???????????????
     *
     * @param fuelPercentageValue
     */
    private void updateFuelPercent(int fuelPercentageValue) {
        binding.fuelPercentPb.setMax(100);
        binding.fuelPercentPb.setProgress(fuelPercentageValue);
        binding.fuelPbTv.setText(fuelPercentageValue + "%");
    }

    /**
     * ???????????????
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
        KLog.e("????????????????????????" + ret);
        binding.elecPercentPb.setMax(100);
        binding.elecPercentPb.setProgress(((int) ret));
        binding.elecPbTv.setText(format.format(ret) + "%");
    }

    private void updateEnginePower() {
        int enginePower = engineDevice.getEnginePower();
        KLog.e("???????????????" + enginePower + " kw");
        binding.enginePowerTv.setText(enginePower + " kw");
        binding.enginePowerEpv.setVelocity(enginePower);

        updateEnergyCost();
    }

    private void updateEnergyCost() {
        //??????????????????
        double totalFuelConValue = statisticDevice.getTotalFuelConValue();
        //???????????????
        double totalElecConValue = statisticDevice.getTotalElecConValue();

        String elec_listener_and_cacu = totalElecConPHM + " " + format.format((totalElecConValue * 100.0f / totalMileageValue)) + "(?????????)";
        String fuel_listener_and_cacu = totalFuelConPHM + " " + format.format((totalFuelConValue * 100.0f / totalMileageValue)) + "(?????????)";
        binding.totalElecConPhmTv.setText(elec_listener_and_cacu);
        binding.totalFuelConPhmTv.setText(fuel_listener_and_cacu);
    }

    private String getEnergyModeName(int energyMode) {
        String modeName;
        switch (energyMode) {
            case BYDAutoEnergyDevice.ENERGY_MODE_STOP:
                modeName = "STOP";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_EV://??????
                modeName = "EV";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_FORCE_EV:
                modeName = "FORCE EV";
                break;
            case BYDAutoEnergyDevice.ENERGY_MODE_HEV://??????
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