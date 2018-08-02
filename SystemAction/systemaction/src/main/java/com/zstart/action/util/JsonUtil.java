package com.zstart.action.util;

import com.zstart.action.model.AppInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtil {
	public static String getInstalledJSON(ArrayList<AppInfo> apps) {
		if(apps == null)
			return "";
		String mess = "";
		try {
			JSONArray json = new JSONArray();
			for (int i = 0; i < apps.size(); i++) {
				JSONObject object = new JSONObject();
				AppInfo info = apps.get(i);
				object.put("index", String.valueOf(i));
				object.put("name", info.title.toString());
				object.put("package", info.packageName);
				object.put("sourcelocation", info.sourceLocation);
				object.put("installTime", info.firstAddedTime / 1000L);
				object.put("lastUpdateTime", info.lastUpdatedTime / 1000L);
				object.put("version", info.version);
				object.put("versionCode", info.versionCode);
				object.put("size", info.size);
				object.put("width", info.iconWidth);
				object.put("height", info.iconHeight);
				json.put(object);
			}

			mess = json.toString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return mess;
	}
	
	public static String getAppJson(AppInfo info){
		if(info == null)
			return "";
		try{
			JSONObject object = new JSONObject();
			object.put("index",0);
			object.put("name", info.title.toString());
			object.put("package", info.packageName);
			object.put("sourcelocation", info.sourceLocation);
			object.put("installTime", info.firstAddedTime / 1000L);
			object.put("lastUpdateTime", info.lastUpdatedTime / 1000L);
			object.put("version", info.version);
			object.put("versionCode", info.versionCode);
			object.put("size", info.size);
			object.put("width", info.iconWidth);
			object.put("height", info.iconHeight);
			return object.toString();
		}catch (Exception e){
			return "";
		}
	}
}
