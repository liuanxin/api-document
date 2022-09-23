package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;

import java.util.*;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentUrl implements Comparable<DocumentUrl> {

    private static final String WRAP = "\n";
    private static final String WRAP_REPLACE = " <> ";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String COMMENT_START = "  /* ";
    private static final String COMMENT_END = " */";

    private static final Pattern SPLIT_PATTERN = Pattern.compile("/");
    private static final Pattern START_BIG_PATTERN = Pattern.compile("\\{");
    private static final Pattern END_BIG_PATTERN = Pattern.compile("}");


    private String id;
    private String title = ApiConst.EMPTY;
    private String desc = ApiConst.EMPTY;
    private String develop = ApiConst.EMPTY;

    private String exampleUrl;
    private String method;
    private String url;

    private String useGlobalParam = ApiConst.EMPTY;
    private String requestBody = ApiConst.EMPTY;
    private String basicParamRequestBody = ApiConst.EMPTY;
    private List<DocumentParam> paramList;
    private List<DocumentResponse> responseList;
    private List<DocumentReturn> returnList;
    private String commentJson;


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
        if (Tools.isNotEmpty(id)) {
            return id;
        }

        String url = SPLIT_PATTERN.matcher(this.url).replaceAll(ApiConst.HORIZON);
        url = START_BIG_PATTERN.matcher(url).replaceAll(ApiConst.EMPTY);
        url = END_BIG_PATTERN.matcher(url).replaceAll(ApiConst.EMPTY);
        if (Tools.isNotEmpty(method)) {
            return method.toLowerCase() + (url.startsWith(ApiConst.HORIZON) ? ApiConst.EMPTY : ApiConst.HORIZON) + url;
        } else {
            return url.startsWith(ApiConst.HORIZON) ? url.substring(1) : url;
        }
    }
    public DocumentUrl setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return Tools.isEmpty(title) ? getId() : title;
    }
    public DocumentUrl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDesc() {
        return desc;
    }
    public DocumentUrl setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getDevelop() {
        return develop;
    }
    public DocumentUrl setDevelop(String develop) {
        this.develop = develop;
        return this;
    }

    public String getExampleUrl() {
        return exampleUrl;
    }
    public DocumentUrl setExampleUrl(String exampleUrl) {
        this.exampleUrl = exampleUrl;
        return this;
    }

    public String getMethod() {
        return method;
    }
    public DocumentUrl setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return url;
    }
    public DocumentUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getContentType() {
        if ("1".equals(requestBody)) {
            return "application/json";
        } else {
            if (paramList != null && !paramList.isEmpty()) {
                boolean file = false;
                for (DocumentParam param : paramList) {
                    if (ApiConst.FILE_TYPE.equalsIgnoreCase(param.getDataType())) {
                        file = true;
                        break;
                    }
                }
                if (file) {
                    return "multipart/form-data";
                }
            }
        }
        return "application/x-www-form-urlencoded";
    }

    public String getUseGlobalParam() {
        return useGlobalParam;
    }
    public DocumentUrl setUseGlobalParam(String useGlobalParam) {
        this.useGlobalParam = useGlobalParam;
        return this;
    }

    public String getRequestBody() {
        return requestBody;
    }
    public DocumentUrl setRequestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public String getBasicParamRequestBody() {
        return basicParamRequestBody;
    }
    public DocumentUrl setBasicParamRequestBody(String basicParamRequestBody) {
        this.basicParamRequestBody = basicParamRequestBody;
        return this;
    }

    public List<DocumentParam> getParamList() {
        return paramList;
    }
    public DocumentUrl setParamList(List<DocumentParam> paramList) {
        this.paramList = paramList;
        return this;
    }

    public List<DocumentResponse> getResponseList() {
        return responseList;
    }
    public DocumentUrl setResponseList(List<DocumentResponse> responseList) {
        this.responseList = responseList;
        return this;
    }

    public DocumentUrl setReturnList(List<DocumentReturn> returnList) {
        this.returnList = returnList;
        return this;
    }

    public DocumentUrl setCommentJson(String commentJson) {
        this.commentJson = commentJson;
        return this;
    }

    public int getIndex() {
        return index;
    }
    public DocumentUrl setIndex(int index) {
        this.index = index;
        return this;
    }

    public boolean isReturnRecordLevel() {
        return returnRecordLevel;
    }
    public DocumentUrl setReturnRecordLevel(boolean returnRecordLevel) {
        this.returnRecordLevel = returnRecordLevel;
        return this;
    }

    public boolean isCommentInReturnExampleWithLevel() {
        return commentInReturnExampleWithLevel;
    }
    public DocumentUrl setCommentInReturnExampleWithLevel(boolean commentInReturnExampleWithLevel) {
        this.commentInReturnExampleWithLevel = commentInReturnExampleWithLevel;
        return this;
    }

    public boolean isCommentInReturnExample() {
        return commentInReturnExample;
    }
    public DocumentUrl setCommentInReturnExample(boolean commentInReturnExample) {
        this.commentInReturnExample = commentInReturnExample;
        return this;
    }

    public String getReturnJson() {
        return returnJson;
    }
    public DocumentUrl setReturnJson(String returnJson) {
        this.returnJson = returnJson;
        return this;
    }

    public List<DocumentReturn> getReturnList() {
        return returnList(commentInReturnExample, returnRecordLevel, returnList);
    }

    public String getCommentJson() {
        return commentJson(returnJson, commentInReturnExample, commentInReturnExampleWithLevel, returnList);
    }


    public static List<DocumentReturn> returnList(boolean commentInReturnExample,
                                                  boolean returnRecordLevel,
                                                  List<DocumentReturn> returnList) {
        if (commentInReturnExample || Tools.isEmpty(returnList)) {
            return Collections.emptyList();
        }
        if (returnRecordLevel) {
            return returnList;
        }

        List<DocumentReturn> documentReturns = new LinkedList<>();
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
    public static String commentJson(String returnJson,
                                     boolean commentInReturnExample,
                                     boolean commentInReturnExampleWithLevel,
                                     List<DocumentReturn> returnList) {
        if (Tools.isEmpty(returnJson)) {
            return ApiConst.EMPTY;
        }

        String commentJson = Tools.toPrettyJson(returnJson);
        if (Tools.isEmpty(commentJson)) {
            return ApiConst.EMPTY;
        }

        // for windows pretty... \r\n to \n
        commentJson = commentJson.replace("\r", ApiConst.EMPTY);
        if (!commentInReturnExample) {
            return commentJson;
        }

        StringBuilder sbd = new StringBuilder();

        String[] split = commentJson.split(WRAP);
        if (commentInReturnExampleWithLevel) {
            Map<Integer, String> indexMap = indexMap(split);
            Map<String, String> commentMap = documentReturnMap(returnList);

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
            for (String comment : split) {
                sbd.append(comment);

                comment = comment.trim();
                if (returnList.size() > index) {
                    DocumentReturn documentReturn = returnList.get(index);
                    if (Tools.isNotEmpty(documentReturn)) {
                        String returnName = documentReturn.getName().replace(ReturnHandler.TAB, ApiConst.EMPTY).trim();
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
    private static Map<Integer, String> indexMap(String[] split) {
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
    private static Map<String, String> documentReturnMap(List<DocumentReturn> returnList) {
        Map<String, String> returnMap = Tools.newHashMap();
        for (DocumentReturn documentReturn : returnList) {
            returnMap.put(documentReturn.getName().replace(ReturnHandler.TAB, ApiConst.EMPTY), documentReturn.getDesc());
        }
        return returnMap;
    }


    @Override
    public int compareTo(DocumentUrl obj) {
        if (Tools.isBlank(obj)) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentUrl that = (DocumentUrl) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(desc, that.desc) &&
                Objects.equals(develop, that.develop) &&
                Objects.equals(exampleUrl, that.exampleUrl) &&
                Objects.equals(method, that.method) &&
                Objects.equals(url, that.url) &&
                Objects.equals(useGlobalParam, that.useGlobalParam) &&
                Objects.equals(requestBody, that.requestBody) &&
                Objects.equals(paramList, that.paramList) &&
                Objects.equals(responseList, that.responseList) &&
                Objects.equals(returnList, that.returnList);
    }
    @Override
    public int hashCode() {
        return Objects.hash(title, desc, develop, exampleUrl, method, url, useGlobalParam, requestBody, paramList, responseList, returnList);
    }
}
