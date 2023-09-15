package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiResponse;
import com.github.liuanxin.api.util.Tools;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentResponse implements Comparable<DocumentResponse> {

    private int code;
    private String msg;

    private String comment;
    private List<DocumentReturn> returnList;


    @JsonIgnore
    private Class<?> response;
    @JsonIgnore
    private Class<?> genericParent;
    @JsonIgnore
    private Class<?>[] generic;
    @JsonIgnore
    private Class<?>[] genericChild;


    public DocumentResponse() {
    }
    public DocumentResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public DocumentResponse(ApiResponse response) {
        this.code = response.code();
        this.msg = response.msg();
    }


    public int getCode() {
        return code;
    }
    public DocumentResponse setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }
    public DocumentResponse setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getComment() {
        return comment;
    }
    public DocumentResponse setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public List<DocumentReturn> getReturnList() {
        return returnList;
    }
    public DocumentResponse setReturnList(List<DocumentReturn> returnList) {
        this.returnList = returnList;
        return this;
    }

    public Class<?> getResponse() {
        return response;
    }
    public DocumentResponse setResponse(Class<?> response) {
        this.response = response;
        return this;
    }

    public Class<?> getGenericParent() {
        return genericParent;
    }
    public DocumentResponse setGenericParent(Class<?> genericParent) {
        this.genericParent = genericParent;
        return this;
    }

    public Class<?>[] getGeneric() {
        return generic;
    }
    public DocumentResponse setGeneric(Class<?>[] generic) {
        this.generic = generic;
        return this;
    }

    public Class<?>[] getGenericChild() {
        return genericChild;
    }
    public DocumentResponse setGenericChild(Class<?>[] genericChild) {
        this.genericChild = genericChild;
        return this;
    }


    public DocumentResponse setResponse(Class<?> response, Class<?>[] generic) {
        this.response = response;
        this.generic = generic;
        return this;
    }
    public DocumentResponse setResponse(Class<?> response, Class<?> genericParent, Class<?>[] generic) {
        this.response = response;
        this.genericParent = genericParent;
        this.generic = generic;
        return this;
    }
    public DocumentResponse setResponse(Class<?> response, Class<?> genericParent, Class<?>[] generic, Class<?>[] genericChild) {
        this.response = response;
        this.genericParent = genericParent;
        this.generic = generic;
        this.genericChild = genericChild;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentResponse that = (DocumentResponse) o;
        return code == that.code &&
                Objects.equals(msg, that.msg) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(returnList, that.returnList);
    }
    @Override
    public int hashCode() {
        return Objects.hash(code, msg, comment, returnList);
    }

    @Override
    public int compareTo(DocumentResponse response) {
        return Tools.isNull(response) ? -1 : (this.code - response.getCode());
    }
}
