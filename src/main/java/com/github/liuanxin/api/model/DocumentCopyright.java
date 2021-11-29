package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentCopyright {

    private String title;
    private String team;
    private String version;
    private String copyright;

    private int groupCount = 0;
    private int apiCount = 0;


    /** when true will not return data */
    @JsonIgnore
    private boolean online = false;

    /** url|method or url */
    @JsonIgnore
    private Set<String> ignoreUrlSet;

    /** if method set, ignore global */
    @JsonIgnore
    private List<DocumentResponse> globalResponse;

    /** global token, generate in every api's param */
    @JsonIgnore
    private List<DocumentParam> globalTokens;

    /** return whether the sample contains a comment(method not has @ApiMethod, use this) */
    @JsonIgnore
    private boolean commentInReturnExample = true;

    /**
     * return Field Description Whether to record the parent attribute when listed separately,
     * regardless of this value when commentInReturnExample is true.
     *
     * forget this. It's a bad ide
     */
    @JsonIgnore
    private boolean returnRecordLevel = false;

    /** whether to combine multiple projects as expected */
    @JsonIgnore
    private boolean projectMerge = false;
    /** use it when want to collect other project */
    @JsonIgnore
    private Map<String, String> projectMap;


    public DocumentCopyright() {
    }

    public DocumentCopyright(String title, String team, String version, String copyright) {
        this.title = title;
        this.team = team;
        this.version = version;
        this.copyright = copyright;
    }


    public String getTitle() {
        return title;
    }
    public DocumentCopyright setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTeam() {
        return team;
    }
    public DocumentCopyright setTeam(String team) {
        this.team = team;
        return this;
    }

    public String getVersion() {
        return version;
    }
    public DocumentCopyright setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCopyright() {
        return copyright;
    }
    public DocumentCopyright setCopyright(String copyright) {
        this.copyright = copyright;
        return this;
    }

    public int getGroupCount() {
        return groupCount;
    }
    public DocumentCopyright setGroupCount(int groupCount) {
        this.groupCount = groupCount;
        return this;
    }

    public int getApiCount() {
        return apiCount;
    }
    public DocumentCopyright setApiCount(int apiCount) {
        this.apiCount = apiCount;
        return this;
    }

    public boolean isOnline() {
        return online;
    }
    public DocumentCopyright setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public Set<String> getIgnoreUrlSet() {
        return ignoreUrlSet;
    }
    public DocumentCopyright setIgnoreUrlSet(Set<String> ignoreUrlSet) {
        this.ignoreUrlSet = ignoreUrlSet;
        return this;
    }

    public List<DocumentResponse> getGlobalResponse() {
        return globalResponse;
    }
    public DocumentCopyright setGlobalResponse(List<DocumentResponse> globalResponse) {
        this.globalResponse = globalResponse;
        return this;
    }

    public List<DocumentParam> getGlobalTokens() {
        return globalTokens;
    }
    public DocumentCopyright setGlobalTokens(List<DocumentParam> globalTokens) {
        this.globalTokens = globalTokens;
        return this;
    }

    public boolean isCommentInReturnExample() {
        return commentInReturnExample;
    }
    public DocumentCopyright setCommentInReturnExample(boolean commentInReturnExample) {
        this.commentInReturnExample = commentInReturnExample;
        return this;
    }

    public boolean isReturnRecordLevel() {
        return returnRecordLevel;
    }
    public DocumentCopyright setReturnRecordLevel(boolean returnRecordLevel) {
        this.returnRecordLevel = returnRecordLevel;
        return this;
    }

    public boolean isProjectMerge() {
        return projectMerge;
    }
    public DocumentCopyright setProjectMerge(boolean projectMerge) {
        this.projectMerge = projectMerge;
        return this;
    }

    public Map<String, String> getProjectMap() {
        return projectMap;
    }
    public DocumentCopyright setProjectMap(Map<String, String> projectMap) {
        this.projectMap = projectMap;
        return this;
    }
}
