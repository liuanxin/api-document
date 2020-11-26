package com.github.liuanxin.api.util;

public enum DateType {

    YYYY_MM_DD_HH_MM_SS_SSS("yyyy-MM-dd HH:mm:ss.SSS"),
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
    MM_DD_YYYY_HH_MM_SS_SSS("MM/dd/yyyy HH:mm:ss.SSS"),

    MM_DD_YYYY_HH_MM_SS("MM/dd/yyyy HH:mm:ss"),
    YYYY_MM_DD("yyyy-MM-dd"),
    YYYY__MM__DD("yyyy/MM/dd"),
    MM_DD_YYYY("MM/dd/yyyy"),

    YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),
    YYYY_MM_DD_AP("yyyy-MM-dd a"),
    YYYY_MM("yyyy-MM"),

    CN_YYYY_MM_DD_HH_MM_SS("yyyy年MM月dd日 HH时mm分ss秒"),
    CN_YYYY_MM_DD_HH_MM("yyyy年MM月dd日 HH点mm分"),
    CN_YYYY_MM_DD_HH("yyyy年MM月dd日 HH点"),
    CN_YYYY_MM_DD("yyyy年MM月dd日"),

    YYYYMMDDHHMMSSSSS("yyyyMMddHHmmssSSS"),
    YYYYMMDDHHMMSS("yyyyMMddHHmmss"),
    YYYYMMDDHHMM("yyyyMMddHHmm"),
    YYYYMMDD("yyyyMMdd"),
    YYMMDD("yyMMdd"),
    YYYYMM("yyyyMM"),
    HH_MM_SS("HH:mm:ss"),
    HH_MM("HH:mm"),

    TSZ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
    TS("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    TZ("yyyy-MM-dd'T'HH:mm:ss'Z'"),
    T("yyyy-MM-dd'T'HH:mm:ss");

    private String value;
    DateType(String value) { this.value = value; }
    public String getValue() { return value; }
}
