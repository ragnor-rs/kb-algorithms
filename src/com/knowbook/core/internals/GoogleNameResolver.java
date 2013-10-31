package com.knowbook.core.internals;

import com.knowbook.core.HttpUtils;
import com.knowbook.core.NameResolver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GoogleNameResolver implements NameResolver {

    private static final String SEARCH_ENGINE_ID = "SEARCH_ENGINE_ID";
    private static final String API_KEY = "API_KEY";

    @Override
    public String resolveProfession(String name) {

        String urlString = HttpUtils.createUrl(
                "https://www.googleapis.com/customsearch/v1",
                "cx", SEARCH_ENGINE_ID,
                "key", API_KEY,
                "q", name
        );

        JSONObject jsonObject;
        try {
            jsonObject = HttpUtils.loadJson(urlString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!jsonObject.has("items")) {
            return name;
        }

        JSONArray itemsJson = jsonObject.getJSONArray("items");
        String link = itemsJson.getJSONObject(0).getString("link");
        String ss = link.substring(link.lastIndexOf("/") + 1);

        String normalizedName;
        try {
            normalizedName = URLDecoder.decode(ss, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return normalizedName;

    }

}
