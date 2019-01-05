package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
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
    private List<DocumentUrl> urlList = new ArrayList<>();

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
    public int compareTo(DocumentModule obj) {
        if (obj == null) {
            return -1;
        }
        // sort: field index first, info second, name third
        int sort = index - obj.getIndex();
        if (sort != 0) {
            return sort;
        }
        sort = info.compareTo(obj.getInfo());
        if (sort != 0) {
            return sort;
        }
        return name.compareTo(obj.getName());
    }
}
