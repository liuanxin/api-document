package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.util.Tools;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentInfo {

    private List<DocumentParam> tokenList;
    private List<DocumentResponse> responseList;
    /**
     * <pre>
     * public enum Gender { <span style="color:green">// This enum will collect to: { Gender: [ "Nil", "Male", "Female" ] }</span>
     *   Nil, Male, Female;
     * }
     *
     *
     * public enum Gender { <span style="color:green">// This enum will collect to: { Gender: { "0": "Nil", "1": "Male", "2": "Female" } }</span>
     *   Nil(0), Male(1), Female(2);
     *
     *   int code;
     *   Gender(int code) { this.code = code; }
     *
     *   public int getCode() { return code; }
     * }
     *
     *
     * public enum Gender { <span style="color:green">// This enum will collect to: { gender: { "0": "未知", "1": "男", "2": "女" } }</span>
     *   Nil(0, "未知"), Male(1, "男"), Female(2, "女");
     *
     *   int code;
     *   String value;
     *   Gender(int code, String value) { this.code = code; this.value = value; }
     *
     *   public int getCode() { return code; }
     *   public String getValue() { return value; }
     * }
     * </pre>
     */
    private Map<String, Object> enumInfo;
    private List<DocumentModule> moduleList;


    public List<DocumentParam> getTokenList() {
        return tokenList;
    }
    public DocumentInfo setTokenList(List<DocumentParam> tokenList) {
        this.tokenList = tokenList;
        return this;
    }

    public List<DocumentResponse> getResponseList() {
        return responseList;
    }
    public DocumentInfo setResponseList(List<DocumentResponse> responseList) {
        this.responseList = responseList;
        return this;
    }

    public Map<String, Object> getEnumInfo() {
        return enumInfo;
    }
    public DocumentInfo setEnumInfo(Map<String, Object> enumInfo) {
        this.enumInfo = enumInfo;
        return this;
    }

    public List<DocumentModule> getModuleList() {
        return moduleList;
    }
    public DocumentInfo setModuleList(List<DocumentModule> moduleList) {
        this.moduleList = moduleList;
        return this;
    }


    public void append(List<DocumentInfo> projects) {
        if (Tools.isNotEmpty(projects)) {
            Set<DocumentParam> tokenSet = Tools.isEmpty(tokenList) ? new LinkedHashSet<>() : new LinkedHashSet<>(tokenList);
            Set<DocumentResponse> responseSet = Tools.isEmpty(responseList) ? new LinkedHashSet<>() : new LinkedHashSet<>(responseList);
            Map<String, Object> enumMap = Tools.isEmpty(enumInfo) ? new LinkedHashMap<>() : new LinkedHashMap<>(enumInfo);
            Set<DocumentModule> moduleSet = Tools.isEmpty(moduleList) ? new LinkedHashSet<>() : new LinkedHashSet<>(moduleList);

            for (DocumentInfo info : projects) {
                tokenSet.addAll(info.getTokenList());
                responseSet.addAll(info.getResponseList());
                enumMap.putAll(info.getEnumInfo());
                moduleSet.addAll(info.getModuleList());
            }

            this.tokenList = new ArrayList<>(tokenSet);
            this.responseList = new ArrayList<>(responseSet);
            this.enumInfo = new LinkedHashMap<>(enumMap);
            this.moduleList = new ArrayList<>(moduleSet);
        }
        Collections.sort(responseList);
    }
}
