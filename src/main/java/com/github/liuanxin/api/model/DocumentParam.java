package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.annotation.ApiToken;
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
                .setName(name).setDesc(desc).setExample(example).setHasTextarea(textarea ? "1" : Tools.EMPTY);
    }

    public static DocumentParam buildToken(ApiToken token) {
        DocumentParam param = new DocumentParam();
        param.setDataType("String");
        param.setName(token.name());
        param.setDesc(token.desc());
        param.setExample(token.example());
        param.setHasToken("1");
        param.setParamType(token.paramType().hasHeader() ? "1" : Tools.EMPTY);
        param.setHasMust(token.must() ? "1" : Tools.EMPTY);
        param.setHasTextarea(token.textarea() ? "1" : Tools.EMPTY);
        param.setStyle(token.style());
        return param;
    }
}
