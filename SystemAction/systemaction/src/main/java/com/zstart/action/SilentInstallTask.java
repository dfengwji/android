package com.zstart.action;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class SilentInstallTask implements Runnable{
	String pkgName;
	String filePath;
	String respond;
	Context context;
	
	private ArrayList<String> installing;
	private ICallBack callBack;
	public SilentInstallTask(Context context, String packageName, String path, ICallBack fun) {
		this.context = context;
		this.pkgName = packageName;
		this.filePath = path;
		callBack = fun;
		installing = new ArrayList<>();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		File apkFile = new File(filePath);
		if (!apkFile.exists()) {
			LogUtil.d("Warning apk file invalid : " + filePath);
            if(callBack != null){
                callBack.installFailed(pkgName,"NON");
            }
			return;
		}
		apkFile.setWritable(true);
        if(callBack != null){
            callBack.installBegin(pkgName);
        }
		installApk(apkFile,pkgName);
	}

	private void installApk(File apkFile,String pkgName) {
		respond = "";
		installing.add(pkgName);
		updateSharedData();
		String filePath = apkFile.getAbsolutePath();
		String[] temp = filePath.split("/");
		String apkName = temp[temp.length - 1];
		String newFilePath = filePath.replace(apkName, "TMP_" + apkName);
		LogUtil.d("install APK that path = "+filePath+";new path = "+newFilePath + ";write = "+apkFile.canWrite());
		apkFile.canWrite();
		if (apkFile.renameTo(new File(newFilePath))) {
			apkFile = new File(newFilePath);
		}
		else
		{
			LogUtil.e("Rename file failed!!" + filePath);
			apkFile = null;
            if(callBack != null){
                callBack.installFailed(pkgName,"INVALID");
            }
			return;
		}
		if (installPackage(pkgName,apkFile)) {
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
			if (respond.contains("NOSPACE") || respond.contains("STORAGE") || respond.contains("COPY")) {

				//String path = apkFile.getAbsolutePath();
				//String oldPath = path.replace("TPM_", "");
				//apkFile.renameTo(new File(oldPath));
			}
		}
		installing.remove(pkgName);
		updateSharedData();
	}
	
	private void updateSharedData(){
		LogUtil.v("updateSharedData..."+ installing.size());
		SharedPreferences sp = this.context.getSharedPreferences(AppActionProxy.SHARED_KEY_MAIN,Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putStringSet(AppActionProxy.SHARED_KEY_SUB, new HashSet<String>(installing));
		edit.commit();
	}
	
	protected boolean installPackage(String pkgName, File apkFile) {
		if (null == apkFile || !apkFile.exists())
		{
			LogUtil.d("install fail....not find file");
			return false;
		}
		//String[] args = { "pm", "install", "-r","-i",pkgName,"-user 0", apkFile.getAbsolutePath() };
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
