package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.liuanxin.api.annotation.ParamType;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentUrl implements Comparable<DocumentUrl> {

    private static final String WRAP = "\n";
    private static final String WRAP_REPLACE = " <> ";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String COMMENT_START = "  /* ";
    private static final String COMMENT_END = " */";

    private static final Pattern SPLIT_PATTERN = Pattern.compile("/");
    private static final Pattern START_BIG_PATTERN = Pattern.compile("\\{");
    private static final Pattern END_BIG_PATTERN = Pattern.compile("}");


    private String title = Tools.EMPTY;
    private String desc = Tools.EMPTY;
    private String develop = Tools.EMPTY;

    private String exampleUrl;
    private String method;
    private String url;

    private List<DocumentParam> paramList;
    private List<DocumentResponse> responseList;
    private List<DocumentReturn> returnList;
    // private String commentJson;


    @JsonIgnore
    private int index = Integer.MAX_VALUE;
    @JsonIgnore
    private boolean returnRecordLevel;
    @JsonIgnore
    private boolean commentInReturnExampleWithLevel;
    @JsonIgnore
    private boolean commentInReturnExample;
    @JsonIgnore
    private String returnJson;


    public String getId() {
        String url = SPLIT_PATTERN.matcher(this.url).replaceAll("-");
        url = START_BIG_PATTERN.matcher(url).replaceAll(Tools.EMPTY);
        url = END_BIG_PATTERN.matcher(url).replaceAll(Tools.EMPTY);
        if (Tools.isNotEmpty(method)) {
            return method.toLowerCase() + (url.startsWith("-") ? Tools.EMPTY : "-") + url;
        } else {
            return url.startsWith("-") ? url.substring(1) : url;
        }
    }

    public String getTitle() {
        return Tools.isEmpty(title) ? getId() : title;
    }

    public List<DocumentReturn> getReturnList() {
        if (commentInReturnExample || Tools.isEmpty(returnList)) {
            return Collections.emptyList();
        }
        if (returnRecordLevel) {
            return returnList;
        }

        List<DocumentReturn> documentReturns = new ArrayList<>();
        for (DocumentReturn documentReturn : returnList) {
            String name = documentReturn.getName();
            if (name.contains(ReturnHandler.LEVEL_APPEND)) {
                DocumentReturn dr = new DocumentReturn();
                dr.setName(name.substring(0, name.indexOf(ReturnHandler.LEVEL_APPEND)).trim());
                dr.setType(documentReturn.getType());
                dr.setDesc(documentReturn.getDesc());
                documentReturns.add(dr);
            } else {
                documentReturns.add(documentReturn);
            }
        }
        return documentReturns;
    }

    public boolean getHasExample() {
        if (Tools.isEmpty(paramList)) {
            return false;
        }
        for (DocumentParam param : paramList) {
            if (Tools.isNotEmpty(param) && Tools.isNotEmpty(param.getExample())) {
                return true;
            }
        }
        return false;
    }

    public boolean getHasFile() {
        if (Tools.isEmpty(paramList)) {
            return false;
        }
        for (DocumentParam param : paramList) {
            if (Tools.isNotEmpty(param) && param.getHasFile()) {
                return true;
            }
        }
        return false;
    }

    public boolean getHasHeader() {
        if (Tools.isEmpty(paramList)) {
            return false;
        }
        for (DocumentParam param : paramList) {
            if (Tools.isNotEmpty(param) && ParamType.hasHeader(param.getParamType())) {
                return true;
            }
        }
        return false;
    }

    public String getCommentJson() {
        if (Tools.isEmpty(returnJson)) {
            return Tools.EMPTY;
        }

        String commentJson = Tools.toPrettyJson(returnJson);
        if (Tools.isEmpty(commentJson)) {
            return Tools.EMPTY;
        }

        // for windows pretty... \r\n to \n
        commentJson = commentJson.replace("\r", Tools.EMPTY);
        if (!commentInReturnExample) {
            return commentJson;
        }

        String[] split = commentJson.split(WRAP);
        // add comment in json
        StringBuilder sbd = new StringBuilder();

        if (commentInReturnExampleWithLevel) {
            Map<Integer, String> indexMap = indexMap(split);
            Map<String, String> commentMap = documentReturnMap();

            for (int i = 0; i < split.length; i++) {
                String comment = split[i];
                sbd.append(comment);

                String desc = commentMap.get(indexMap.get(i));
                if (Tools.isNotEmpty(desc) && !comment.contains(desc)) {
                    sbd.append(COMMENT_START).append(desc.replace(WRAP, WRAP_REPLACE)).append(COMMENT_END);
                }
                sbd.append(WRAP);
            }
        } else {
            int index = 0;
            // add comment in json
            for (String comment : split) {
                sbd.append(comment);

                comment = comment.trim();
                if (returnList.size() > index) {
                    DocumentReturn documentReturn = returnList.get(index);
                    if (Tools.isNotEmpty(documentReturn)) {
                        String returnName = documentReturn.getName().replace(ReturnHandler.TAB, Tools.EMPTY).trim();
                        if (returnName.contains(ReturnHandler.LEVEL_APPEND)) {
                            returnName = returnName.substring(0, returnName.indexOf(ReturnHandler.LEVEL_APPEND)).trim();
                        }
                        if (comment.startsWith(DOUBLE_QUOTE + returnName + DOUBLE_QUOTE)) {
                            String desc = documentReturn.getDesc();
                            if (Tools.isNotEmpty(desc) && !comment.contains(desc)) {
                                sbd.append(COMMENT_START).append(desc.replace(WRAP, WRAP_REPLACE)).append(COMMENT_END);
                            }
                            index++;
                        }
                    }
                }
                sbd.append(WRAP);
            }
        }
        if (sbd.toString().endsWith(WRAP)) {
            sbd.delete(sbd.length() - 1, sbd.length());
        }
        return sbd.toString();
    }
    /**
     * <pre>
     * 0  {
     * 1    "p1" : "a",
     * 2    "p2" : [ {
     * 3      "p3" : {
     * 4        "p4" : "ab",
     * 5        "p5" : {
     * 6          "p6" : "abc"
     * 7        }
     * 8      },
     * 9      "p7" : "abcd"
     * 10   }, {
     * 11     "p3" : {
     * 12       "p4" : "abcde",
     * 13       "p5" : {
     * 14         "p6" : "abcdef"
     * 15       }
     * 16     },
     * 17     "p7" : "abcdefg"
     * 18   } ],
     * 19   "p8" : "aa",
     * 20   "p9" : {
     * 21     "p10" : [ "aaa", "aaaa" ],
     * 22     "p11" : {
     * 23       "p12" : "aaaaa",
     * 24       "p13" : "aaaaaa"
     * 25     }
     * 26   }
     * 27 }
     *
     * to
     *
     * 0  : {
     * 1  : p1
     * 2  : p2
     * 3  : p3 -> p2
     * 4  : p4 -> p3 -> p2
     * 5  : p5 -> p3 -> p2
     * 6  : p6 -> p5 -> p3 -> p2
     * 7  : }
     * 8  : },
     * 9  : p7 -> p2
     * 10 : }, {
     * 11 : p3 -> p2
     * 12 : p4 -> p3 -> p2
     * 13 : p5 -> p3 -> p2
     * 14 : p6 -> p5 -> p3 -> p2
     * 15 : }
     * 16 : },
     * 17 : p7 -> p2
     * 18 : } ],
     * 19 : p8
     * 20 : p9
     * 21 : p10 -> p9
     * 22 : p11 -> p9
     * 23 : p12 -> p11 -> p9
     * 24 : p13 -> p11 -> p9
     * 25 : }
     * 26 : }
     * 27 : }
     * </pre>
     */
    private Map<Integer, String> indexMap(String[] split) {
        Map<Integer, String> indexMap = Tools.newHashMap();
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String comment = split[i].trim();
            if (!comment.contains(DOUBLE_QUOTE)) {
                indexMap.put(i, comment);
            } else {
                // "properties" : xxx => properties
                String tmp = comment.substring(1);
                tmp = tmp.substring(0, tmp.indexOf(DOUBLE_QUOTE));

                // put current line:  line-num : properties -> parent
                indexMap.put(i, tmp + field);

                // if end with "{", add one parent level: -> parent
                if (comment.endsWith("{")) {
                    field.insert(0, ReturnHandler.LEVEL_APPEND + tmp);
                }
            }
            // if end with ("}" etc...), subtract one parent level(not contains "}, {")
            if (comment.endsWith("}") || comment.endsWith("},")
                    || comment.endsWith("} ]") || comment.endsWith("} ],")) {
                // " -> parent"  =>  "parent"    or    " -> parent -> p'p"  =>  "parent -> p'p"
                if (field.toString().startsWith(ReturnHandler.LEVEL_APPEND)) {
                    field.delete(0, ReturnHandler.LEVEL_APPEND.length());
                }
                if (field.toString().contains(ReturnHandler.LEVEL_APPEND)) {
                    // "parent -> p'p"  =>  " -> p'p"
                    field.delete(0, field.indexOf(ReturnHandler.LEVEL_APPEND));
                } else {
                    // "parent"  =>  ""
                    field.delete(0, field.length());
                }
            }
        }
        return indexMap;
    }
    /**
     * <pre>
     * p1 : desc1
     * p2 : desc2
     * p3 -> p2 : desc3
     * p4 -> p3 -> p3 : desc4
     * p5 -> p3 -> p2 : desc5
     * p6 -> p2 : desc6
     * p7 : desc7
     * </pre>
     */
    private Map<String, String> documentReturnMap() {
        Map<String, String> returnMap = Tools.newHashMap();
        for (DocumentReturn documentReturn : returnList) {
            returnMap.put(documentReturn.getName().replace(ReturnHandler.TAB, Tools.EMPTY), documentReturn.getDesc());
        }
        return returnMap;
    }


    @Override
    public int compareTo(DocumentUrl obj) {
        if (obj == null) {
            return -1;
        }

        // sort: field index first, develop second, title third
        int sort = index - obj.getIndex();
        if (sort != 0) {
            return sort;
        } else {
            sort = develop.compareTo(obj.getDevelop());
            if (sort != 0) {
                return sort;
            } else {
                return title.compareTo(obj.getTitle());
            }
        }
    }
}
