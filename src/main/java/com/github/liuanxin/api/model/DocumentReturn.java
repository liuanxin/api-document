package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Tools;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentReturn {

    private String name;
    private String type;
    private String desc = Tools.EMPTY;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentReturn that = (DocumentReturn) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(desc, that.desc);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, type, desc);
    }
}
