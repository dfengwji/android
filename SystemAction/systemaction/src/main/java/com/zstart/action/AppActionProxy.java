package com.zstart.action;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.StatFs;
import com.zstart.action.AppLoadTask.IAppLoader;
import com.zstart.action.common.ActionCallBack;
import com.zstart.action.common.ICallBack;
import com.zstart.action.model.AppInfo;
import com.zstart.action.util.APKUtil;
import com.zstart.action.util.JsonUtil;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;
import com.zstart.action.wifi.AppWifiProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppActionProxy {
	public final static String SHARED_KEY_MAIN = "com.zstart.apps";
	public final static String SHARED_KEY_SUB = "com.zstart.apps.installing";

	private Context context;
	private ArrayList<AppInfo> installedApps = new ArrayList<>();
	private AppLoadTask appLoader;
	private HandlerThread loadThread;
	private Handler loadHandler;
	private LauncherIconHelper iconHelper;
	private ICallBack callBack;
	private AppWifiProxy wifiProxy;
	public AppActionProxy(){
        callBack = new ActionCallBack();
        LogUtil.d("Init AppActionProxy....");
	}

	public void setContext(Context act){
        this.context = act;
        iconHelper = new LauncherIconHelper(this.context);
        //wifiProxy = new AppWifiProxy(act, callBack);
    }

	public void setCallBack(ICallBack fun){
	    callBack = fun;
    }

	public void loadInit(){
        appLoader = new AppLoadTask(this.context,iconHelper, new IAppLoader() {

            @Override
            public void OnLoadCompleted(ArrayList<AppInfo> all) {
                if (null != all) {
                    installedApps = all;
                    LogUtil.d("send message that installed Apps length = "+all.size());
                    if(callBack != null){
                        callBack.loadComplete(all);
                    }
                }
            }
        });

        loadThread = new HandlerThread("loadThread");
        loadThread.start();
        loadHandler = new Handler(loadThread.getLooper());
    }

	public String getInstalledJSON() {
		return JsonUtil.getInstalledJSON(installedApps);
	}
	
	public void setLogoPath(String path){
		if(appLoader == null)
		    return;
        appLoader.setLogoPath(path);
	}
	
	public void setWhitePackages(List<String> list){
		if(appLoader == null) {
            return;
        }
        appLoader.setWhitePackages(list);
	}
	
	private void runOnLoadThread(Runnable r) {
        if(loadThread == null)
            return;
		if (loadThread.getThreadId() == Process.myTid()) {
			r.run();
		} else {
			loadHandler.post(r);
		}
	}
	
	public void clearSharedData(){
		SharedPreferences sp = context.getSharedPreferences(SHARED_KEY_MAIN,Context.MODE_PRIVATE);
		sp.edit().remove(SHARED_KEY_SUB).apply();
	}
	
	public synchronized boolean install(String appPath,String appPkg,String selfPkg) {
		try{		
			if(isInstalling(appPkg)){
				LogUtil.d("isApkInstalling...."+appPkg);
				return false;
			}
			
			LogUtil.d("installAPK...."+appPath+" , "+appPkg);
			File file = new File(appPath);
			if (!file.exists()) {
				return false;
			}
	
			LogUtil.v("installAPK that pkgName : " + appPkg);
			Thread thread = new Thread(new SilentInstallTask(this.context,selfPkg, appPkg, appPath));
			thread.start();
			return  true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public void uninstall(String packageName) {
		LogUtil.d("silent uninstall app:" + packageName);
		Thread thread = new Thread(new SilentUninstallTask(this.context, packageName));
		thread.start();
	}

	public List<AppInfo> getApps() {
		LogUtil.d("getInstalledApps ...");
		return installedApps;
	}
	
	public void loadApps(){
        if(appLoader == null)
            return;
		if (!appLoader.isRunning()) {
			LogUtil.d("loadInstalledApps begin....");
			runOnLoadThread(appLoader);
		}
	}
	
	public void removeApp(String packageName){
		for(int i = 0;i < installedApps.size();i++){
			if(installedApps.get(i).packageName.equals(packageName)){
				installedApps.remove(i);
				LogUtil.d("removeInstalledApp ...."+packageName);
				break;
			}
		}
	}
	
	private AppInfo getInfo(String packageName) {
		if (installedApps == null || packageName == null || packageName.isEmpty()) {
			LogUtil.w("package is error!!!! package = "+packageName);
			return null;
		}
		for(int i = 0,max = installedApps.size() ;i < max;i++){
			AppInfo info = installedApps.get(i);
			if(packageName.equals(info.packageName))
				return info;
		}
		return null;
	}
	
	private void addInfo(AppInfo info){
		if(info == null || hadInstalled(info.packageName))
			return;
		installedApps.add(info);
	}
	
	public AppInfo getApp(String pkgName){
		AppInfo info = getInfo(pkgName);
		LogUtil.d("update installed app = "+pkgName + "---"+info);
		if(info == null){
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
	
			final PackageManager packageManager = this.context.getPackageManager();
			final List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
			if (apps == null || apps.isEmpty()) {
				LogUtil.v("local apps is empty");
				return null;
			}
			for (int i = 0,size = apps.size(); i < size; i++) {
				ResolveInfo resolveInfo = apps.get(i);
				
				ComponentName component = APKUtil.getComponentFromResolveInfo(resolveInfo);
				if (component != null && pkgName.equals(component.getPackageName())) {
					info = appLoader.updateAppInfo(resolveInfo,packageManager);
					addInfo(info);
					break;
				}
			}
			
			appLoader.checkHomeApps(installedApps);
		}
		return info;
	}

	public long getStorage(int type){
		File file = Environment.getDataDirectory();
		if(type == 0){
			return getSFSize(file.getPath(), false);
		}else{
			return getSFSize(file.getPath(), true);
		}
	}

	private long getSFSize(String path, boolean free) {
		LogUtil.w("action: getSFSize path = " + path + ", available = " + free);
		StatFs sf = new StatFs(path);
		return free ? sf.getAvailableBytes() : sf.getTotalBytes();
	}

	public String getSN(){
		LogUtil.d("action: getSN");
		return SystemUtil.getSerialNumber(context);
	}

	public String getMacAddress(){
	    return SystemUtil.getMacAddress(context);
    }
	
	public byte[] getAppIcon(String packageName)
	{
		AppInfo videoInfo = getInfo(packageName);
		if(videoInfo != null)
			return videoInfo.iconData;
		else
			return new byte[1];
	}

	public boolean run(String packageName) {
		try {
			AppInfo app = getApp(packageName);
			if(app == null || app.isHome){
				LogUtil.v("apps is home");
				return false;
			}
			PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.getApplicationContext().startActivity(intent);
            return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			callBack.runFailed(packageName,e.getMessage());
			return false;
		}
	}
	
	public boolean hadInstalled(String packName){
		for(int i = 0;i < installedApps.size();i++){
			if(installedApps.get(i).packageName.equals(packName))
				return true;
		}
		return false;
	}
	
	public void check(String pkgName){
		new Thread(new AppCheckTask(context , pkgName ,callBack)).start();
	}
	
	public boolean isInstalling(String pkgName){
		SharedPreferences sp = context.getSharedPreferences(SHARED_KEY_MAIN,Context.MODE_PRIVATE);
		Set<String> set = sp.getStringSet(SHARED_KEY_SUB, null);
		
		if(set == null)
			return false;
		if(set.contains(pkgName))
			return true;
		else
			return false;
	}
}
