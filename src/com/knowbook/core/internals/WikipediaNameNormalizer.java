package com.knowbook.core.internals;

import com.knowbook.core.HttpUtils;
import com.knowbook.core.NameNormalizer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class WikipediaNameNormalizer implements NameNormalizer {

    @Override
    public String normalizeProfessionName(String name) {

        String urlString;

        urlString = HttpUtils.createUrl(
                "http://ru.wikipedia.org/w/api.php",
                "format", "json",
                "action", "query",
                "list", "search",
                "srsearch", name,
                "srprop", "score",
                "srwhat", "text"
        );

        URL searchProfessionUrl;
        try {
            searchProfessionUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        JSONObject resultJson;
        try {
            resultJson = HttpUtils.loadJson(searchProfessionUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONArray searchArray = resultJson.getJSONObject("query").getJSONArray("search");
        if (searchArray.length() == 0) {
            return name;
        }

        return searchArray.getJSONObject(0).getString("title");

    }

    @Override
    public String getProfessionUrl(String normalizedName) {
        return HttpUtils.createUrl(
                "http://ru.wikipedia.org/w/index.php",
                "title", normalizedName
        );
    }

}
