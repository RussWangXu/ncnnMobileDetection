package com.iray.infiray_lt_m3_sdk.utils;

public class ConverUtils {
    public static short convertByte2Short(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        short r = 0;
        if (bBigEnding) {
            for (int i = 0; i < 2; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        } else {
            for (int i = 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        }

        return r;
    }

    public static int convertByte2Int(byte[] buf) {
        int r = 0;
        for (int i = buf.length - 1; i >= 0; i--) {
            r <<= 8;
            r |= (buf[i] & 0x000000ff);
        }
        return r;
    }

    public static byte[] converInt2Byte(int val) {
        byte[] result = new byte[4];
        result[0] = (byte) (val & 0xff);
        result[1] = (byte) ((val >> 8) & 0xff);
        result[2] = (byte) ((val >> 16) & 0xff);
        result[3] = (byte) ((val >> 24) & 0xff);
        return result;
    }

    public static byte[] convertShort2Byte(short val) {
        byte[] result = new byte[2];
        result[0] = (byte) (val & 0xff);
        result[1] = (byte) ((val >> 8) & 0xff);
        return result;
    }
}
