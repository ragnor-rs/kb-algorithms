package com.knowbook.core.internals;

import com.knowbook.core.HttpUtils;
import com.knowbook.core.NameResolver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WikipediaNameResolver implements NameResolver {

    @Override
    public String resolveProfession(String name) {

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

        JSONObject resultJson;
        try {
            resultJson = HttpUtils.loadJson(urlString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONArray searchArray = resultJson.getJSONObject("query").getJSONArray("search");
        if (searchArray.length() == 0) {
            return name;
        }

        return searchArray.getJSONObject(0).getString("title");

    }

}
