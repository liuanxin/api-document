package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiToken;
import com.github.liuanxin.api.annotation.ParamType;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentParam {

    private String name;
    private String dataType;
    private String paramType = Tools.EMPTY;
    private Boolean must = false;
    private String desc = Tools.EMPTY;
    private String example = Tools.EMPTY;
    private Boolean hasTextarea = false;
    private Boolean hasFile = false;
    private Boolean hasToken = false;
    private String style;

    public static DocumentParam buildToken(String name, String desc, String example, boolean textarea) {
        return new DocumentParam().setDataType("String")
                .setHasToken(true).setParamType(ParamType.Header.name())
                .setName(name).setDesc(desc).setExample(example).setHasTextarea(textarea);
    }

    public static DocumentParam buildToken(ApiToken token) {
        DocumentParam param = new DocumentParam();
        param.setDataType("String");
        param.setName(token.name());
        param.setDesc(token.desc());
        param.setExample(token.example());
        param.setParamType(token.paramType().name());
        param.setMust(token.must());
        param.setHasTextarea(token.textarea());
        param.setStyle(token.style());
        param.setHasToken(true);
        return param;
    }
}
