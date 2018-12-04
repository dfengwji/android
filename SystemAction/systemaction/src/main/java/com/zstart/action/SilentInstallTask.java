package com.zstart.action;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

public class SilentInstallTask implements Runnable{
	private String appPkg;
	private String appPath;
	private String respond;
	private Context context;
	private String selfPkg;
	private ArrayList<String> installing;
	private ICallBack callBack = null;
	public SilentInstallTask(Context context,String self, String appPkg, String path, ICallBack fun) {
		this.context = context;
		this.selfPkg = self;
		this.appPkg = appPkg;
		this.appPath = path;
		callBack = fun;
		installing = new ArrayList<>();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		File apkFile = new File(appPath);
		if (!apkFile.exists()) {
			LogUtil.d("Warning apk file invalid : " + appPath);
            if(callBack != null){
                callBack.installFailed(appPkg,"NON");
            }
			return;
		}
		apkFile.setWritable(true);
        if(callBack != null){
            callBack.installBegin(appPkg);
        }
		tryInstall(apkFile,appPkg,this.selfPkg);
	}

	private synchronized void tryInstall(File apkFile,String pkgName,String self) {
		respond = "";
		installing.add(pkgName);
		updateSharedData();
		LogUtil.d("install APK that path = "+appPath + ";write = "+apkFile.canWrite());
		apkFile.canWrite();
		boolean success = installFile(self, apkFile);
		LogUtil.e("install apk success = " + success);
		if (success) {
			if (apkFile.exists()) {
				apkFile.delete();
			}
            if(callBack != null){
                callBack.installSuccess(pkgName);
            }
		} 
		else {
            if(callBack != null){
                callBack.installFailed(pkgName,respond);
            }
		}
		installing.remove(pkgName);
		updateSharedData();
	}
	
	private void updateSharedData(){
		LogUtil.v("updateSharedData..."+ installing.size());
		SharedPreferences sp = this.context.getSharedPreferences(AppActionProxy.SHARED_KEY_MAIN,Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putStringSet(AppActionProxy.SHARED_KEY_SUB, new HashSet<>(installing));
		edit.apply();
	}

    private boolean installFile(String self, File apkFile) {
        if (null == apkFile)
        {
            LogUtil.d("install fail....not find file");
            return false;
        }
        String cmd = "pm install -r -d -i "+self+" --user 0 " + apkFile.getAbsolutePath();
        LogUtil.d(cmd);
        InputStream errorInput = null;
        InputStream inputStream = null;
        String result = "";
        Process process = null;
        String error = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            errorInput = process.getErrorStream();
            inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader = new BufferedReader(new InputStreamReader(errorInput));
            while ((line = reader.readLine()) != null) {
                error += line;
            }
            respond = result;
            LogUtil.d("install result = " + result+";error = "+error);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (errorInput != null) {
                    errorInput.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        if (result == null || !"Success".equals(result)) {
            LogUtil.d("install fail....");
            return false;
        } else {
            LogUtil.d("install success!");
            return true;
        }
    }

	private boolean installPackage(String pkgName, File apkFile) {
		if (null == apkFile || !apkFile.exists())
		{
			LogUtil.d("install fail....not find file");
			return false;
		}
		String[] args = { "pm", "install", "-r","-i",pkgName,"--user 0", apkFile.getAbsolutePath() };
		//String[] args = { "pm", "install", "-r", apkFile.getAbsolutePath() };
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		String result = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			respond = new String(data);
			LogUtil.d("install state:" + respond);
			String[] temp = respond.split("\n");
			
			if (temp.length <= 1) {
				LogUtil.d("install fail respond length <= 1");
			} else {
				result = temp[temp.length - 1];
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		
		if (result == null || !"Success".equals(result)) {
			LogUtil.d("install fail....");
			return false;
		} else {
			LogUtil.d("install success!");
			return true;
		}
    }
/*
	private int PER_USER_RANGE = 100000;
	private int getUserId(int uid) {
		return uid / PER_USER_RANGE;
	}

	private Class<?>[] getParamTypes(Class<?> cls, String mName) {
		try {
			Class<?> cs[] = null;
			Method[] mtd = cls.getMethods();
			for (int i = 0; i < mtd.length; i++) {
				if (!mtd[i].getName().equals(mName)) {
					continue;
				}
				cs = mtd[i].getParameterTypes();
			}
			return cs;
		}catch(Exception e){
			return null;
		}

	}

	private synchronized boolean installAPK(String packageName,File apkFile){
		if (null == apkFile || !apkFile.exists())
		{
			LogUtil.d("install apk fail....not find file");
			return false;
		}
		LogUtil.d("install file..."+apkFile.getAbsolutePath());
		try {
			Class<?> activityThread = Class.forName("android.app.ActivityThread");
			Class<?> paramTypes[] = getParamTypes(activityThread, "getPackageManager");
			Method method1 = activityThread.getMethod("getPackageManager", paramTypes);
			Object PackageManagerService = method1.invoke(activityThread);
			Class<?> pmService = PackageManagerService.getClass();
			Class<?> paramTypes1[] = getParamTypes(pmService, "installPackageAsUser");
			Method method = pmService.getMethod("installPackageAsUser", paramTypes1);
			method.invoke(PackageManagerService, apkFile.getAbsolutePath(), null, 2, packageName, getUserId(android.os.Binder.getCallingUid()));//getUserId
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}*/
}
