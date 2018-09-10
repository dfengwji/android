package com.zstart.action.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Pair;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Unity调wifi的接口都放在这里
 */
public class AppWifiProxy implements IWifiProxy {
    private static String TAG = "AppWifiProxy";
    private WifiHelper helper;
    private Context mContext;
    public static String currentWifiStatus = null;

    public AppWifiProxy(Context context,ICallBack fun) {
        mContext = context;
        helper = new WifiHelper(mContext, fun);
        setWifiEventListener(true);
        Pair<String, String> config = readConfig();
        if (config != null) {
            helper.initReceiver();
            helper.updateWifi(config.first, config.second);
        } else {
            helper.enable();
        }
    }

    private synchronized Pair<String, String> readConfig() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/wifi.cf";
        try {
            File cfFile = new File(path);
            if (!cfFile.exists()) {
                LogUtil.d("wifi: there's no local config file found, use default config.");
                return null;
            }
            BufferedReader bReader = new BufferedReader(new FileReader(cfFile));
            String ssid = bReader.readLine();
            String password = bReader.readLine();
            LogUtil.d("wifi: read out local config: ssid=" + ssid + ", pwd=" + password);
            bReader.close();
            return new Pair<>(ssid, password);
        } catch (Exception e) {
            LogUtil.w(e.toString());
            return null;
        }
    }

    public int getWifiState() {
        return helper.getState();
    }

    public String getCurrentWifiStatus() {
        String ret = currentWifiStatus;
        currentWifiStatus = null;
        return ret;
    }

    public void setWifiEventListener(boolean flag) {
        LogUtil.d("wifi:setWifiEventListener" + String.valueOf(flag));
        if (flag == true) {
            helper.registerCallback(this);
        } else {
            helper.unregisterCallback(this);
        }
    }

    public void openWifi() {
        LogUtil.d("wifi:openWifi!");
        helper.enable();
    }

    public void closeWifi() {
        LogUtil.d("wifi:closeWifi");
        helper.disable();
    }

    public void startWifiScan() {
        LogUtil.d("wifi:startWifiScan");
        helper.startScanning();
//        setWifiEventListener(true);
    }

    public boolean getWifiScanning() {
        return helper.getScanningState();
    }

    public boolean checkWificonfiguration(String name, String address) {
        return helper.isConfigured(name);
    }

    public void forgetWifiNetwork(int netId) {
        LogUtil.d("forgetWifiNetwork:netid= " + String.valueOf(netId));
    }

    public void connectWifiByNetId(int netId) {
        LogUtil.d("connectWifiByNetId netId= " + String.valueOf(netId));
        helper.connect(netId);
    }

    // 连接wifi
    public void connectWifi(String name, String address, String pwd) {
        //mIVRManager.removeAllConfiguration();
        LogUtil.d("wifi:name:" + name + "address:" + address + "pwd:" + pwd);
        helper.connectNetWork(name, pwd);
    }

    public void disconnectWifi(int netId) {
        LogUtil.d("wifi: disconnectWifi & remove netid = " + String.valueOf(netId));
        helper.disableNetwork(netId);
        //mIVRManager.removeAllConfiguration();
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.removeNetwork(netId);
        wifiManager.saveConfiguration();
    }

    public String getActiveAccessPoint() {
        WifiAccessApInfo info = helper.getActiveAccessPoint();
        if (info == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("name", info.getName() == null ? "" : info.getName());
            object.put("address", info.getBSSID() == null ? "" : info.getBSSID());
            object.put("security", info.getSecurity());
            object.put("speed", info.getSpeed() == null ? "" : info.getSpeed());
            object.put("level", info.getLevel());
            object.put("netid", info.getNetId());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtil.d("wifi:getActiveAccessPoint = " + object.toString());
        return object.toString();
    }

    @Override
    public void onWifiStateChanged(int wifiState) {
        LogUtil.d("wifi:onWifiStateChanged:" + wifiState);
    }

    @Override
    public void updateAccessPoints(List<WifiAccessPoint> Aps) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        JSONArray jsonArray = new JSONArray();
//        LogUtil.d("updateAccessPoints size:" + Aps.size());
        for (int i = 0; i < Aps.size(); i++) {
            WifiAccessPoint ap = Aps.get(i);
            JSONObject object = new JSONObject();
            try {
                if (ap.getBSSID() == null) {
                    continue;
                }
                object.put("name", ap.getSSID() == null ? "" : ap.getSSID());
                object.put("addrss", ap.getBSSID() == null ? "" : ap.getBSSID());
                object.put("security", ap.getSecurity());
                object.put("level", ap.getLevel());
                object.put("netid", ap.getNetworkId());
//                public enum DetailedState {
//                    /** Ready to start data connection setup. */
//                    IDLE,
//                    /** Searching for an available access point. */
//                    SCANNING,
//                    /** Currently setting up data connection. */
//                    CONNECTING,
//                    /** Network link established, performing authentication. */
//                    AUTHENTICATING,
//                    /** Awaiting response from DHCP server in order to assign IP address information. */
//                    OBTAINING_IPADDR,
//                    /** IP traffic should be available. */
//                    CONNECTED,
//                    /** IP traffic is suspended */
//                    SUSPENDED,
//                    /** Currently tearing down data connection. */
//                    DISCONNECTING,
//                    /** IP traffic not available. */
//                    DISCONNECTED,
//                    /** Attempt to connect failed. */
//                    FAILED,
//                    /** Access to this network is blocked. */
//                    BLOCKED,
//                    /** Link has poor connectivity. */
//                    VERIFYING_POOR_LINK,
//                    /** Checking if network is a captive portal */
//                    CAPTIVE_PORTAL_CHECK
//                }

                if (ap.getDetailState() == DetailedState.DISCONNECTED && ap.getConfigureState() == 0) {

                }
                object.put("state", ap.getDetailState() != null ? ap.getDetailState().ordinal() : 0);
//                /** this is the network we are currently connected to */
//                public static final int CURRENT = 0;
//                /** supplicant will not attempt to use this network */
//                public static final int DISABLED = 1;
//                /** supplicant will consider this network available for association */
//                public static final int ENABLED = 2;
                object.put("wifiConfigStatus", ap.getConfigureState());
                object.put("disableReason", ap.getDisableReason());
                jsonArray.put(object);
//                LogUtil.d("updateAccessPoints: " + object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onScanFailedCallback() {
        //TODO:send message to unity scan faild
        return;
    }

    @Override
    public void onPasswordErrorCallback(WifiAccessPoint ap) {
        //TODO:send message to unity on password error
        return;
    }

    @Override
    public void onNotAccessInternetCallback() {
        //TODO:send message to unity can't access internet
        return;
    }
}
