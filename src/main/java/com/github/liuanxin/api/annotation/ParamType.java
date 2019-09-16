package com.github.liuanxin.api.annotation;

public enum ParamType {

    Header, Query;

    public boolean hasHeader() {
        return this == Header;
    }
}
