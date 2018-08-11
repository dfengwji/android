package com.zstart.action.util;

import android.content.Context;
import android.content.Intent;

import com.zstart.action.AppActionProxy;

public final class BroadcastUtil {
	public static void broadcast(Context context, String action, String param){
		LogUtil.w("broadcast action = "+action+"; param = "+param);
		Intent intent = new Intent(action);
		//intent.putExtra(AppActionProxy.EXTRA_DATA_STRING, param);
		context.sendBroadcast(intent);
	}
}
