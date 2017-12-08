package com.github.liuanxin.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentUrl {
    /** 接口 id: method-url 即可, 也用来做页面锚点 */
    private String id;

    /** 接口标题 */
    private String title;
    /** 接口详细说明 */
    private String desc = "待完善详细说明";
    /** 开发者及联系方式 */
    private String develop = "未标明开发者信息";
    private String method;
    private String url;
    private List<DocumentParam> paramList;
    private List<DocumentReturn> returnList;
    private String returnJson;

    public String getId() {
        String url = this.url.replace("/", "-").replace("{", "").replace("}", "");
        return method.toLowerCase() + (url.startsWith("-") ? "" : "-") + url;
    }
    public String getTitle() {
        return (title == null || "".equals(title)) ? getId() : title;
    }
}
