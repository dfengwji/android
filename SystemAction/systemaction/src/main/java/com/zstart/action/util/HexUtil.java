package com.zstart.action.util;

public final class HexUtil {
    public static final String MD5_String = "B9D8FB6FD354C36443386F3C54F19017";
   
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(bytes, offset, tmp, 0, length);
        return bytesToHex(tmp);
    }
}
