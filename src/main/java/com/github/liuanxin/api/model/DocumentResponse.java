package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Data
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
}
