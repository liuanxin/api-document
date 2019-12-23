package com.github.liuanxin.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final String HTTP = "http://";
    private static final String HTTPS = "http://";
    private static final String SCHEME = "//";
    private static final String URL_SPLIT = "/";

    private static final int TIME_OUT = 5 * 1000;

    public static String get(String url) {
        return get(url, null);
    }
    public static String get(String url, Map<String, Object> params) {
        return get(url, params, TIME_OUT);
    }
    public static String get(String url, Map<String, Object> params, int timeout) {
        return connection(url, "GET", params, timeout);
    }

    public static String post(String url) {
        return post(url, null);
    }
    public static String post(String url, Map<String, Object> params) {
        return post(url, params, TIME_OUT);
    }
    public static String post(String url, Map<String, Object> params, int timeout) {
        return connection(url, "POST", params, timeout);
    }

    public static String handleUrl(String url) {
        String tmpUrl = url.toLowerCase();
        return (!tmpUrl.startsWith(HTTP) && !tmpUrl.startsWith(HTTPS)) ? ("http://" + url) : url;
    }

    public static String getDomain(String url) {
        if (Tools.isBlank(url)) {
            return Tools.EMPTY;
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith(HTTP)) {
            String tmp = url.substring(HTTP.length());
            return url.substring(HTTP.length(), HTTP.length() + (tmp.contains(URL_SPLIT) ? tmp.indexOf(URL_SPLIT) : tmp.length()));
        } else if (lowerUrl.startsWith(HTTPS)) {
            String tmp = url.substring(HTTPS.length());
            return url.substring(HTTPS.length(), HTTPS.length() + (tmp.contains(URL_SPLIT) ? tmp.indexOf(URL_SPLIT) : tmp.length()));
        } else if (lowerUrl.startsWith(SCHEME)) {
            String tmp = url.substring(SCHEME.length());
            return url.substring(SCHEME.length(), SCHEME.length() + (tmp.contains(URL_SPLIT) ? tmp.indexOf(URL_SPLIT) : tmp.length()));
        } else {
            return url.substring(0, (url.contains(URL_SPLIT) ? url.indexOf(URL_SPLIT) : url.length()));
        }
    }

    private static String connection(String url, String method, Map<String, Object> params, int timeout) {
        url = handleUrl(url);

        String result = Tools.EMPTY;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod(method);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            long start = System.currentTimeMillis();
            connection.connect();

            InputStream inputStream = response(connection);
            if (inputStream != null) {
                result = inputToString(inputStream);
                if (LOGGER.isDebugEnabled()) {
                    long ms = System.currentTimeMillis() - start;
                    StringBuilder sbd = new StringBuilder();
                    sbd.append("Http => (").append(method).append(" ").append(url).append(")");
                    if (Tools.isNotEmpty(params)) {
                        sbd.append(" params(").append(params).append(")");
                    }
                    sbd.append(" time(").append(ms).append("ms), return(").append(result).append(")");
                    LOGGER.debug(sbd.toString());
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("request ({}, {}) exception", url, method, e);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
    private static InputStream response(HttpURLConnection conn) {
        try {
            return conn.getInputStream();
        } catch (IOException e) {
            return conn.getErrorStream();
        }
    }
    private static String inputToString(InputStream input) {
        try (
                InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(in)
        ) {
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("input stream to string exception", e);
            }
            return Tools.EMPTY;
        }
    }
}
