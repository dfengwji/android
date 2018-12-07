package com.zstart.action.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;

import com.zstart.action.common.ICallBack;
import com.zstart.action.constant.ExceptionState;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.NetworkUtil;
import com.zstart.action.util.ExceptionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i11 on 15-6-16.
 */
public final class WifiHelper {
    private static final String TAG = "WifiHelper";
    private WifiManager mWifiManager;
    private ConnectivityManager connectManager;
    private Context mContext;
    private List<WifiAccessPoint> mWifiAps;
    private BroadcastReceiver mReceiver;
    private boolean isScanning = false;
    private String defaultSSID = "";
    private String defaultPassword = "";
    private ICallBack callBack;

    //能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    private WifiManager.WifiLock mWifiLock;

    //已连接过的wifi列表
    private List<WifiConfiguration> wifiConfigurationList;

    private  Handler wifiHandler;

    public WifiHelper(Context context,ICallBack fun) {
        mContext = context;
        callBack = fun;
        wifiConfigurationList = new ArrayList<>();
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiAps = new ArrayList<>();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    LogUtil.d("wifi: state changed that = "+state);
                    updateWifiState(state);
                } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    LogUtil.d("wifi: scan complete!!!");
                    updateAccessPoints();
                } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                    //密码错误
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    //错误码
                    int code = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, WifiManager.ERROR_AUTHENTICATING);
                    LogUtil.d("wifi: SUPPLICANT state = "+state+" ; error code = "+code);
                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    LogUtil.i("wifi:network state changed that is connected = "+info.isConnectedOrConnecting());
                    if(!info.isConnectedOrConnecting()) {
                        updateAccessPoints();
                        //updateConnectionState(info.getDetailedState(), action);
                        checkConnectTarget();
                    }else{
                        if(callBack != null){
                            callBack.connectSuccess(defaultSSID);
                        }
                    }
                } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    boolean connected = NetworkUtil.isWifiConnected(context);
                    LogUtil.i("wifi:connectivity action that is connected = "+connected);
                    if (!connected) {
                        removeAllConfiguration();
                        mWifiAps.clear();
                        startScanning();
                    }
                }
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    isScanning = false;
                    checkConnectTarget();
                    wifiHandler.sendEmptyMessageDelayed(0, 15 * 1000);
                }
            }
        };

        wifiHandler = new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                wifiHandler.removeMessages(msg.what);
                LogUtil.w("wifi:try to scan network!!!!");
                if (!TextUtils.isEmpty(defaultSSID) && !checkConnected(defaultSSID))
                    startScanning();
            }
        };
    }

    public void updateWifi(String ssid, String password) {
        enable();
        defaultSSID = ssid;
        defaultPassword = password;
        if (checkConnected(ssid))
            return;
        removeAllConfiguration();
        startScanning();
    }

    public void initReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mContext.registerReceiver(mReceiver, mFilter);
    }

    private void checkConnectTarget() {
        if (TextUtils.isEmpty(defaultSSID) || isScanning)
            return;
        if (checkConnected(defaultSSID))
            return;
        WifiAccessPoint point = getAccessPoint(defaultSSID);
        if (point == null) {
            LogUtil.d("wifi:can not find target = " + defaultSSID);
            return;
        }

        NetworkInfo.DetailedState st = point.getDetailState();
        if (st == NetworkInfo.DetailedState.CONNECTING
                || st == NetworkInfo.DetailedState.SCANNING
                || st == NetworkInfo.DetailedState.AUTHENTICATING
                || st == NetworkInfo.DetailedState.OBTAINING_IPADDR
                || st == NetworkInfo.DetailedState.CONNECTED) {
            LogUtil.d("wifi:is connecting ssid = " + defaultSSID);
            return;
        }
        LogUtil.d("wifi:ready to connect default ssid = " + defaultSSID + " and detail state = " + st);
        connectNetwork(defaultSSID, defaultPassword);
    }

    public boolean checkConnected(String target) {
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
        int type = ConnectivityManager.TYPE_DUMMY;
        if(networkInfo != null)
            type = networkInfo.getType();
        if(type != ConnectivityManager.TYPE_WIFI)
            return  false;
        int netID = 0;
        String ssid = null;
        if (networkInfo.isConnectedOrConnecting()) {
            final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
                netID = connectionInfo.getNetworkId();
            }
        }
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        LogUtil.d("wifi:current connected ssid: " + ssid + ", target: " + target);
        if (ssid.equals(convertToQuotedString(target)))
            return true;
        mWifiManager.disableNetwork(netID);
        mWifiManager.removeNetwork(netID);
        mWifiManager.disconnect();
        return false;
    }

    private WifiAccessPoint getAccessPoint(String ssid) {
        for (WifiAccessPoint point : mWifiAps) {
            if (point.getSSID().equals(ssid))
                return point;
        }
        return null;
    }

    //开启Wifi
    public void enable() {
        int state = mWifiManager.getWifiState();
        if (state == WifiManager.WIFI_STATE_DISABLED || state == WifiManager.WIFI_STATE_DISABLING) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    //关闭Wifi
    public void disable() {
        int state = mWifiManager.getWifiState();
        if (state == WifiManager.WIFI_STATE_ENABLED || state == WifiManager.WIFI_STATE_ENABLING) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /*
     *return: 0:Disabling;1:disabled;2:enabling;3:enabled
     */
    public int getState() {
        return mWifiManager.getWifiState();
    }


    public void startScanning() {
        if (isScanning) {
            return;
        }
        LogUtil.d("wifi:start scanning!!!");
        isScanning = mWifiManager.startScan();
    }

    public boolean getScanningState() {
        return isScanning;
    }

    //获取已连接的wifi
    public WifiAccessApInfo getActiveAccessPoint() {
        for (WifiAccessPoint ap : mWifiAps) {
            if (ap.getDetailState() == NetworkInfo.DetailedState.CONNECTED) {
                return new WifiAccessApInfo(mContext, ap);
            }
        }
        return null;
    }

    public boolean removeWifi(int netId) {
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 移除wifi
     *
     * @param SSID wifi名
     */
    public boolean removeWifi(String SSID) {
        WifiConfiguration conf = getExitsWifiConfig(SSID);
        if (conf != null) {
            return removeWifi(conf.networkId);
        } else {
            return false;
        }
    }

    /**
     * 创建一个WifiLock
     **/
    public void createWifiLock() {
        mWifiLock = this.mWifiManager.createWifiLock("testLock");
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     **/
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * 解锁WifiLock
     **/
    public void releaseWifilock() {
        if (mWifiLock.isHeld()) {
            //判断时候锁定
            mWifiLock.acquire();
        }
    }

    //Wifi帐号是否配置过
    public boolean isConfigured(String name) {
        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
        if (name != null && wifiConfigList != null) {
            LogUtil.d("wifi:isConfigured want config name = " + name);
            for (WifiConfiguration wifi : wifiConfigList) {
                if (wifi != null && wifi.SSID != null && wifi.SSID.equals(convertToQuotedString(name))) {
                    //LogUtil.d("wifi:"+ name + " isConfigured that ssid = " + wifi.SSID);
                    return true;
                }
            }
        } else {
            LogUtil.d("wifi:isConfigured null name or address!");
        }
        return false;
    }

    public void addNetWork(String SSID) {
        int netId = -1;
        if (getExitsWifiConfig(SSID) != null) {
            //这个wifi是连接过的，如果这个wifi在连接之后改了密码，那就只能手动去删除了
            netId = getExitsWifiConfig(SSID).networkId;
            //这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
            //无论是否连接上，都返回true。。。。
            mWifiManager.enableNetwork(netId, true);
        }
    }

    /**
     * 连接指定wifi
     * 6.0以上版本，直接查找时候有连接过，连接过的拿出wifiConfiguration用
     * 不要去创建新的wifiConfiguration,否者失败
     */
    public void addNetWork(String SSID, String password, int Type) {
        int netId = -1;
        /*先执行删除wifi操作，1.如果删除的成功说明这个wifi配置是由本APP配置出来的；
                           2.这样可以避免密码错误之后，同名字的wifi配置存在，无法连接；
                           3.wifi直接连接成功过，不删除也能用, netId = getExitsWifiConfig(SSID).networkId;*/
        if (removeWifi(SSID)) {
            //移除成功，就新建一个
            netId = mWifiManager.addNetwork(createNetworkConfig(SSID, password, Type));
        } else {
            //删除不成功，要么这个wifi配置以前就存在过，要么是还没连接过的
            if (getExitsWifiConfig(SSID) != null) {
                //这个wifi是连接过的，如果这个wifi在连接之后改了密码，那就只能手动去删除了
                netId = getExitsWifiConfig(SSID).networkId;
            } else {
                //没连接过的，新建一个wifi配置
                netId = mWifiManager.addNetwork(createNetworkConfig(SSID, password, Type));
            }
        }

        //这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
        //无论是否连接上，都返回true。。。。
        mWifiManager.enableNetwork(netId, true);
    }

    /**
     * 获取配置过的wifiConfiguration
     */
    public WifiConfiguration getExitsWifiConfig(String SSID) {
        wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigurationList) {
            if (wifiConfiguration.SSID.equals("\"" + SSID + "\"")) {
                return wifiConfiguration;
            }
        }
        return null;
    }

    /**
     * Add quotes around the string.
     *
     * @param string to convert
     * @return string with quotes around it
     */
    protected static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    //连接Wifi网络
    public boolean connectNetwork(String name, String psw) {
        LogUtil.d("wifi: connectNetwork that ssid = " + name);
        List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();
        if(configList != null) {
            for (WifiConfiguration conf : configList) {
                if ((conf.SSID).equals(convertToQuotedString(name))) {
                    if (psw != null && !conf.preSharedKey.equals(psw)) {
                        conf.preSharedKey = psw;
                        LogUtil.d("wifi:reset password = " + psw);
                    }
                    LogUtil.d("wifi: try to connect saved SSID = " + conf.SSID + " and psw = " + psw);
                    mWifiManager.updateNetwork(conf);
                    mWifiManager.saveConfiguration();
                    mWifiManager.enableNetwork(conf.networkId, true);
                    return true;
                }
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        for (ScanResult result : results) {
            if ((result.SSID).equals(name)) {
                LogUtil.d("wifi: try to connect SSID = " + result.SSID + " and psw = " + psw);
                int security = WifiConstant.getSecurity(result);
                WifiConfiguration config = createNetworkConfig(name, psw, security);
                int newWorkId = mWifiManager.addNetwork(config);
                config.networkId = newWorkId;
                mWifiManager.enableNetwork(config.networkId, true);
                mWifiManager.saveConfiguration();
                return true;
            }
        }
        LogUtil.d("wifi:can not find the ssid = " + name);
        if(callBack != null)
            callBack.connectFailed(ExceptionUtil.getExceptionTip(mContext, ExceptionState.Wifi_Error_SSID)+ ":" + name);
        return false;
    }

    //连接网络，与enableNetWork区别在于必须知道netId
    public void connect(int netId) {
        LogUtil.d("wifi:connect target that net id = " + netId);
        mWifiManager.enableNetwork(netId,true);
    }

    //忘记网络
    public void forgetNetwork(int netId) {
        mWifiManager.disableNetwork(netId);
    }

    //断开wifi,但不会移除configuration
    public void disableNetwork(int netId) {
        mWifiManager.disableNetwork(netId);
    }

    //断开activite的连接
    public boolean disconnect() {
        return mWifiManager.disconnect();
    }

    //清除所有Wifi配置
    public void removeAllConfiguration() {
        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
        if (wifiConfigList == null)
            return;
        for (WifiConfiguration wifi : wifiConfigList) {
            mWifiManager.removeNetwork(wifi.networkId);
            mWifiManager.saveConfiguration();
        }
    }

    //生成Configuration
    private WifiConfiguration createNetworkConfig(String SSID, String pwd, int security) {
        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        LogUtil.d("wifi:generateOpenNetworkConfig: Security= " + security + " password= " + pwd);
        if (security == WifiConstant.SECURITY_NONE) {
            LogUtil.d("wifi:UWifiAccessPoint.SECURITY_NONE");
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (security == WifiConstant.SECURITY_WEP) {
            config.hiddenSSID = true;
            //Fix: wep hex or ascii password can't connect
            LogUtil.d("wifi:WEP password length = " + pwd.length());
            if ((pwd.length() == 10 || pwd.length() == 26 || pwd.length() == 32)
                    && pwd.matches("[0-9A-Fa-f]*")) {
                config.wepKeys[0] = pwd;
            } else {
                config.wepKeys[0] = "\"" + pwd + "\"";
            }
            LogUtil.d("WEP password = " + config.wepKeys[0]);
            //end
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (security == WifiConstant.SECURITY_WPA_PSK
                || security == WifiConstant.SECURITY_WPA2_PSK
                || security == WifiConstant.SECURITY_PSK) {
            config.preSharedKey = "\"" + pwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();
        LogUtil.d("wifi:updateAccessPoints wifiState = " + wifiState);
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                final Collection<WifiAccessPoint> accessPoints = getAccessPoints();
                if (accessPoints.size() < 1) {
                    LogUtil.d("wifi:updateAccessPoints return 0 ");
                    return;
                }

                mWifiAps.clear();
                for (WifiAccessPoint ap : accessPoints) {
                    //remove author faild
                    if (ap.getConfigureState() == WifiConfiguration.Status.DISABLED) {
                        LogUtil.d("wifi:connect failed that reason = " + ap.getDisableReason() + " and ssid = " + ap.getSSID());
                        mWifiManager.removeNetwork(ap.getConfig().networkId);
                        mWifiManager.saveConfiguration();//save configure
                        if (ap.getSecurity() == WifiConstant.SECURITY_WEP &&
                                ap.getDisableReason() == -1) {
                            if(callBack != null)
                                callBack.connectFailed(ExceptionUtil.getExceptionTip(mContext, ExceptionState.WifiError));
                        } else {
                            if(callBack != null)
                                callBack.connectFailed(ExceptionUtil.getExceptionTip(mContext, ExceptionState.Wifi_Error_PSW));
                        }
                    }
                    mWifiAps.add(ap);
                }

                LogUtil.d("wifi:updateAccessPoints ap size:" + mWifiAps.size());
                //mListViewAdapter.notifyDataSetChanged();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                break;
            default:
                break;
        }
    }

    private void updateConnectionState(NetworkInfo.DetailedState state, String action) {
      /*  if (!mWifiManager.isWifiEnabled()) {
//            mScanner.pause();
            return;
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (mLastInfo == null)
            return;

        if (state != null) {
            mLastState = state;
        }

        LogUtil.d("wifi:updateConnectionState: SSID: " + mLastInfo.getSSID() + "; LastState:" + state);
        for (WifiAccessPoint ap : mWifiAps) {
            ap.update(mLastInfo, mLastState);
        }*/
    }

    private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_DISABLED
                || state == WifiManager.WIFI_STATE_DISABLING) {
            enable();
        }
    }

    /**
     * Lookup table to more quickly update AccessPoints by only considering
     * objects with the correct SSID. Maps SSID -> List of AccessPoints with
     * the given SSID.
     */
    private List<WifiAccessPoint> getAccessPoints() {
        ArrayList<WifiAccessPoint> accessPoints = new ArrayList<>();
        Multimap<String, WifiAccessPoint> apMap = new Multimap<>();
        //获取wificonfiguration
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                WifiAccessPoint accessPoint = new WifiAccessPoint(null, config);

                accessPoints.add(accessPoint);
                apMap.put(accessPoint.getSSID(), accessPoint);
            }
        }

        //获取最近一次扫描的AccessPoint
        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            LogUtil.d("wifi:getAccessPoints results = " + results.size());
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (WifiAccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    WifiAccessPoint accessPoint = new WifiAccessPoint(null, result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.getSSID(), accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        ArrayList<WifiAccessPoint> origAccessPoints = new ArrayList<>(accessPoints.size());
        origAccessPoints.addAll(accessPoints);
        //sort ap
        try {
            Collections.sort(accessPoints);
        } catch (ClassCastException e) {
            LogUtil.d("wifi: collection.sort exception;origAccessPoints="
                    + origAccessPoints);
            return origAccessPoints;
        } catch (UnsupportedOperationException e) {
            LogUtil.d("wifi:collection.sort exception;origAccessPoints="
                    + origAccessPoints);
            return origAccessPoints;
        }
        return accessPoints;
    }

    /**
     * A restricted multimap for use in constructAccessPoints
     */
    private class Multimap<K, V> {
        private HashMap<K, List<V>> store = new HashMap<K, List<V>>();

        /**
         * retrieve a non-null list of values with key K
         */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }
}
