package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentCopyright {

    private String title;
    private String team;
    private String version;
    private String copyright;

    private int groupCount = 0;
    private int apiCount = 0;


    /** url|method or url */
    @JsonIgnore
    private Set<String> ignoreUrlSet;

    /** when true will not return data */
    @JsonIgnore
    private boolean online = false;

    /** if method set, ignore global */
    @JsonIgnore
    private List<DocumentResponse> globalResponse;

    /** return whether the sample contains a comment(method not has @ApiMethod, use this) */
    @JsonIgnore
    private boolean commentInReturnExample = true;

    /**
     * Return Field Description Whether to record the parent attribute when listed separately,
     * regardless of this value when commentInReturnExample is true.
     *
     * forget this. It's a bad ide
     */
    @JsonIgnore
    private boolean returnRecordLevel = false;

    /** global token, generate in every api's param */
    @JsonIgnore
    private DocumentParam globalToken;


    public DocumentCopyright(String title, String team, String version, String copyright) {
        this.title = title;
        this.team = team;
        this.version = version;
        this.copyright = copyright;
    }
}
