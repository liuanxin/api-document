package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Tools;
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

    private String name;
    private String type;
    private String desc = Tools.EMPTY;
}
