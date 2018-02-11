package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentCopyright {
    private String title;
    private String copyright;
    private String contact;
    private String version;

    /** 忽略收集的 url 列表(格式 url|method, 如果没有 method 则只匹配 url) */
    @JsonIgnore
    private Set<String> ignoreUrlSet;
    /** 是否是线上环境, 如果是线上环境将不会输出文档 */
    @JsonIgnore
    private boolean online = false;
    /** 返回示例中是否包含注释 */
    @JsonIgnore
    private boolean commentInReturnExample = true;
    /** 返回字段说明在单独罗列时是否记录父属性, 无视此值当 commentInReturnExample 为 true 时. forget this. It's a bad ide */
    @JsonIgnore
    private boolean returnRecordLevel = false;
}
