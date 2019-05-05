package com.idealens.system.status;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.NetworkUtil;
import com.zstart.action.util.SystemUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @file:com.idealens.system.status.DeviceProxy.java
 * @authoer:huangzizhen
 * @date:2017/12/7
 * @version:V1.0
 */

public class DeviceProxy {
    public final static int WIFI_STATE_CONNECTED = 0;
    public final static int WIFI_STATE_CLOSE = 1;
    public final static int WIFI_STATE_DISCONNECT = 2;

    private DeviceStateInfo stateInfo;
    private DeviceBaseInfo baseInfo;
    private StorageHelper storageHelper;
    private int vrBattery = 0;
    private Context context = null;
    private String notifyTarget = "AndroidNotifyReceiver";
    private String notifyFun = "OnDeviceStateUpdate";
    private boolean isScreenOn = false;
    private BroadcastReceiver castReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    isScreenOn = false;
                    break;
                case Intent.ACTION_SCREEN_ON:
                    isScreenOn = true;
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int percent = getVRGlassesBattery(intent);
                    if (stateInfo.updateGlassesBattery(percent)) {
                        sendNotify(DeviceStateType.Battery_Glasses, percent + "");
                    }
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    stateInfo.isCharging = true;
                    sendNotify(DeviceStateType.GlassesCharging, "1");
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    stateInfo.isCharging = false;
                    sendNotify(DeviceStateType.GlassesCharging, "0");
                    break;
                case Intent.ACTION_HEADSET_PLUG:
                    if(intent.hasExtra("state")){
                        stateInfo.earphone = intent.getIntExtra("state",0) != 0;
                    }else
                        stateInfo.earphone = false;
                    break;
                case WifiManager.RSSI_CHANGED_ACTION:
                    int strength = NetworkUtil.getWifiRssi(context);
                    if (stateInfo.updateWifiStrength(strength))
                        sendNotify(DeviceStateType.WifiStrength, strength + "");
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int bst = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    LogUtil.d("status: bluetooth state changed!!!" + bst);
                    if (bst == BluetoothAdapter.STATE_OFF || bst == BluetoothAdapter.STATE_TURNING_OFF) {
                        stateInfo.updateBluetooth(0);
                        sendNotify(DeviceStateType.Bluetooth, "0");
                    } else if (bst == BluetoothAdapter.STATE_ON || bst == BluetoothAdapter.STATE_TURNING_ON) {
                        stateInfo.updateBluetooth(1);
                        sendNotify(DeviceStateType.Bluetooth, "1");
                    }
                    break;
            }
        }
    };

    public DeviceProxy(Context context,String u3dTarget,String u3dFun) {
        this.notifyTarget = u3dTarget;
        this.notifyFun = u3dFun;
        this.context = context;
        storageHelper = new StorageHelper(context);
        stateInfo = new DeviceStateInfo();
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        stateInfo.glassesBattery = getVRGlassesBattery(intent);
        stateInfo.isCharging = isVRCharging(intent);
        stateInfo.joystickBattery = getJoystickBattery();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int st = adapter.getState();
        if(st == BluetoothAdapter.STATE_OFF || st == BluetoothAdapter.STATE_TURNING_OFF)
            stateInfo.bluetooth = 0;
        else if(st == BluetoothAdapter.STATE_ON || st == BluetoothAdapter.STATE_TURNING_ON)
            stateInfo.bluetooth = 1;
        stateInfo.internalStorage = storageHelper.getSelfStorage();
        stateInfo.externalStorage = storageHelper.getTFStorage();
        stateInfo.otgStorage = storageHelper.getOTGStorage().first;

        initBaseInfo();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(isScreenOn)
                    updateTimerTask();
            }
        };
        Timer timer = new Timer(false);
        timer.schedule(timerTask, 1000, 6000);
        initReceiver();
    }

    public void sendNotify(DeviceStateType type, String flag) {
        if (UnityPlayer.currentActivity != null && isScreenOn) {
            String tmp = type.getCode() + "|" + flag;
            LogUtil.v("status: sendDeviceNotify.....param = " + tmp);
            UnityPlayer.UnitySendMessage(notifyTarget, notifyFun, tmp);
        }
    }

    private void updateTimerTask() {
        int percent = getJoystickBattery();
        boolean batteryUp = stateInfo.updateJoystickBattery(percent);
        if (batteryUp) {
            if (stateInfo.joystickBattery < 0 || stateInfo.joystickBattery > 100)
                sendNotify(DeviceStateType.Battery_Joystick, "0");
            else
                sendNotify(DeviceStateType.Battery_Joystick, percent + "");
        }
    }

  /*  public long getTotalMemory() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/meminfo")), 1000);
            String content = reader.readLine();
            LogUtil.d("readLine   = " + content);
            reader.close();
            int begin = content.indexOf(':');
            int end = content.indexOf('k');
            content = content.substring(begin + 1, end).trim();
            return Long.parseLong(content);
        } catch (IOException ex) {
            LogUtil.e("IOException" + ex.toString());
            return -1;
        }
    }*/

    public long getFreeMemory() {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / 1024;
    }

    public long getTotalMemory() {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.totalMem / 1024;
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        context.registerReceiver(castReceiver, filter);
    }

    private void initBaseInfo(){
        baseInfo = new DeviceBaseInfo();
        baseInfo.id = SystemUtil.id(context);
        baseInfo.sn = SystemUtil.getSerialNumber(this.context);
        baseInfo.ipAddress = SystemUtil.getIPAddress(this.context);
        baseInfo.memory = getTotalMemory();
        baseInfo.internalStorage = storageHelper.getSelfStorage();
        baseInfo.externalStorage = storageHelper.getTFStorage();
        baseInfo.otgStorage = storageHelper.getOTGStorage().second;
        baseInfo.macAddress = getMacAddress();
        baseInfo.bluetoothAddress = getBluetoothAddress();
        baseInfo.androidVersion = Build.VERSION.RELEASE;
        baseInfo.osVersion = SystemUtil.getROMVersion();
        baseInfo.module = SystemUtil.getSystemProperty("persist.sys.product.model","");
    }

    public static String getVersion() {
        String ret = "";
        try {
            Class<?> sys = Class.forName("android.os.SystemProperties");
            Method get = sys.getMethod("get", String.class);
            ret += (String) get.invoke(sys, "ro.build.display.id");
        } catch (Exception e) {

        }
        return ret;
    }

    public DeviceStateInfo getInfo() {
        return stateInfo;
    }

    //region External API
    public void updateBaseInfo(){
        baseInfo.externalStorage = storageHelper.getTFStorage();
        baseInfo.otgStorage = storageHelper.getOTGStorage().second;
    }

    public String getStateInfo() {
        StatusSetInfo info = new StatusSetInfo();
        try {
            if (stateInfo.joystickBattery < 0 || stateInfo.joystickBattery > 100) {
                info.addStateItem(DeviceStateType.Battery_Joystick,"0");
            } else {
                info.addStateItem(DeviceStateType.Battery_Joystick, stateInfo.joystickBattery+"");
            }
            info.addStateItem(DeviceStateType.Battery_Glasses, stateInfo.glassesBattery+"");
            info.addStateItem(DeviceStateType.GlassesCharging,(stateInfo.isCharging ? 1 : 0)+"");
            info.addStateItem(DeviceStateType.Bluetooth, stateInfo.bluetooth+"");
            info.addStateItem(DeviceStateType.Earphone, stateInfo.earphone ? "1" : "0");
            info.addStateItem(DeviceStateType.Storage_TF,stateInfo.externalStorage+"");
            info.addStateItem(DeviceStateType.Storage_Self,stateInfo.internalStorage+"");
            info.addStateItem(DeviceStateType.Storage_OTG,stateInfo.otgStorage+"");
            info.addStateItems(getNetworkInfo().getStateItems());
            LogUtil.d("status:check state info....." + info.toString());
            return info.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getStateByType(int type){
        if(type == DeviceStateType.Battery_Glasses.getCode()){
            return stateInfo.glassesBattery+"";
        }else if(type == DeviceStateType.Battery_Joystick.getCode()){
            return stateInfo.joystickBattery+"";
        }else if(type == DeviceStateType.Bluetooth.getCode()){
            return stateInfo.bluetooth+"";
        }else if(type == DeviceStateType.Earphone.getCode()){
            return stateInfo.earphone ? "1" : "0";
        }else if(type == DeviceStateType.GlassesCharging.getCode()){
            return stateInfo.isCharging ? "1" : "0";
        }else if(type == DeviceStateType.Memory.getCode()){
            long memory = getFreeMemory();
            if(stateInfo.updateMemory(memory)){
                stateInfo.memory = getFreeMemory();
            }
            return stateInfo.memory+"";
        }else if(type == DeviceStateType.Storage_TF.getCode()){
            if(baseInfo.externalStorage > 0)
                stateInfo.externalStorage = storageHelper.getFreeTFStorage();
            return stateInfo.externalStorage+"";
        }else if(type == DeviceStateType.Storage_Self.getCode()){
            stateInfo.internalStorage = storageHelper.getFreeSelfStorage();
            return stateInfo.internalStorage+"";
        }else if(type == DeviceStateType.Storage_OTG.getCode()){
            if(baseInfo.otgStorage > 0)
                stateInfo.otgStorage = storageHelper.getOTGStorage().first;
            return stateInfo.otgStorage+"";
        }else if(type == DeviceStateType.WifiStrength.getCode()){
            return stateInfo.wifiStrength+"";
        }else if(type == DeviceStateType.Server.getCode()){
            return "0";
        }else
            return "";
    }

    public String getBaseInfo(){
        StatusSetInfo info = new StatusSetInfo();
        try {
            info.addBaseItem(DeviceBaseType.Address_Bluetooth, baseInfo.bluetoothAddress);
            info.addBaseItem(DeviceBaseType.Address_IP,baseInfo.ipAddress);
            info.addBaseItem(DeviceBaseType.Address_Mac, baseInfo.macAddress);
            info.addBaseItem(DeviceBaseType.Memory, baseInfo.memory+"");
            info.addBaseItem(DeviceBaseType.Storage_TF, baseInfo.externalStorage+"");
            info.addBaseItem(DeviceBaseType.Storage_Self, baseInfo.internalStorage+"");
            info.addBaseItem(DeviceBaseType.Storage_OTG, baseInfo.otgStorage+"");
            info.addBaseItem(DeviceBaseType.Version_Android, baseInfo.androidVersion);
            info.addBaseItem(DeviceBaseType.Version_OS, baseInfo.osVersion);
            info.addBaseItem(DeviceBaseType.SN, baseInfo.sn);
            info.addBaseItem(DeviceBaseType.Module, baseInfo.module);
            LogUtil.d("status:check base info....." + info.toBaseString());
            return info.toBaseString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getBaseByType(int type){
        if(type == DeviceBaseType.Address_Bluetooth.getCode()){
            return baseInfo.bluetoothAddress;
        }else if(type == DeviceBaseType.Address_IP.getCode()){
            return baseInfo.bluetoothAddress;
        }else if(type == DeviceBaseType.Address_Mac.getCode()){
            return baseInfo.bluetoothAddress;
        }else if(type == DeviceBaseType.CPU.getCode()){
            return "CPU";
        }else if(type == DeviceBaseType.GPU.getCode()){
            return "GPU";
        }else if(type == DeviceBaseType.Memory.getCode()){
            return baseInfo.memory+"";
        }else if(type == DeviceBaseType.Module.getCode()){
            return baseInfo.module;
        }else if(type == DeviceBaseType.SN.getCode()){
            return baseInfo.sn;
        }else if(type == DeviceBaseType.Storage_TF.getCode()){
            return baseInfo.externalStorage+"";
        }else if(type == DeviceBaseType.Storage_Self.getCode()) {
            return baseInfo.internalStorage + "";
        }else if(type == DeviceBaseType.Storage_OTG.getCode()){
                return baseInfo.otgStorage+"";
        }else if(type == DeviceBaseType.Version_Android.getCode()){
            return baseInfo.androidVersion;
        }else if(type == DeviceBaseType.Version_OS.getCode()){
            return baseInfo.osVersion;
        }else
            return "";
    }
//endregion

    private StatusSetInfo getNetworkInfo() {
        StatusSetInfo info = new StatusSetInfo();
        int wifiState = checkWifiState(this.context);
        if (wifiState == WIFI_STATE_CLOSE || wifiState == WIFI_STATE_DISCONNECT) {
            info.addStateItem(DeviceStateType.WifiStrength,"0");
        } else {
            info.addStateItem(DeviceStateType.WifiStrength, stateInfo.wifiStrength+"");
        }
        return info;
    }

    /**
     * 获得本地mac地址
     *
     * @return
     */
    public String getMacAddress() {
        String address = "02:00:00:00:00:00";
        String name = SystemUtil.getSystemProperty("wifi.interface","");
        String path = String.format("/sys/class/net/%s/address", name);
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(path);
            bufferedReader = new BufferedReader(fileReader);
            address = bufferedReader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("debugxxx", "wifi mac address: " + address);
        return address;
    }

    /**
     * 获得蓝牙地址
     *
     * @return
     */
    public String getBluetoothAddress() {
        BluetoothManager bm = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        String address = bm.getAdapter().getAddress();
        LogUtil.d("status: get local bluetooth address:" + address);
        return address;
    }

    private int checkWifiState(Context context) {
        if (!NetworkUtil.isWifiEnabled(context)) {
            return WIFI_STATE_CLOSE;
        } else if (!NetworkUtil.isWifiConnected(context)) {
            return WIFI_STATE_DISCONNECT;
        } else {
            return WIFI_STATE_CONNECTED;
        }
    }

    public int getJoystickBattery() {
        return -1;
    }

    public boolean isVRCharging(Intent intent) {
        if (intent == null)
            return false;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public int getVRGlassesBattery(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = level * 100 / scale;
        if (vrBattery == percent)
            return vrBattery;
        vrBattery = percent;
        return vrBattery;
    }
}
