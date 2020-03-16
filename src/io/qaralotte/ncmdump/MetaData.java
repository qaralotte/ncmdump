package io.qaralotte.ncmdump;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class MetaData {
    public String musicName;
    public String[][] artist;
    public String album;
    public String format;

    public static MetaData read_from_json(byte[] json) {
        Gson gson = new Gson();
        return gson.fromJson(new String(json, StandardCharsets.UTF_8), MetaData.class);
    }
}