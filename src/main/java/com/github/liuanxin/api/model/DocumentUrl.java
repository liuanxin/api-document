package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Utils;
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
    private String id;
    /** 接口标题 */
    private String title;
    /** 接口详细说明 */
    private String desc = "详细说明待完善";
    /** 开发者及联系方式 */
    private String develop = "开发者信息待完善";

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
        return Utils.isBlank(title) ? getId() : title;
    }
}
