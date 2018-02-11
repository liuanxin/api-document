package com.github.liuanxin.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentParam {
    /** 参数名称 */
    private String name;
    /** 参数类型 */
    private String type;
    /** 参数是否必须 */
    private boolean must;
    /** 参数说明 */
    private String desc;
    // /** 参数示例 */
    // private String example;
}
