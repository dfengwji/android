package com.zstart.action;

import android.content.Context;

import com.zstart.action.common.ICallBack;
import com.zstart.action.util.BroadcastUtil;
import com.zstart.action.util.LogUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
			LogUtil.d("silent uninstall app success!!!"+packageName);
			if(callBack != null){
				callBack.uninstallSuccess(packageName);
			}
		}
		else
		{
			LogUtil.d("silent uninstall app fail!!!"+packageName);
			if(callBack != null){
				callBack.uninstallFailed(packageName, result);
			}
		}
	}

	private boolean uninstallPackage(String pkgName) {
		if (null == pkgName || pkgName.isEmpty())
		{
			LogUtil.d("uninstall fail....not package is empty!!!");
			return false;
		}
		String cmd = "pm uninstall " + pkgName;
		LogUtil.d(cmd);
		InputStream errorInput = null;
		InputStream inputStream = null;
		String result = "";
		String error = "";
		Process process = null;
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
			LogUtil.d("uninstall result = " + result+";error = "+error);
		} catch (Exception e) {
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
			LogUtil.d("uninstall fail....");
			return false;
		} else {
			LogUtil.d("uninstall success!");
			return true;
		}
	}

	private boolean uninstallApp(String packageName) {
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
			LogUtil.d("silent uninstall app result = "+result);
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
		if (result != null && (result.endsWith("Success") || result.endsWith("Success\n"))) {
			return true;
		}
		return false;
	}
}
