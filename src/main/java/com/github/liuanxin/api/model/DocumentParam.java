package com.github.liuanxin.api.model;

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
}
