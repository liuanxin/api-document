package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.util.Tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DocumentModule extends Document implements Comparable<DocumentModule> {

    @JsonIgnore
    private int index = Integer.MAX_VALUE;

    private String name;
    private String info;
    private List<DocumentUrl> urlList;

    public DocumentModule() {
    }

    public DocumentModule(String groupName) {
        fillNameAndInfo(groupName);
    }

    public int getIndex() {
        return index;
    }
    public DocumentModule setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }
    public DocumentModule setName(String name) {
        this.name = name;
        return this;
    }

    public String getInfo() {
        return info;
    }
    public DocumentModule setInfo(String info) {
        this.info = info;
        return this;
    }

    public List<DocumentUrl> getUrlList() {
        return urlList;
    }
    public DocumentModule setUrlList(List<DocumentUrl> urlList) {
        this.urlList = urlList;
        return this;
    }

    public void fillNameAndInfo(String groupName) {
        if (Tools.isNotEmpty(groupName)) {
            if (groupName.contains(ApiConst.HORIZON)) {
                int index = groupName.indexOf(ApiConst.HORIZON);
                this.name = groupName.substring(0, index);
                this.info = groupName.substring(index + 1);
            } else {
                this.name = this.info = groupName;
            }
        }
    }
    public void fillModule(String moduleName) {
        if (Tools.isNotEmpty(moduleName)) {
            String[] split = moduleName.split(ApiConst.HORIZON);
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
            String url = documentUrl.getUrl();
            if (!exampleUrl.startsWith(moduleUrl)) {
                documentUrl.setUrl(moduleUrl + url);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentModule that = (DocumentModule) o;
        return Objects.equals(index, that.index) &&
                Objects.equals(name, that.name) &&
                Objects.equals(info, that.info) &&
                Objects.equals(urlList, that.urlList);
    }
    @Override
    public int hashCode() {
        return Objects.hash(index, name, info, urlList);
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
