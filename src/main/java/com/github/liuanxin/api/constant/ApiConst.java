package com.github.liuanxin.api.constant;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public final class ApiConst {

    public static final String BEAN_NAME = "document-collect-controller";

    public static final String URL_PREFIX = "/api";
    public static final String URL_CLEAR = "/clear";
    public static final String URL_INFO = "/info";
    public static final String PLACEHOLDER = "id";
    public static final String URL_EXAMPLE = "/example/{" + PLACEHOLDER + "}.json";
    public static final String PRODUCES = "application/json; charset=UTF-8";


    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String SPLIT = ",";
    public static final String FILE_TYPE = "file";
    public static final Date NOW = new Date();


    public static final String HTTP = "http://";
    public static final String HTTPS = "http://";
    public static final String SCHEME = "//";
    public static final String URL_SPLIT = "/";


    public static final List<String> DATES = Arrays.asList("date", "time", "datetime");


    public static final Pattern ID_URL_PATTERN = Pattern.compile("\\{.*?}");
}
