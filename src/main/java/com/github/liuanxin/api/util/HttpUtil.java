package com.github.liuanxin.api.util;

import com.github.liuanxin.api.constant.ApiConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final int TIME_OUT = 5 * 1000;

    public static String get(String url) {
        return get(url, null, TIME_OUT);
    }
    public static String get(String url, Map<String, Object> params, int timeout) {
        return connection(url, "GET", params, timeout);
    }

    public static String handleUrl(String url) {
        String tmpUrl = url.toLowerCase();
        return (!tmpUrl.startsWith(ApiConst.HTTP) && !tmpUrl.startsWith(ApiConst.HTTPS)) ? ("http://" + url) : url;
    }

    public static String getUrl(String url) {
        String lowerUrl = url.toLowerCase();
        String returnUrl;
        if (lowerUrl.startsWith(ApiConst.HTTP)) {
            returnUrl = url.substring(ApiConst.HP.length());
        } else if (lowerUrl.startsWith(ApiConst.HTTPS)) {
            returnUrl = url.substring(ApiConst.HPS.length());
        } else {
            returnUrl = url;
        }
        return returnUrl.endsWith("/") ? returnUrl.substring(0, returnUrl.length() - 1) : returnUrl;
    }

    private static String connection(String url, String method, Map<String, Object> params, int timeout) {
        url = handleUrl(url);
        url = appendParams(url, params);

        String result = ApiConst.EMPTY;
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
                    sbd.append("Http => (").append(method).append(ApiConst.SPACE).append(url).append(")");
                    if (Tools.isNotEmpty(params)) {
                        sbd.append(" params(").append(params).append(")");
                    }
                    sbd.append(" time(").append(ms).append("ms), return(").append(result).append(")");
                    LOGGER.debug(sbd.toString());
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("request ({}, {}) exception", method, url, e);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
    private static String appendParams(String url, Map<String, Object> params) {
        if (Tools.isEmpty(params)) {
            return url;
        }

        StringBuilder sbd = new StringBuilder(url);
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (Tools.isNull(entry.getKey()) || Tools.isNull(entry.getValue())) {
                continue;
            }
            if (first) {
                sbd.append(url.contains("?") ? "&" : "?");
                first = false;
            } else {
                sbd.append("&");
            }
            sbd.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sbd.append("=");
            sbd.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
        }
        return sbd.toString();
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
            StringBuilder sbd = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                sbd.append(str);
            }
            return sbd.toString();
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("input stream to string exception", e);
            }
            return ApiConst.EMPTY;
        }
    }
}
