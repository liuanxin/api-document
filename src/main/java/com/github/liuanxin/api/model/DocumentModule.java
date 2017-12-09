package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Utils;
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

    private String name = "模块名待完善";
    private String info = "模块说明待完善";
    @SuppressWarnings("unchecked")
    private List<DocumentUrl> urlList = Utils.lists();

    public DocumentModule(String groupName) {
        if (Utils.isNotBlank(groupName)) {
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
