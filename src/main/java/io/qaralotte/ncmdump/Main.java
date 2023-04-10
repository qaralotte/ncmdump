package io.qaralotte.ncmdump;

import io.qaralotte.ncmdump.dump.NcmDump;
import io.qaralotte.ncmdump.utils.ErrorUtils;
import io.qaralotte.ncmdump.utils.StringUtils;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            ErrorUtils.error("No input .ncm File");
        } else {
            for (String arg : args) {
                File file = new File(arg);
                NcmDump ncmDump = new NcmDump(file);
                ncmDump.execute();
            }
        }
    }
}
