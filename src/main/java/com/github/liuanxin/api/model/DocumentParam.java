package com.github.liuanxin.api.model;

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
public class DocumentParam {

    private String name;
    private String dataType;
    private String paramType = Tools.EMPTY;
    private Boolean must = false;
    private String desc = Tools.EMPTY;
    private String example = Tools.EMPTY;
    private Boolean hasTextarea = false;
    private Boolean hasFile = false;

    public static DocumentParam buildToken(String name, String desc, String example, ParamType paramType) {
        DocumentParam param = new DocumentParam().setDataType("String").setName(name).setDesc(desc).setExample(example);
        if (paramType != null) {
            param.setParamType(paramType.name());
        }
        return param;
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
        return param;
    }
}
