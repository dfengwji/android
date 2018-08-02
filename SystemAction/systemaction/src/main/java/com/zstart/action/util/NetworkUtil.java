package com.zstart.action.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public final class NetworkUtil {
	 public static boolean isWifiConnected(Context context) {
	        ConnectivityManager connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        if (connetManager == null) {
	        	LogUtil.w("isNetWorkAvailable connetManager = null");
	            return false;
	        }
	        NetworkInfo[] infos = connetManager.getAllNetworkInfo();
	        if (infos == null) {
	        	LogUtil.w("NetworkInfo= null");
	            return false;
	        }
	        for (int i = 0; i < infos.length && infos[i] != null; i++) {
	            if (infos[i].isConnected() && infos[i].isAvailable()) {
	            	return true;
	            }
	        }
	        return false;
	  }

	public static int getWifiState(Context context){
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return manager.getWifiState();
	}

	public static boolean isWifiEnabled(Context context){
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return manager.isWifiEnabled();
	}

	/*
	返回的数值越大，信号强度越好[0,4]
	 */
	public static int getWifiRssi(Context context){
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		if(info == null)
			return 0;
		int level = info.getRssi();
     int strength = WifiManager.calculateSignalLevel(level,5);
		/*if(level > -40){
			strength = 5;
		}else if(level > -60){
			strength = 4;
		}else if(level > -80)
			strength = 3;
		else if(level > -90)
			strength = 2;
		else
			strength = 1;*/
		return strength;
	}
}
