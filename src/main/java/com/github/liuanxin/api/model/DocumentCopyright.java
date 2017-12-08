package com.github.liuanxin.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentCopyright {
    /** 作者 */
    private String auth;
    /** 团队 */
    private String team;
    /** 版本 */
    private String version;
    /** 是否是线上环境, 如果是线上环境将不会输出文档 */
    private boolean online = false;
}
