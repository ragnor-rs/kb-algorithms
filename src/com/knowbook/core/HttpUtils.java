package com.knowbook.core;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class HttpUtils {

    public static String createUrl(String uri, String... params) {
        StringBuilder stringBuilder = new StringBuilder(uri);
        if (params.length > 0) {
            stringBuilder.append(!uri.contains("?") ? "?" : "&");
            for (int i = 0; i < params.length; i += 2) {
                try {
                    stringBuilder.append(params[i]).append("=").append(URLEncoder.encode(params[i + 1], "utf-8")).append("&");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    public static JSONObject loadJson(URL url) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br =  new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            br.close();
        }
        return new JSONObject(sb.toString());
    }

}
