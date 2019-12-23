package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
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


    public void append(List<DocumentInfo> projects) {
        if (Tools.isNotEmpty(projects)) {
            Set<DocumentParam> tokenSet = Tools.isBlank(tokenList)
                    ? new LinkedHashSet<DocumentParam>()
                    : new LinkedHashSet<>(tokenList);

            Set<DocumentResponse> responseSet = Tools.isBlank(responseList)
                    ? new LinkedHashSet<DocumentResponse>()
                    : new LinkedHashSet<>(responseList);

            Map<String, Object> enumMap = Tools.isBlank(enumInfo)
                    ? new LinkedHashMap<String, Object>()
                    : new LinkedHashMap<>(enumInfo);

            Set<DocumentModule> moduleSet = Tools.isBlank(moduleList)
                    ? new LinkedHashSet<DocumentModule>()
                    : new LinkedHashSet<>(moduleList);

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
    }
}
