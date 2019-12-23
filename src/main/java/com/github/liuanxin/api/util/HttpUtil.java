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
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

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
        return (!tmpUrl.startsWith(ApiConst.HTTP) && !tmpUrl.startsWith(ApiConst.HTTPS)) ? ("http://" + url) : url;
    }

    public static String getDomain(String url) {
        if (Tools.isBlank(url)) {
            return ApiConst.EMPTY;
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith(ApiConst.HTTP)) {
            String tmp = url.substring(ApiConst.HTTP.length());
            return url.substring(ApiConst.HTTP.length(), ApiConst.HTTP.length() + getIndex(tmp));
        } else if (lowerUrl.startsWith(ApiConst.HTTPS)) {
            String tmp = url.substring(ApiConst.HTTPS.length());
            return url.substring(ApiConst.HTTPS.length(), ApiConst.HTTPS.length() + (getIndex(tmp)));
        } else if (lowerUrl.startsWith(ApiConst.SCHEME)) {
            String tmp = url.substring(ApiConst.SCHEME.length());
            return url.substring(ApiConst.SCHEME.length(), ApiConst.SCHEME.length() + (getIndex(tmp)));
        } else {
            return url.substring(0, (getIndex(url)));
        }
    }
    private static int getIndex(String tmp) {
        return tmp.contains(ApiConst.URL_SPLIT) ? tmp.indexOf(ApiConst.URL_SPLIT) : tmp.length();
    }

    private static String connection(String url, String method, Map<String, Object> params, int timeout) {
        url = handleUrl(url);

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
                LOGGER.error(String.format("request (%s, %s) exception", method, url), e);
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
