package com.zstart.action.util;

import android.util.Log;

public final class LogUtil {
	private static final String TAG = "ZApp";
	public static boolean isDebug = true;

	public static void i(String msg) {
		if (isDebug)
			Log.i(TAG, msg);
	}

	public static void d(String msg) {
		if (isDebug)
			Log.d(TAG, msg);
	}

	public static void e(String msg) {
		if (isDebug)
			Log.e(TAG, msg);
	}

	public static void v(String msg) {
		if (isDebug)
			Log.v(TAG, msg);
	}

	public static void w(String msg) {
		if (isDebug)
			Log.w(TAG, msg);
	}
}
