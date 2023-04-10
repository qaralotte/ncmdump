package io.qaralotte.ncmdump.dump;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.qaralotte.ncmdump.utils.StringUtils;

import java.util.Arrays;

public class MetaData {

    private final JSONObject metaDataJson;

    public MetaData(byte[] metaDataBytes) {
        String metaDataStr = StringUtils.toString(metaDataBytes);
        this.metaDataJson = JSON.parseObject(metaDataStr);
    }

    public JSONObject getJson() {
        return metaDataJson;
    }

    public String[] getArtistsName() {
        JSONArray artists = metaDataJson.getJSONArray("artist");
        String[] artistsName = new String[artists.size()];
        for (int i = 0; i < artists.size(); ++i) {
            artistsName[i] = artists.getJSONArray(i).getString(0);
        }
        return artistsName;
    }

    @Override
    public String toString() {

        return "=> Music Name: " + metaDataJson.getString("musicName") + "\n" +
                "=> Artists: " + Arrays.toString(getArtistsName()) + "\n" +
                "=> Album: " + metaDataJson.getString("album") + "\n" +
                "=> Bitrate: " + metaDataJson.getInteger("bitrate") + "\n" +
                "=> Format: " + metaDataJson.getString("format");
    }
}
