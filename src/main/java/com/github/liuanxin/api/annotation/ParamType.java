package com.github.liuanxin.api.annotation;

public enum ParamType {

    Header, Query;

    public static boolean hasHeader(String type) {
        return Header.name().equalsIgnoreCase(type);
    }
}
