package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentResponse {

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


    public DocumentResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public DocumentResponse(ApiResponse response) {
        this.code = response.code();
        this.msg = response.msg();
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
}
