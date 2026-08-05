package com.github.liuanxin.api.model;

import com.github.liuanxin.api.constant.ApiConst;

import java.util.Objects;

public class DocumentReturn {

    private String name;
    private String type;
    private String desc = ApiConst.EMPTY;


    public DocumentReturn() {
    }
    public DocumentReturn(String name, String type, String desc) {
        this.name = name;
        this.type = type;
        this.desc = desc;
    }


    public String getName() {
        return name;
    }
    public DocumentReturn setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }
    public DocumentReturn setType(String type) {
        this.type = type;
        return this;
    }

    public String getDesc() {
        return desc;
    }
    public DocumentReturn setDesc(String desc) {
        this.desc = desc;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentReturn that = (DocumentReturn) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(desc, that.desc);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, type, desc);
    }
}
