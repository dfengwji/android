package com.zstart.action.wifi;


import java.util.List;

/**
 * Created by i11 on 15-6-18.
 */
public interface IWifiProxy {

    void onWifiStateChanged(int wifiState);

    void updateAccessPoints(List<WifiAccessPoint> Aps);

    void onScanFailedCallback();

    void onPasswordErrorCallback(WifiAccessPoint ap);

    void onNotAccessInternetCallback();
    //void updateConnectionState(WifiInfo wifiInfo);

}
