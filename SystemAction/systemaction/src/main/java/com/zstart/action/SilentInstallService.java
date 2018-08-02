package com.zstart.action;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zstart.action.util.BroadcastUtil;
import com.zstart.action.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class SilentInstallService extends IntentService{
	protected String respond = "";
	private ArrayList<String> installingPkgs = null;
	public SilentInstallService() {
		super("SilentInstallService");
		installingPkgs = new ArrayList<String>();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (null != intent) {
			String mess = intent.getStringExtra("data");
			String[] array = mess.split("\\|");
			if(array == null || array.length < 2){
				LogUtil.w("install param invalid that data = "+mess);
				return;
			}
			String pkgName = array[0];
			String path = array[1];
			if (null == path) return;
			File apkFile = new File(path);
			if (null == apkFile || !apkFile.exists()) {
				LogUtil.d("Warning apk file invalid : " + path);
				//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_FAIL,pkgName + "|INVALID");
				return;
			}
			apkFile.setWritable(true);
			//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_BEGIN,pkgName);
			installApk(apkFile,pkgName);
		}
	}
	
	private void installApk(File apkFile,String pkgName) {
		respond = "";
		installingPkgs.add(pkgName);
		updateSharedData();
		String filePath = apkFile.getAbsolutePath();
		String[] temp = filePath.split("/");
		String apkName = temp[temp.length - 1];
		String newFilePath = filePath.replace(apkName, "TMP_" + apkName);
		LogUtil.d("install APK that path = "+filePath+";new path = "+newFilePath + ";write = "+apkFile.canWrite());
		apkFile.canWrite();
		/*if (apkFile.renameTo(new File(newFilePath))) {
			apkFile = new File(newFilePath);
		}
		else
		{
			LogUtil.e("Rename file failed!!" + filePath);
			apkFile = null;
			return;
		}*/
		if (installPackage(apkFile)) {
			if (apkFile.exists()) {
				apkFile.delete();
			}
			//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_SUCCESS, pkgName);
		} 
		else {
			if (respond.contains("INVALID")) {
				//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_FAIL,pkgName + "|INVALID");
			}
			else if (respond.contains("NOSPACE") || respond.contains("STORAGE") || respond.contains("COPY")) {
				//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_FAIL,pkgName + "|NOSPACE");
				//String path = apkFile.getAbsolutePath();
				//String oldPath = path.replace("TPM_", "");
				//apkFile.renameTo(new File(oldPath));
			}
			else {
				//BroadcastUtil.broadcast(this,AppActionProxy.ACTION_INSTALL_FAIL,pkgName+"|"+respond);
			}
		}
		installingPkgs.remove(pkgName);
		updateSharedData();
	}
	
	private void updateSharedData(){
		LogUtil.v("updateSharedData..."+installingPkgs.size());
		SharedPreferences sp = getSharedPreferences(AppActionProxy.SHARED_KEY_MAIN,Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putStringSet(AppActionProxy.SHARED_KEY_SUB, new HashSet<String>(installingPkgs));
		edit.commit();
	}
	
	protected boolean installPackage(File apkFile) {
		if (null == apkFile || !apkFile.exists())
		{
			LogUtil.d("install fail....not find file");
			return false;
		}
		String[] args = { "pm", "install", "-r", apkFile.getAbsolutePath() };
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
				LogUtil.d("install fail length <= 1");
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
}
