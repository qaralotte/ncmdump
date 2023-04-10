package io.qaralotte.ncmdump.utils;

public class ErrorUtils {

    public static void error(String reason, String detail) {
        System.out.println("Error");
        System.out.println("=> " + reason);

        if (!detail.isEmpty()) {
            System.out.println("=> " + detail);
        }

        System.exit(1);
    }

    public static void error(String reason) {
        error(reason, "");
    }

}
