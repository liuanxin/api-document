package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentModule {

    private String name;
    private String info;
    @SuppressWarnings("unchecked")
    private List<DocumentUrl> urlList = Tools.lists();

    public DocumentModule(String groupName) {
        if (Tools.isNotBlank(groupName)) {
            String[] split = groupName.split("-");
            if (split.length > 1) {
                this.name = split[0];
                this.info = split[1];
            } else {
                this.name = groupName;
            }
        }
    }
    public void addUrl(DocumentUrl url) {
        urlList.add(url);
    }
}
