package com.zstart.action.model;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo {
	public CharSequence title = "";
	public Intent intent;
	public Drawable icon;
	public byte[] iconData = new byte[1];
	public int iconWidth = 0;
	public int iconHeight = 0;
	public long firstAddedTime;
	public int type;
	public ComponentName componentName;
	public long lastUpdatedTime;
	public String sourceLocation;
	public String packageName;
	public String version;
	public int versionCode;
	public long size;
	public boolean isHome;
}
