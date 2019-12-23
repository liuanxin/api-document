package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.util.Tools;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentModule implements Comparable<DocumentModule> {

    @JsonIgnore
    private int index = Integer.MAX_VALUE;

    private String name;
    private String info;
    private List<DocumentUrl> urlList;

    public DocumentModule(String groupName) {
        fillNameAndInfo(groupName);
    }
    public void fillNameAndInfo(String groupName) {
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
    public void fillModule(String moduleName) {
        if (Tools.isNotEmpty(moduleName)) {
            String[] split = moduleName.split("-");
            String name, info;
            if (split.length > 1) {
                name = split[0];
                info = split[1];
            } else {
                name = info = moduleName;
            }
            this.name = name + this.name;
            this.info = info + "(" + this.info + ")";
        }
    }
    public void fillExampleUrl(String moduleUrl) {
        for (DocumentUrl documentUrl : urlList) {
            String exampleUrl = documentUrl.getExampleUrl();
            if (!exampleUrl.startsWith(moduleUrl)) {
                documentUrl.setExampleUrl(moduleUrl + exampleUrl);
            }
        }
    }
    public void addUrl(DocumentUrl url) {
        if (Tools.isBlank(urlList)) {
            urlList = new LinkedList<>();
        }
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
