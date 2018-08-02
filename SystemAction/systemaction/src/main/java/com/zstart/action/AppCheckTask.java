package com.zstart.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.MD5Util;
import com.zstart.action.util.LogUtil;

import dalvik.system.DexClassLoader;

public class AppCheckTask implements Runnable {
	private String packageName;
	private Context context;
	private ICallBack callBack;
	public AppCheckTask(Context cont,String pkgName, ICallBack fun){
		packageName = pkgName;
		context = cont;
		callBack = fun;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			boolean isFound = false;
			Context appContext = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
			if(appContext != null){
				try {
					InputStream inputStream = appContext.getAssets().open("bin/Data/Managed/Ivrplugin.dll");
					String result = MD5Util.md5sum(inputStream);
					//return CheckAppAuth.MD5_String.equals(result);
                    isFound = !result.isEmpty();
				}
				catch (IOException e) {
					LogUtil.d("check app invalid 1");
					e.printStackTrace();
					isFound = false;
				}
				if(isFound){
                    if(callBack != null){
                    	callBack.checkSuccess(packageName);
					}
					return;
				}
				try {
					DexClassLoader dLoader = new DexClassLoader(appContext.getApplicationInfo().sourceDir,
							"/data/data/com.idealsee.appmanager", null, ClassLoader.getSystemClassLoader().getParent());
                    Class calledClass = dLoader.loadClass("com.idealsee.vr.NativeInterface");
                    if(calledClass == null)
                        isFound = false;
                    else
                        isFound = true;
				}
				catch (ClassNotFoundException e) {
					LogUtil.d("check app invalid 2");
					e.printStackTrace();
					isFound = false;
				}
				if(isFound){
					if(callBack != null){
						callBack.checkSuccess(packageName);
					}
					return;
				}
				try {
					File f = new File("/data/data/"+packageName+"/lib/libivr.so");
					FileInputStream inputStream = null;
					if (f.exists()) {
						inputStream = new FileInputStream("/data/data/"+packageName+"/lib/libivr.so");
					} else {
						inputStream = new FileInputStream("/data/data/"+packageName+"/lib64/libivr.so");
					}
					String result = MD5Util.md5sum(inputStream);
					inputStream.close();
                    isFound = !result.isEmpty();

				}
				catch (Exception e) {
					LogUtil.d("check app invalid 3");
					e.printStackTrace();
					isFound = false;
				}
			}
            if(isFound) {
				if (callBack != null) {
					callBack.checkSuccess(packageName);
				}
			}
            else{
				if(callBack != null){
					callBack.checkFailed(packageName,"INVALID");
				}
			}
		} catch (Exception e) {
			LogUtil.d("check app invalid 4");
			if(callBack != null){
				callBack.checkFailed(packageName,"INVALID");
			}
			e.printStackTrace();
		}
	}
}
