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
public class DocumentReturn {
    /** 返回名称 */
    private String name;
    /** 返回类型. 字符串需要用双引号包住, bool 和 数字 不用 */
    private String type;
    /** 返回说明 */
    private String desc;
}
