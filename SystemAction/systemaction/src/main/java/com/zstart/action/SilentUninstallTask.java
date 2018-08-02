package com.zstart.action;

import android.content.Context;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.BroadcastUtil;
import com.zstart.action.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class SilentUninstallTask implements Runnable{
	String packageName;
	String result;
	Context context;
	ICallBack callBack;
	public SilentUninstallTask(Context context, String packageName, ICallBack fun) {
		this.context = context;
		this.packageName = packageName;
		this.callBack = fun;
	}

	@Override
	public void run() {
		result = "";
		if (uninstallApp(packageName))
		{
			LogUtil.d("slient unistall app success!!!"+packageName);
			if(callBack != null){
				callBack.uninstallSuccess(packageName);
			}
		}
		else
		{
			LogUtil.d("slient unistall app fail!!!"+packageName);
			if(callBack != null){
				callBack.uninstallFailed(packageName, result);
			}
		}
	}

	public boolean uninstallApp(String packageName) {
		String[] args = { "pm", "uninstall", packageName };
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
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
			result = new String(data);

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
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		if (result != null
				&& (result.endsWith("Success") || result.endsWith("Success\n"))) {
			return true;
		}
		return false;
	}
}
