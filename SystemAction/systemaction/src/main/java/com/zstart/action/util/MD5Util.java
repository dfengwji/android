package com.zstart.action.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Util { 
    public static String md5sum(InputStream is){
    	if(is == null)
    		return "";
        byte[] buffer = new byte[1024];
        int numRead =0;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            while((numRead=is.read(buffer))>0){
                md5.update(buffer, 0, numRead);
            }
            is.close();
            return HexUtil.bytesToHex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static byte[] calculateFileChecksumBytes(String absFilePath) {
        try {

            File file = new File(absFilePath);
            if(!file.exists()) {
                LogUtil.v("MD5 check failed that file not found!!! path = "+absFilePath);
                return null;
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int numRead = -1;
            do {
                numRead = is.read(buffer);
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            return md.digest();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.w("getFileChecksumString ...exception = "+e.getMessage());
        }
        return null;
    }

    public static String getFileChecksumString(String absFilePath) {
        byte[] md5Bytes = calculateFileChecksumBytes(absFilePath);
        if (null == md5Bytes) {
            return "";
        }
        return HexUtil.bytesToHex(md5Bytes).toUpperCase();
    }
}
