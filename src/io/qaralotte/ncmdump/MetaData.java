package io.qaralotte.ncmdump;

import com.google.gson.Gson;

public class MetaData {
    //public long musicId;
    public String musicName;
    public String[][] artist;
    //public long albumId;
    public String album;
    //public String albumPicDocId;
    public String albumPic;
    //public long bitrate;
    //public String mp3DocId;
    // public long duration;
    //public long mvId;
    //public String[] alias;
    //public String[] transNames;
    public String format;

    public static MetaData read_from_json(byte[] json) {
        Gson gson = new Gson();
        return gson.fromJson(new String(json), MetaData.class);
    }
}