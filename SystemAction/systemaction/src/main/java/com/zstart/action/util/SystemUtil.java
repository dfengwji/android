package com.zstart.action.util;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.PowerManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
public final class SystemUtil {
     public static void clearMemory(Context context) {
         try {
             ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
             List<ActivityManager.RunningAppProcessInfo> infoList = actManager.getRunningAppProcesses();

             if (infoList == null)
                 return;
             long beforeMem = getAvailableMemory(context);
             LogUtil.d("system memory before memory = " + beforeMem);
             int count = 0;
             for (int i = 0; i < infoList.size(); i++) {
                 ActivityManager.RunningAppProcessInfo info = infoList.get(i);
                 //LogUtil.w("process name = "+info.processName + ";importance = "+info.importance);
                 if (info.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                     String[] pkgList = info.pkgList;
                     for (int j = 0; j < pkgList.length; j++) {
                         LogUtil.d("will kill process that name = " + pkgList[j]);
                         actManager.killBackgroundProcesses(pkgList[j]);
                         count++;
                     }
                 }
             }
             long afterMem = getAvailableMemory(context);
             LogUtil.d("system clear that kill = " + count + " process and release " + (afterMem - beforeMem) / 1024 + " kb memory");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

    public static void clearMemory(Context context,List<String> taskPkgList){
        if(taskPkgList == null || taskPkgList.size() < 1)
            return;
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> infoList = actManager.getRunningAppProcesses();

            if (infoList == null)
                return;
            long beforeMem = getAvailableMemory(context);
            int count = 0;
            for (int i = 0; i < infoList.size(); i++) {
                ActivityManager.RunningAppProcessInfo info = infoList.get(i);
                //LogUtil.w("process name = "+info.processName + ";importance = "+info.importance);
                if (info.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = info.pkgList;
                    for (int j = 0; j < pkgList.length; j++) {
                        if (taskPkgList.contains(pkgList[j])) {
                            LogUtil.d("system clear that will kill process of = " + pkgList[j]);
                            //actManager.killBackgroundProcesses(pkgList[j]);
                            Method method = Class.forName("android.app.ActivityManager")
                                    .getMethod("forceStopPackage", String.class);
                            method.invoke(actManager,pkgList[j]);
                            count++;
                        }
                    }
                }
            }
            long afterMem = getAvailableMemory(context);
            LogUtil.d("system clear that kill = " + count + " process and release " + (afterMem - beforeMem) / 1024 + " kb memory");
        }catch (Exception e){
            LogUtil.e(e.toString());
        }
    }
    
    public static void clearMemoryExcept(Context context,String exceptPkg){
        try {
        	if(TextUtils.isEmpty(exceptPkg))
        		return;
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> infoList = actManager.getRunningAppProcesses();

            if (infoList == null)
                return;
            long beforeMem = getAvailableMemory(context);
            int count = 0;
            for (int i = 0; i < infoList.size(); i++) {
                ActivityManager.RunningAppProcessInfo info = infoList.get(i);
                //LogUtil.w("process name = "+info.processName + ";importance = "+info.importance);
                if (info.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = info.pkgList;
                    for (int j = 0; j < pkgList.length; j++) {
                        if (!exceptPkg.equals(pkgList[j])) {
                            LogUtil.d("system clear that will kill process of = " + pkgList[j]);
                            //actManager.killBackgroundProcesses(pkgList[j]);
                            Method method = Class.forName("android.app.ActivityManager")
                                    .getMethod("forceStopPackage", String.class);
                            method.invoke(actManager,pkgList[j]);
                            count++;
                        }
                    }
                }
            }
            long afterMem = getAvailableMemory(context);
            LogUtil.d("system clear that kill = " + count + " process and release " + (afterMem - beforeMem) / 1024 + " kb memory");
        }catch (Exception e){
            LogUtil.e(e.toString());
        }
    }

    public static void forceStopPackage(Context context,String pkgName){
    	 try{
             ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
             List<ActivityManager.RunningAppProcessInfo> runningList = actManager.getRunningAppProcesses();
             for (ActivityManager.RunningAppProcessInfo info : runningList) {
                 String[] pkgStrings = info.pkgList;
                 for (int i = 0; i < pkgStrings.length; i++) {
                     if (pkgStrings[i].equals(pkgName)){
                         Method method = Class.forName("android.app.ActivityManager")
                                 .getMethod("forceStopPackage", String.class);
                         method.invoke(actManager,pkgName);
                         return;
                     }
                 }
             }
         }catch(Exception e){
             LogUtil.e(e.toString());
         }
    }
    
    public static void startMediaScanner(File file, Context context) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		context.sendBroadcast(intent);
	}

    public static long getAvailableMemory(Context context){
        ActivityManager actManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(info);
        return info.availMem;
    }

