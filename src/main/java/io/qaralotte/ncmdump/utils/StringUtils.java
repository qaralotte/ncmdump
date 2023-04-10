package io.qaralotte.ncmdump.utils;

import java.nio.charset.StandardCharsets;

public class StringUtils {

    // Encoding UTF-8
    public static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
