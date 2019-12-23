package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiToken;
import com.github.liuanxin.api.constant.ApiConst;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentParam {

    private String name;
    private String dataType;
    private String showDataType;
    private String paramType;
    private String hasMust;
    private String desc;
    private String example;
    private String hasTextarea;
    private String datePattern;
    private String hasFile;
    private String hasToken;
    private String style;

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
        param.setHasToken("1");
        param.setParamType(token.paramType().hasHeader() ? "1" : ApiConst.EMPTY);
        param.setHasMust(token.must() ? "1" : ApiConst.EMPTY);
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
                Objects.equals(hasMust, that.hasMust) &&
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
                name, dataType, showDataType, paramType, hasMust, desc,
                example, hasTextarea, datePattern, hasFile, hasToken, style
        );
    }
}
