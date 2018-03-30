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
public class DocumentModule {

    @JsonIgnore
    private int index = Integer.MAX_VALUE;

    private String name;
    private String info = Tools.EMPTY;
    private List<DocumentUrl> urlList = new ArrayList<>();

    public DocumentModule(String groupName) {
        if (Tools.isNotBlank(groupName)) {
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
}
