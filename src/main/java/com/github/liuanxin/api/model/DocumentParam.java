package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiToken;
import com.github.liuanxin.api.constant.ApiConst;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentParam extends Document{

    private String dataType;
    private String showDataType;
    private String paramType;
    private String required;
    private String desc;
    private String example;
    private String hasTextarea;
    private String datePattern;
    private String hasFile;
    private String hasToken;
    private String style;

    public String getName() {
        return name;
    }
    public DocumentParam setName(String name) {
        this.name = name;
        return this;
    }

    public String getDataType() {
        return dataType;
    }
    public DocumentParam setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public String getShowDataType() {
        return showDataType;
    }
    public DocumentParam setShowDataType(String showDataType) {
        this.showDataType = showDataType;
        return this;
    }

    public String getParamType() {
        return paramType;
    }
    public DocumentParam setParamType(String paramType) {
        this.paramType = paramType;
        return this;
    }

    public String getRequired() {
        return required;
    }
    public DocumentParam setRequired(String required) {
        this.required = required;
        return this;
    }

    public String getDesc() {
        return desc;
    }
    public DocumentParam setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getExample() {
        return example;
    }
    public DocumentParam setExample(String example) {
        this.example = example;
        return this;
    }

    public String getHasTextarea() {
        return hasTextarea;
    }
    public DocumentParam setHasTextarea(String hasTextarea) {
        this.hasTextarea = hasTextarea;
        return this;
    }

    public String getDatePattern() {
        return datePattern;
    }
    public DocumentParam setDatePattern(String datePattern) {
        this.datePattern = datePattern;
        return this;
    }

    public String getHasFile() {
        return hasFile;
    }
    public DocumentParam setHasFile(String hasFile) {
        this.hasFile = hasFile;
        return this;
    }

    public String getHasToken() {
        return hasToken;
    }
    public DocumentParam setHasToken(String hasToken) {
        this.hasToken = hasToken;
        return this;
    }

    public String getStyle() {
        return style;
    }
    public DocumentParam setStyle(String style) {
        this.style = style;
        return this;
    }


    public static DocumentParam buildToken(String name, String desc, String example, boolean textarea) {
        return new DocumentParam().setDataType("String").setHasToken("1").setParamType("1")
                .setName(name).setDesc(desc).setExample(example).setHasTextarea(textarea ? "1" : ApiConst.EMPTY);
    }

    public static DocumentParam buildToken(ApiToken token) {
        DocumentParam param = new DocumentParam();
        param.setDataType(token.dataType());
        param.setName(token.name());
        param.setDesc(token.desc());
        param.setExample(token.example());
        param.setHasToken(token.globalSave() ? "1" : ApiConst.EMPTY);
        param.setParamType(token.paramType().hasHeader() ? "1" : ApiConst.EMPTY);
        param.setRequired(token.required() ? "1" : ApiConst.EMPTY);
        param.setHasTextarea(token.textarea() ? "1" : ApiConst.EMPTY);
        param.setStyle(token.style());
        return param;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentParam that = (DocumentParam) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(dataType, that.dataType) &&
                Objects.equals(showDataType, that.showDataType) &&
                Objects.equals(paramType, that.paramType) &&
                Objects.equals(required, that.required) &&
                Objects.equals(desc, that.desc) &&
                Objects.equals(example, that.example) &&
                Objects.equals(hasTextarea, that.hasTextarea) &&
                Objects.equals(datePattern, that.datePattern) &&
                Objects.equals(hasFile, that.hasFile) &&
                Objects.equals(hasToken, that.hasToken) &&
                Objects.equals(style, that.style);
    }
    @Override
    public int hashCode() {
        return Objects.hash(
                name, dataType, showDataType, paramType, required, desc,
                example, hasTextarea, datePattern, hasFile, hasToken, style
        );
    }
}
