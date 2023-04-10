package io.qaralotte.ncmdump.utils;

import java.io.*;

public class StreamUtils {

    public static int readBytes(FileInputStream fis, byte[] b) {
        try {
            return fis.read(b);
        } catch (IOException e) {
            ErrorUtils.error("No more bytes could read");
        }
        return -1;
    }

    public static void skipN(FileInputStream fis, long n) {
        try {
            fis.skip(n);
        } catch (IOException e) {
            ErrorUtils.error("No more bytes could skip");
        }
    }

    // Write bytes in File
    public static void writeBytes(File dest, byte[] data) {

        // Overwrite
        try {
            if (dest.exists()) {
                dest.delete();
            }
        } catch (SecurityException e) {
            ErrorUtils.error("No permission to overwrite dumped files", "Path: " + dest.getAbsolutePath());
        }

        try {
            FileOutputStream fos = new FileOutputStream(dest);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            ErrorUtils.error("Write bytes failed", "Path: " + dest.getAbsolutePath());
        }
    }

}
