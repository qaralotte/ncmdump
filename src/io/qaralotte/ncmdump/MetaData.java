package io.qaralotte.ncmdump;

import com.google.gson.Gson;

public class MetaData {
    public String musicName;
    public String[][] artist;
    public String album;
    public String format;

    public static MetaData read_from_json(byte[] json) {
        Gson gson = new Gson();
        return gson.fromJson(new String(json), MetaData.class);
    }
}