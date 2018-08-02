package com.zstart.action.util;

import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.zstart.action.FastBitmapDrawable;

import java.io.ByteArrayOutputStream;

public class APKUtil {
	
	public static byte[] getBytesFromDrawable(Drawable icon) {
		Bitmap bitmap = ((FastBitmapDrawable) icon).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	public static ComponentName getComponentFromResolveInfo(ResolveInfo info) {
		if(info == null)
			return null;
		if (info.activityInfo != null) {
			return new ComponentName(info.activityInfo.packageName,
					info.activityInfo.name);
		} else {
			return new ComponentName(info.serviceInfo.packageName,
					info.serviceInfo.name);
		}
	}
}
