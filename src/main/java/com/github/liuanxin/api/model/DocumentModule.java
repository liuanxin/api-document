package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentModule implements Comparable<DocumentModule> {

    @JsonIgnore
    private int index = Integer.MAX_VALUE;

    private String name;
    private String info = Tools.EMPTY;
    private List<DocumentUrl> urlList = new LinkedList<>();

    public DocumentModule(String groupName) {
        if (Tools.isNotEmpty(groupName)) {
            String[] split = groupName.split("-");
            if (split.length > 1) {
                this.name = split[0];
                this.info = split[1];
            } else {
                this.name = this.info = groupName;
            }
        }
    }
    public void addUrl(DocumentUrl url) {
        urlList.add(url);
    }


    @Override
    public int compareTo(DocumentModule module) {
        if (Tools.isBlank(module)) {
            return -1;
        }

        // sort: field index first, info second, name third
        int index = this.index - module.getIndex();
        if (index != 0) {
            return index;
        } else {
            index = info.compareTo(module.getInfo());
            if (index != 0) {
                return index;
            } else {
                return name.compareTo(module.getName());
            }
        }
    }
}
