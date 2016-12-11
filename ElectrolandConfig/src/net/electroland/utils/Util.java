package net.electroland.utils;

import java.util.StringTokenizer;

public class Util {

    public static byte getHiByte(short s)
    {
        return (byte)(s >> 8);
    }

    public static byte getLoByte(short s)
    {
        return (byte)s;
    }

    public static String bytesToHex(byte b)
    {
        return Integer.toHexString((b&0xFF) | 0x100).substring(1,3) + " ";
    }

    public static String bytesToHex(byte[] b, int length)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i< length; i++){
            sb.append(Integer.toHexString((b[i]&0xFF) | 0x100).substring(1,3) + " ");
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static int unsignedByteToInt(byte b) 
    {
        return (int) b & 0xFF;
    }

    public static String consolidateWhiteSpace(String raw, char finalWhiteSpace){
        StringTokenizer st = new StringTokenizer(raw, " \t", false);
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreElements()){
            buffer.append(st.nextToken());
            buffer.append(finalWhiteSpace);
        }
        if (buffer.length() > 0){
            buffer.setLength(buffer.length() - 1);
        }
        return buffer.toString();
    }
}