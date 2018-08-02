package com.zstart.action;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.zstart.action.model.AppInfo;
import com.zstart.action.util.BitmapUtil;

import java.util.HashMap;

public class LauncherIconHelper {
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

	private static class CacheEntry {
		public Drawable icon;
	}

	private final Drawable defaultIcon;
	private final Context context;
	private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
			INITIAL_ICON_CACHE_CAPACITY);
	private int iconDPI;

	private PackageManager mPackageManager;

	public LauncherIconHelper(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		this.context = context;
		mPackageManager = context.getPackageManager();
		iconDPI = activityManager.getLauncherLargeIconDensity();

		// need to set mIconDpi before getting default icon
		defaultIcon = makeDefaultIcon();
	}

	public Drawable getFullResDefaultActivityIcon() {
		return getFullResIcon(Resources.getSystem(),
				android.R.mipmap.sym_def_app_icon);
	}

	public Drawable getFullResIcon(Resources resources, int iconId) {
		Drawable d;
		try {
			d = resources.getDrawableForDensity(iconId, iconDPI);
			//d = context.getDrawable(iconId);
		} catch (Resources.NotFoundException e) {
			d = null;
		}
		return (d != null) ? d : getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(String packageName, int iconId) {
		Resources resources;
		try {
			resources = mPackageManager.getResourcesForApplication(packageName);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(ResolveInfo info) {
		return getFullResIcon(info.activityInfo);
	}

	public Drawable getFullResIcon(ActivityInfo info) {

		Resources resources;
		try {
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return getFullResDefaultActivityIcon();
	}

	private Drawable makeDefaultIcon() {
		Drawable d = getFullResDefaultActivityIcon();
		return d;
	}

	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(ComponentName componentName) {
		synchronized (mCache) {
			mCache.remove(componentName);
		}
	}

	/**
	 * Empty out the cache.
	 */
	public void flush() {
		synchronized (mCache) {
			mCache.clear();
		}
	}

	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public Drawable getIcon(AppInfo app, ResolveInfo info) {
		synchronized (mCache) {
			CacheEntry entry = cacheLocked(app.componentName, info);
			return entry.icon;
		}
	}

	public Drawable getIcon(Intent intent) {
		synchronized (mCache) {
			final ResolveInfo resolveInfo = mPackageManager.resolveActivity(
					intent, 0);
			ComponentName component = intent.getComponent();

			if (resolveInfo == null || component == null) {
				return defaultIcon;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo);
			return entry.icon;
		}
	}

	public Drawable getIcon(ComponentName component, ResolveInfo resolveInfo) {
		synchronized (mCache) {
			if (resolveInfo == null || component == null) {
				return null;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo);
			return entry.icon;
		}
	}

	public boolean isDefaultIcon(Drawable icon) {
		return defaultIcon == icon;
	}

	private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info) {
		CacheEntry entry = mCache.get(componentName);
		if (entry == null) {
			entry = new CacheEntry();

			mCache.put(componentName, entry);
			entry.icon = new FastBitmapDrawable(BitmapUtil.createIconBitmap(
					getFullResIcon(info.activityInfo), context));
		}
		return entry;
	}

	public HashMap<ComponentName, Drawable> getAllIcons() {
		synchronized (mCache) {
			HashMap<ComponentName, Drawable> set = new HashMap<ComponentName, Drawable>();
			for (ComponentName cn : mCache.keySet()) {
				final CacheEntry e = mCache.get(cn);
				set.put(cn, e.icon);
			}
			return set;
		}
	}
}
