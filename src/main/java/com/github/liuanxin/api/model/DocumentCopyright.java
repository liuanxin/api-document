package com.github.liuanxin.api.model;

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
    /** 作者 */
    private String contact;
    /** 团队 */
    private String team;
    /** 版本 */
    private String version;
    /** 忽略收集的 url 列表(格式 url|method, 如果没有 method 则只匹配 url) */
    private Set<String> ignoreUrlList;
    /** 是否是线上环境, 如果是线上环境将不会输出文档 */
    private boolean online = false;
    /** 返回说明是否记录层级 */
    private boolean returnRecordLevel = false;
}
