package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentUrl {

    private static final String WRAP = "\n";
    private static final String WRAP_REPLACE = " <> ";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String COMMENT_START = "  /* ";
    private static final String COMMENT_END = " */";
    private static final String STR = String.class.getSimpleName();

    @JsonIgnore
    private int index = Integer.MAX_VALUE;

    /** 接口标题 */
    private String title;
    /** 接口详细说明 */
    private String desc;
    /** 开发者及联系方式 */
    private String develop;

    private String exampleUrl;
    private String method;
    private String url;
    private List<DocumentParam> paramList;

    /** 返回示例中是否包含注释 */
    @JsonIgnore
    private boolean commentInReturnExample;
    @JsonIgnore
    private String returnJson;
    private List<DocumentReturn> returnList;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\/");
    private static final Pattern START_BIG_PATTERN = Pattern.compile("\\{");
    private static final Pattern END_BIG_PATTERN = Pattern.compile("\\}");

    public String getId() {
        String url = SPLIT_PATTERN.matcher(this.url).replaceAll("-");
        url = START_BIG_PATTERN.matcher(url).replaceAll(Tools.EMPTY);
        url = END_BIG_PATTERN.matcher(url).replaceAll(Tools.EMPTY);
        if (Tools.isNotBlank(method)) {
            return method.toLowerCase() + (url.startsWith("-") ? Tools.EMPTY : "-") + url;
        } else {
            return url.startsWith("-") ? url.substring(1) : url;
        }
    }
    public String getTitle() {
        return Tools.isBlank(title) ? getId() : title;
    }

    public String getCommentJson() {
        if (Tools.isBlank(returnJson)) {
            return Tools.EMPTY;
        }

        String commentJson = Tools.toPrettyJson(returnJson);
        if (Tools.isBlank(commentJson)) {
            return Tools.EMPTY;
        }
        if (!commentInReturnExample) {
            return commentJson;
        }

        StringBuilder sbd = new StringBuilder();
        String[] split = commentJson.split(WRAP);
        int index = 0;
        for (String comment : split) {
            sbd.append(comment);

            String trim = comment.trim();
            if (returnList.size() > index) {
                DocumentReturn documentReturn = returnList.get(index);
                if (Tools.isNotBlank(documentReturn)) {
                    String returnName = documentReturn.getName().replace(ReturnHandler.TAB, Tools.EMPTY).trim();
                    if (trim.startsWith(DOUBLE_QUOTE + returnName + DOUBLE_QUOTE)) {
                        if (!STR.equalsIgnoreCase(documentReturn.getType())) {
                            String desc = documentReturn.getDesc();
                            if (Tools.isNotBlank(desc)) {
                                sbd.append(COMMENT_START).append(desc.replace(WRAP, WRAP_REPLACE)).append(COMMENT_END);
                            }
                        }
                        index++;
                    }
                }
            }
            sbd.append(WRAP);
        }
        return sbd.delete(sbd.length() - 1, sbd.length()).toString();
    }

    public List<DocumentReturn> getReturnList() {
        if (commentInReturnExample) {
            return Collections.emptyList();
        }
        return returnList;
    }
}