    public static void checkSystemMemory(Context context,String pkgName){
        try {
            ActivityManager actManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = actManager.getRunningAppProcesses();
            for(ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos){
                //int uid = appProcessInfo.uid;
                String progressName = appProcessInfo.processName;
                if(progressName.equals(pkgName)) {
                    int pid = appProcessInfo.pid;
                    int[] array = new int[]{pid};
                    Debug.MemoryInfo[] memoryInfos = actManager.getProcessMemoryInfo(array);
                    LogUtil.d("system clear : progress name = " + progressName + ";pid = " + pid
                            + ";private = " + memoryInfos[0].getTotalPrivateDirty()
                            + "kb;PSS = " + memoryInfos[0].getTotalPss()+" kb!!!");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getForegroundPackage(Context context){
        String topPackageName = null;
        UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1 * 10, time);
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (runningTask.isEmpty()) {
                return null;
            }
            topPackageName =  runningTask.get(runningTask.lastKey()).getPackageName();
        }
        return topPackageName;

        /* try{
             ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
             List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(5);


             List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
             if (processes != null){
                 for(ActivityManager.RunningAppProcessInfo info:processes){
                     //LogUtil.w("isRunning....."+info.processName + "---"+ pkgName);
                     if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                         return info.processName;
                 }
             }
         }catch (Exception e){
             e.printStackTrace();
         }
        return "";*/
    }

    public static void forceStopExcept(Context context, ArrayList<String> pkgNames){
        String pkg = getForegroundPackage(context);
        LogUtil.i("forceStopExcept...."+pkg);
        if(pkgNames.contains(pkg))
            return;
        try{
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(am, pkg);
        }catch(Exception e){
            LogUtil.w(e.toString());
        }
    }

    public static boolean isRunning(Context context, String pkgName) {
        try {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runnings = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runnings) {
                String[] pkgStrings = runningAppProcessInfo.pkgList;
                for (int i = 0; i < pkgStrings.length; i++) {
                    if (pkgStrings[i].equals(pkgName)){
                        return true;
                    }
                }
            }
            LogUtil.w("isRunning....false...."+pkgName);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRunningForeground(Context context, String pkgName){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runnings = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo info:runnings){
            //LogUtil.w("isRunning....."+info.processName + "---"+ pkgName);
            if(info.processName.equals(pkgName)){
                if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
	    
    /*
     * 获取SN
     */
    public static String getSerialNumber(Context context) {
        try
        {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getDeclaredMethod("get",String.class);
            return (String) method.invoke(classType,"ro.boot.serialno");
        }catch (Exception e) {
            LogUtil.e(e.toString());
            return "";
        }
	}
    
    /*
     * 获取Rom版本
     */
    public static String getROMVersion() {
        String ret = "";
        try {
            Class<?> sys = Class.forName("android.os.SystemProperties");
            Method get = sys.getMethod("get", String.class);
            ret += (String) get.invoke(sys, "ro.build.display.id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void setSystemProperty(String key,String val) {
        try
        {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getMethod("set",String.class,String.class);
            method.invoke(classType,key,val);
        }catch (Exception e) {
            LogUtil.e(e.toString());
        }
    }

    public static String getSystemProperty(String key,String val) {
        String value = val;
        try
        {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getMethod("get",String.class,String.class);
            value = (String) method.invoke(classType,key,"unknown");
        }catch (Exception e) {
            LogUtil.e(e.toString());
        }
        return value;
    }

    public static String getMacAddress(Context context) {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getIPAddress(Context context){
        InetAddress ip = null;
        try {
            //列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = en_netInterface.nextElement();//得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(ip == null){
            return "";
        }
        return ip.toString();
    }

    public static String intToIP(int ip){
        return (ip & 0xFF)+"."+((ip >> 8) & 0xFF) + "." +((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    public static void reboot(Context context){
        try{
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            powerManager.reboot(null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /*
     * 关机接口
     */
    public static boolean shutDown() {
        try {
            Class<?> serviceMgr = Class.forName("android.os.ServiceManager");
            Method getServerMethod = serviceMgr.getMethod("getService", java.lang.String.class);
            Object oRemoteObject = getServerMethod.invoke(null, Context.POWER_SERVICE);
            Class<?> stub = Class.forName("android.os.IPowerManager$Stub");
            Method asInterface = stub.getMethod("asInterface", android.os.IBinder.class);
            Object object = asInterface.invoke(null, oRemoteObject);
            if(Build.VERSION.SDK_INT < 24){
                Method shutdown = object.getClass().getMethod("shutdown", boolean.class, boolean.class);
                shutdown.invoke(object, false, true);
            }else {
                Method shutdown = object.getClass().getMethod("shutdown", boolean.class,String.class, boolean.class);
                shutdown.invoke(object, false,"", true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
    
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        id = id.substring(0, 15);
        out.write(id.getBytes());
        out.close();
    }
}
