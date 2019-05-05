package com.idealens.system.status;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Pair;

import com.zstart.action.util.LogUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @file:com.idealens.system.status.StateHelper.java
 * @authoer:huangzizhen
 * @date:2018/1/3
 * @version:V1.0
 */

final class StorageHelper {
    private static final long RESERVED_SIZE = 500 * 1024 * 1024;
    private Context context;
    public StorageHelper(Context con){
        context = con;
    }

    public long getSelfStorage() {
        File file = Environment.getDataDirectory();
        return getSFSize(file.getPath(), false);
    }

    public long getFreeSelfStorage() {
        File file = Environment.getDataDirectory();
        long ret = getSFSize(file.getPath(), true);
        return ret > RESERVED_SIZE ? ret - RESERVED_SIZE : 0;
    }

    /*
    TF Card
    * */
    public long getTFStorage() {
        if (hasExternalStorage()){
            String path = getTFPath();
            if (TextUtils.isEmpty(path)){
                return 0;
            }
            return getSFSize(path, false);
        }
        return 0;
    }

    public long getFreeTFStorage() {
        if (hasExternalStorage()){
            String path = getTFPath();
            if (TextUtils.isEmpty(path)){
                return 0;
            }
            return getSFSize(path, true);
        }
        return 0;
    }

    private boolean hasExternalStorage(){
        return Environment.MEDIA_MOUNTED.equals(getTFState());
    }

    private long getSFSize(String path, boolean free) {
        LogUtil.w("status: getSFSize path = " + path + ", available = " + free);
        StatFs sf = new StatFs(path);
        return free ? sf.getAvailableBytes() : sf.getTotalBytes();
    }

    public Pair<Long,Long> getVolumeStrings(){
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        Pair<Long,Long> pair = new Pair<>(0L,0L);
        try {
            Method method = storageManager.getClass().getMethod("getVolumes");
            Class<?> volumeInfo = Class.forName("android.os.storage.VolumeInfo");
            Method typeMethod = volumeInfo.getMethod("getType");
            Method pathMethod = volumeInfo.getMethod("getPath");
            List list = (List)method.invoke(storageManager);
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                int type = (Integer)typeMethod.invoke(element);
                if(type == 1) { //VolumeInfo.TYPE_PRIVATE
                    File file = (File) pathMethod.invoke(element);
                    long freeBytes = file.getFreeSpace();
                    long totalBytes = 0;
                    if(Build.VERSION.SDK_INT >= 25){
                        Method sizeMethod = storageManager.getClass().getMethod("getPrimaryStorageSize");
                        totalBytes = (Long)sizeMethod.invoke(storageManager);
                    } else {
                        totalBytes = file.getTotalSpace();
                    }
//                    final String free = Formatter.formatFileSize(context, freeBytes);
//                    final String total = Formatter.formatFileSize(context, totalBytes);
//                    result = free + "|" + total;
                    pair = new Pair<>(freeBytes / 1024 / 1024, totalBytes / 1024 / 1024);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pair;
    }

    public Pair<Long,Long> getOTGStorage(){
        Pair<Long,Long> pair = new Pair<>(0L,0L);
        String path = getOTGPath();
        if (!TextUtils.isEmpty(path)){
            long free = getSFSize(path, true);
            long total = getSFSize(path, false);
            pair = new Pair<>(free,total);
        }
        return pair;
    }

    private String getTFPath(){
        /*try {
            Class<?> environment = Class.forName("android.os.Environment");
            Method method = environment.getMethod("getExternalStorageSdDirectory");
            Object o = method.invoke(null);
            File file = (File)o;
            if (file != null){
                return file.getPath();
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return "";
    }

    private String getTFState(){
        /*try {
            Class<?> environment = Class.forName("android.os.Environment");
            Method method = environment.getMethod("getExternalStorageStateSd");
            Object o = method.invoke(null);
            return  (String)o;
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return "";
    }

    private String getOTGPath(){
        /*try{
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while (!TextUtils.isEmpty(line = br.readLine())){
                if(line.contains("extSdCard")){
                    String[] array = line.split(" ");
                    String path = array[1];
                    File file = new File(path);
                    if(file.isDirectory()) {
                        br.close();
                        reader.close();
                        is.close();
                        return path;
                    }
                }
            }
            br.close();
            reader.close();
            is.close();
            return "";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
        try {
            Class<?> environment = Class.forName("android.os.Environment");
            Method method = environment.getMethod("getExternalStorageUsbDirectory");
            Object o = method.invoke(null);
            File file = (File)o;
            if (file != null){
                return file.getPath();
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return "";
    }
}
