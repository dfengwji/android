package com.zstart.action;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import com.zstart.action.model.AppInfo;
import com.zstart.action.util.APKUtil;
import com.zstart.action.util.BitmapUtil;
import com.zstart.action.util.FileUtil;
import com.zstart.action.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppLoadTask implements Runnable {
	public interface IAppLoader {
		void OnLoadCompleted(ArrayList<AppInfo> all);
	}
	
	private final Context context;
	private LauncherIconHelper iconHelper;
	private boolean isRunning;
	private String logoPath = "";
	
	//white packages
	private ArrayList<String> whitePackages = new ArrayList<>();
	
	private IAppLoader appLoader;
	public AppLoadTask(Context context,LauncherIconHelper helper, IAppLoader loader) {
		this.context = context;
		iconHelper = helper;
		appLoader = loader;
		isRunning = false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		isRunning = true;
		synchronized (context) {
			ArrayList<AppInfo> installedApps = new ArrayList<AppInfo>();
			final PackageManager manager = this.context.getPackageManager();
			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			
			final List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
			if (apps == null || apps.isEmpty()) {
				LogUtil.v("apps is Empty");
				return;
			}

			final int size = apps.size();
			ArrayList<String> tmpPackageList = new ArrayList<String>();
			LogUtil.d("all installed app count = "+size);
			for (int i = 0; i < size; i++) {
				ResolveInfo resolveInfo = apps.get(i);

				AppInfo info = updateAppInfo(resolveInfo,manager);
				if (info != null && !tmpPackageList.contains(info.packageName)) {
					tmpPackageList.add(info.packageName);
					info.type = 0;
					installedApps.add(info);
				}
			}
			
			checkHomeApps(installedApps);
			
			LogUtil.d("get app length = "+ installedApps.size());
			
			if (appLoader != null) {
				appLoader.OnLoadCompleted(installedApps);
				isRunning = false;
			}
		}
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public void setLogoPath(String path){
		logoPath = path;
	}
	
	public String getLogoPath(){
		return logoPath;
	}

	public void setWhitePackages(List<String> list){
		whitePackages.clear();
		whitePackages.addAll(list);
	}
	
	public synchronized AppInfo updateAppInfo(ResolveInfo resolveInfo,final PackageManager manager) {
		if (resolveInfo == null) {
			return null;
		}

		ComponentName componentName = APKUtil.getComponentFromResolveInfo(resolveInfo);
		ApplicationInfo app = resolveInfo.activityInfo.applicationInfo;
		if (isAdded(app)) {
			AppInfo info = new AppInfo();

			Intent intent = new Intent();
			intent.setComponent(componentName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			info.intent = intent;
			info.componentName = componentName;
			String pkgName = componentName.getPackageName();
			info.packageName = pkgName;
			String path = logoPath+"/"+pkgName+".jpg";
			byte[] buffer = FileUtil.readFile(path);
			if(buffer == null){
				info.icon = iconHelper.getIcon(info.componentName, resolveInfo);
				info.iconData = APKUtil.getBytesFromDrawable(info.icon);
				Bitmap bitmap = ((FastBitmapDrawable) info.icon).getBitmap();
				info.iconWidth = bitmap.getWidth();
				info.iconHeight = bitmap.getHeight();
				LogUtil.w(pkgName + " drawable icon width : height = "+info.iconWidth + "---"+info.iconHeight);
			}else{
				info.iconData = buffer;
				info.iconWidth = BitmapUtil.DEFAULT_TEXTURE_WIDTH;
				info.iconHeight = BitmapUtil.DEFAULT_TEXTURE_HEIGHT;
			}
			info.title = resolveInfo.activityInfo.loadLabel(manager);
			if (info.title == null) {
				info.title = "";
			}

			try {
				PackageInfo packageInfo = manager.getPackageInfo(pkgName,0);
				info.firstAddedTime = packageInfo.firstInstallTime;
				info.lastUpdatedTime = packageInfo.lastUpdateTime;
				info.sourceLocation = packageInfo.applicationInfo.publicSourceDir;
				info.version = packageInfo.versionName;
				info.versionCode = packageInfo.versionCode;
				info.size = new File(info.sourceLocation).length();
				info.isHome = false;
				//LogUtil.d("app source dir:" + info.sourceLocation);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return info;
		}
		return null;
	}
	
	public synchronized void checkHomeApps(ArrayList<AppInfo> apps){
		try{
			final PackageManager manager = this.context.getPackageManager();
			final Intent homeIntent = new Intent();
			homeIntent.setAction(Intent.ACTION_MAIN);
			homeIntent.addCategory(Intent.CATEGORY_HOME);
			final List<ResolveInfo> homeApps = manager.queryIntentActivities(homeIntent, 0);
			LogUtil.w("home apps count = " + homeApps.size());
			for(int i = 0,max = homeApps.size() ;i < max;i++){
				ResolveInfo homeResolve = homeApps.get(i);
				ComponentName component = APKUtil.getComponentFromResolveInfo(homeResolve);
				if(component != null){
					AppInfo app = getAppInfo(apps, component.getPackageName());
					if(app != null)
						app.isHome = true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private AppInfo getAppInfo(ArrayList<AppInfo> installedApps, String packageName) {
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
	
	/*
	 *could be added and then notify unity
	*/
	private boolean isAdded(ApplicationInfo app){
		if((app.flags & ApplicationInfo.FLAG_SYSTEM) > 0){
			//system apk file
			//filter the black packages
			if(whitePackages.contains(app.packageName))
				return true;
			else
				return false;
		}else{
			return true;
		}
	}
}
