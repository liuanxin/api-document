package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentInfo {

    private List<DocumentParam> tokenList;
    private List<DocumentResponse> responseList;
    /**<pre>
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
}
