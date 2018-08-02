package com.zstart.action.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

public class FileUtil {
	public static Boolean isExternalStorageAvailable(){
		return Environment.getExternalStorageDirectory().equals(Environment.MEDIA_MOUNTED);
	}
	
	public static String getSDCardPath(){
		Boolean exist = isExternalStorageAvailable();
		if(exist)
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		else
			return  "";
	}
	
	public static Boolean isFileExist(String path){
		if(path == null || path.isEmpty())
			return false;
		try{
			File file = new File(path);
			return file.exists();
		}catch(Exception e){
			LogUtil.w(e.getMessage());
			return false;
		}
	}

	public  static String getResourcePath(String fileName){
		String path = Environment.getExternalStorageDirectory().getPath() +"/"
				 + fileName;
		return path;
	}
	
	public static void createDirectorys(String path){
		if(path == null || path.isEmpty())
			return;
		try{
			LogUtil.w("create directory....."+path);
            StringTokenizer st = new StringTokenizer(path,"/");
            String path1 = st.nextToken() + "/";
            String path2 = path1;
            while (st.hasMoreTokens()) {
                path1 = st.nextToken() + "/";
                path2 += path1;
                File file = new File(path2);
                if (!file.exists())
                    file.mkdir();
            }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void createDirectory(String path){
		if(path == null || path.isEmpty())
			return;
		try{
			LogUtil.w("create directory....."+path);
			File file = new File(path);
			if (!file.exists())
				file.mkdir();
		}catch (Exception e){
			LogUtil.v(e.toString());
		}
	}
	
	public static void deleteFile(String path){
		if(path == null || path.isEmpty())
			return;
		try{
			File file = new File(path);
            if(file.exists()){
                LogUtil.d("delete file that path = "+path);
                file.delete();
            }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    public static void deleteDirectory(String path){
        try{
            File file = new File(path);
            deleteFile(file);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

	public static void deleteChildrenFiles(String path){
		if(path == null || path.isEmpty())
			return;
		try{
			File file = new File(path);
			if(!file.exists())
				return;
			if(file.isFile()) {
                LogUtil.d("delete file....."+file.getName());
				file.delete();
				return;
			}
			if(file.isDirectory()){
				File[] children = file.listFiles();
				if(children == null || children.length < 1){
					return;
				}
				for(int i = 0;i < children.length;i++){
					deleteFile(children[i]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void deleteFile(File file){
		try{
			if(file == null || !file.exists())
				return;
			LogUtil.d("delete file....."+file.getName());
			if(file.isFile()) {
				file.delete();
				return;
			}
			if(file.isDirectory()){
				File[] children = file.listFiles();
				if(children == null || children.length < 1){
					file.delete();
					return;
				}
				for(int i = 0;i < children.length;i++){
					deleteFile(children[i]);
				}
				file.delete();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static byte[] readFile(String path){
		if(path == null || path.isEmpty())
			return null;
		try{
			File file = new File(path);
			if(!file.exists())
				return null;
			FileInputStream input = new FileInputStream(file);
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			input.close();
			return buffer;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static String readString(String path){
		if(path == null || path.isEmpty())
			return "";
		try{
			File file = new File(path);
			if(!file.exists())
				return "";
			FileInputStream input = new FileInputStream(file);
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			String mess = new String(buffer, "UTF-8");
			input.close();
			return mess;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	public static Boolean writeFile(String path,String message){
		if(path == null || path.isEmpty())
			return false;
		try{
			File file = new File(path);
			if(file.exists())
				file.delete();
			FileOutputStream output = new FileOutputStream(file);
			byte[] bytes = message.getBytes();
			output.write(bytes);
			output.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean writeFile(String path,byte[] data){
		if(path == null || path.isEmpty())
			return false;
		try{
            LogUtil.v("write file..."+path);
			File file = new File(path);
			if(file.exists())
				file.delete();
			FileOutputStream output = new FileOutputStream(file);
			output.write(data);
			output.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static long getFreeDiskSpace(){
		try {
            StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return fs.getAvailableBytes();
		}catch (Exception e){
			LogUtil.w(e.toString());
			return 0;
		}
	}

	public static long getTotalDiskSpace(){
		try {
            StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return fs.getTotalBytes();
		}catch (Exception e){
			LogUtil.w(e.toString());
			return 0;
		}
	}
}
