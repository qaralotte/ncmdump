package io.qaralotte.ncmdump;

import java.io.File;

public class Executor {

    public static void main(String[] args) throws Exception {

        //find ncm file;
        if (args.length == 0) {
            throw new Exception("no ncm file");
        } else {
            for (String arg : args) {
                File ncm_f = new File(arg);
                Dump dump = new Dump(ncm_f);
                dump.execute();
            }
        }
    }
}
