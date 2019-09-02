package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.*;
import com.github.liuanxin.api.model.*;
import com.github.liuanxin.api.util.ParamHandler;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

@ApiIgnore
@RestController("document-collect-controller")
@RequestMapping(DocumentController.PARENT_URL_PREFIX)
public class DocumentController {

    static final String PARENT_URL_PREFIX = "/api";

    private static final String VERSION_CLEAR = "/clear";
    private static final String EXAMPLE_URL = "/example/{id}.json";
    private static final String INFO_URL = "/info";

    private static final String PRODUCES = "application/json; charset=UTF-8";

    private static final Lock LOCK = new ReentrantLock();

    private static final Pattern ID_URL_PATTERN = Pattern.compile("\\{.*?}");

    // local cache
    private static String return_info = null;
    private static Map<String, DocumentUrl> url_map = null;
    // local cache


    private final RequestMappingHandlerMapping mapping;
    private final DocumentCopyright copyright;

    public DocumentController(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping,
                              DocumentCopyright copyright) {
        this.mapping = mapping;
        this.copyright = copyright;
    }


    @PostMapping(value = VERSION_CLEAR, produces = PRODUCES)
    public int clear() {
        if (Tools.isNotBlank(return_info) || Tools.isNotEmpty(url_map)) {
            LOCK.lock();
            try {
                if (Tools.isNotBlank(return_info) || Tools.isNotEmpty(url_map)) {
                    return_info = null;
                    url_map = null;
                }
            } finally {
                LOCK.unlock();
            }
        }
        return 1;
    }

    @GetMapping(value = EXAMPLE_URL, produces = PRODUCES)
    public String urlExample(@PathVariable("id") String id) {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return Tools.EMPTY;
        } else {
            collect(mapping, copyright);
            if (Tools.isBlank(url_map)) {
                return Tools.EMPTY;
            } else {
                DocumentUrl document = url_map.get(id);
                return Tools.isBlank(document) ? Tools.EMPTY : document.getReturnJson();
            }
        }
    }

    @GetMapping(value = INFO_URL, produces = PRODUCES)
    public String url() {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return Tools.EMPTY;
        } else {
            collect(mapping, copyright);
            return return_info;
        }
    }

    private static void collect(RequestMappingHandlerMapping mapping, DocumentCopyright copyright) {
        if (Tools.isBlank(return_info) && Tools.isBlank(url_map)) {
            LOCK.lock();
            try {
                if (Tools.isBlank(return_info) && Tools.isBlank(url_map)) {
                    DocumentInfoAndUrlMap documentInfoAndUrlMap = infoAndUrlMap(mapping, copyright);
                    DocumentInfo document = documentInfoAndUrlMap.getDocumentInfo();
                    DocumentCopyright documentCopyright = copyright(copyright, document.getModuleList());

                    return_info = Tools.toJson(new ReturnInfo(document, documentCopyright));
                    url_map = documentInfoAndUrlMap.getDocumentMap();
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    private static DocumentCopyright copyright(DocumentCopyright copyright, List<DocumentModule> moduleList) {
        if (Tools.isNotEmpty(moduleList)) {
            int apiCount = 0;
            for (DocumentModule module : moduleList) {
                apiCount += module.getUrlList().size();
            }
            copyright.setGroupCount(moduleList.size()).setApiCount(apiCount);
        }
        return copyright;
    }

    private static DocumentInfoAndUrlMap infoAndUrlMap(RequestMappingHandlerMapping mapping,
                                                       DocumentCopyright copyright) {
        Map<String, DocumentModule> moduleMap = Tools.newLinkedHashMap();
        Map<String, DocumentUrl> documentMap = Tools.newLinkedHashMap();

        // meta info
        boolean globalCommentInReturn = copyright.isCommentInReturnExample();
        boolean globalRecordLevel = copyright.isReturnRecordLevel();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMapping = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            if (Tools.isNotBlank(requestMapping) && Tools.isNotBlank(handlerMethod) && wasJsonApi(handlerMethod)) {
                ApiIgnore ignore = getAnnotation(handlerMethod, ApiIgnore.class);
                if (Tools.isBlank(ignore) || !ignore.value()) {
                    Set<String> urlArray = requestMapping.getPatternsCondition().getPatterns();
                    Set<RequestMethod> methodArray = requestMapping.getMethodsCondition().getMethods();
                    if (!ignoreUrl(urlArray, methodArray, copyright.getIgnoreUrlSet())) {
                        DocumentUrl document = new DocumentUrl();
                        // url
                        document.setUrl(Tools.toStr(urlArray));
                        // method : get, post, put...
                        document.setMethod(Tools.toStr(methodArray));
                        // param
                        List<DocumentParam> paramList = ParamHandler.handlerParam(handlerMethod);
                        // no annotation: use global, annotation is false: not use, annotation is true: use self
                        ApiTokens apiTokens = getAnnotation(handlerMethod, ApiTokens.class);
                        if (Tools.isBlank(apiTokens)) {
                            document.setUseGlobalParam(true);
                        } else {
                            document.setUseGlobalParam(false);
                            if (apiTokens.value()) {
                                List<DocumentParam> extraParams = new LinkedList<>();
                                for (ApiToken token : apiTokens.token()) {
                                    extraParams.add(DocumentParam.buildToken(token));
                                }
                                if (extraParams.size() > 0) {
                                    paramList.addAll(0, extraParams);
                                }
                            }
                        }
                        document.setRequestBody(hasRequestBody(handlerMethod));
                        document.setParamList(paramList);

                        ApiMethod apiMethod = handlerMethod.getMethodAnnotation(ApiMethod.class);
                        String returnType = getReturnTypeByMethod(handlerMethod, apiMethod);
                        String method = handlerMethod.toString();
                        // return param
                        document.setReturnList(ReturnHandler.handlerReturn(method, returnType));
                        // return json
                        document.setReturnJson(ReturnHandler.handlerReturnJson(method, returnType));

                        boolean commentInReturn = globalCommentInReturn;
                        boolean recordLevel = globalRecordLevel;
                        if (Tools.isNotBlank(apiMethod)) {
                            document.setTitle(apiMethod.value());
                            document.setDesc(apiMethod.desc());
                            document.setDevelop(apiMethod.develop());
                            document.setIndex(apiMethod.index());
                            document.setCommentInReturnExampleWithLevel(apiMethod.commentInReturnExampleWithLevel());

                            boolean[] commentInReturnExample = apiMethod.commentInReturnExample();
                            if (commentInReturnExample.length > 0) {
                                commentInReturn = commentInReturnExample[0];
                            }
                            boolean[] returnRecordLevel = apiMethod.returnRecordLevel();
                            if (returnRecordLevel.length > 0) {
                                recordLevel = returnRecordLevel[0];
                            }
                        }

                        document.setCommentInReturnExample(commentInReturn);
                        document.setReturnRecordLevel(recordLevel);
                        document.setExampleUrl(getExampleUrl(document.getId()));
                        // response
                        document.setResponseList(methodResponse(handlerMethod, commentInReturn, recordLevel));

                        documentMap.put(document.getId(), document);
                        // add DocumentUrl to DocumentModule
                        ApiGroup apiGroup = getAnnotation(handlerMethod, ApiGroup.class);
                        if (Tools.isBlank(apiGroup)) {
                            // if no annotation on class, use ClassName(if className include Controller then remove)
                            String className = handlerMethod.getBeanType().getSimpleName();
                            String info = className;

                            String classSuffix = "Controller";
                            if (className.endsWith(classSuffix)) {
                                info = className.substring(0, className.indexOf(classSuffix));
                            }
                            addGroup(moduleMap, 0, info + "-" + className, document);
                        } else {
                            int index = apiGroup.index();
                            for (String group : apiGroup.value()) {
                                if (Tools.isNotBlank(group)) {
                                    addGroup(moduleMap, index, group, document);
                                }
                            }
                        }
                    }
                }
            }
        }

        Collection<DocumentModule> modules = moduleMap.values();
        List<DocumentModule> moduleList = new LinkedList<>();
        if (Tools.isNotEmpty(modules)) {
            for (DocumentModule module : modules) {
                Collections.sort(module.getUrlList());
                moduleList.add(module);
            }
            Collections.sort(moduleList);
        }
        DocumentInfo documentInfo = new DocumentInfo()
                .setTokenList(copyright.getGlobalTokens())
                .setResponseList(globalResponse(copyright.getGlobalResponse(), globalCommentInReturn, globalRecordLevel))
                .setEnumInfo(Tools.allEnumInfo())
                .setModuleList(moduleList);
        return new DocumentInfoAndUrlMap(documentInfo, documentMap);
    }

    private static String getReturnTypeByMethod(HandlerMethod handlerMethod, ApiMethod apiMethod) {
        String returnType;
        if (Tools.isNotBlank(apiMethod)) {
            returnType = getReturnTypeByAnnotation(Tools.first(apiMethod.returnType()));
        } else {
            returnType = Tools.EMPTY;
        }

        if (Tools.isEmpty(returnType) && Tools.isNotBlank(handlerMethod)) {
            returnType = handlerMethod.getMethod().getGenericReturnType().toString();
        }
        if (Tools.isNotBlank(returnType)) {
            String prefix = "class ";
            if (returnType.startsWith(prefix)) {
                returnType = returnType.substring(prefix.length());
            }
        }
        return returnType;
    }
    private static String getReturnTypeByAnnotation(ApiReturnType type) {
        return Tools.isBlank(type)
                ? null
                : getReturnType(type.value(), type.genericParent(), type.generic(), type.genericChild());
    }
    private static String getReturnType(Class<?> response, Class<?> genericParent, Class<?>[] generic, Class<?>[] genericChild) {
        if (Tools.isBlank(response)) {
            return null;
        } else {
            StringBuilder sbd = new StringBuilder();
            sbd.append(response.getName());
            if (Tools.isNotBlank(genericParent) && genericParent != Void.class) {
                sbd.append("<").append(genericParent.getName());
            }

            if (Tools.isNotBlank(generic)) {
                int secondLen = generic.length;
                if (secondLen > 0) {
                    int childrenLen = 0;
                    if (Tools.isNotBlank(genericChild)) {
                        childrenLen = genericChild.length;
                        if (childrenLen > 0 && secondLen > 1) {
                            secondLen = 1;
                        }
                    }

                    sbd.append("<");
                    for (int i = 0; i < secondLen; i++) {
                        if (i > 0) {
                            sbd.append(", ");
                        }
                        sbd.append(generic[i].getName());
                    }
                    if (childrenLen > 0) {
                        sbd.append("<");
                        for (int i = 0; i < childrenLen; i++) {
                            if (i > 0) {
                                sbd.append(", ");
                            }
                            sbd.append(genericChild[i].getName());
                        }
                        sbd.append(">");
                    }
                    sbd.append(">");
                }
            }

            if (Tools.isNotBlank(genericParent) && genericParent != Void.class) {
                sbd.append(">");
            }
            return sbd.toString();
        }
    }
    private static String getReturnTypeByResponse(DocumentResponse res) {
        return Tools.isBlank(res)
                ? null
                : getReturnType(res.getResponse(), res.getGenericParent(), res.getGeneric(), res.getGenericChild());
    }

    private static List<DocumentResponse> globalResponse(List<DocumentResponse> globalResponse,
                                                         boolean globalCommentInReturn,
                                                         boolean globalRecordLevel) {
        for (DocumentResponse response : globalResponse) {
            String type = getReturnTypeByResponse(response);
            if (Tools.isNotBlank(type)) {
                String method = "global handler";
                String json = ReturnHandler.handlerReturnJson(method, type);
                List<DocumentReturn> returnList = ReturnHandler.handlerReturn(method, type);

                response.setComment(DocumentUrl.commentJson(json, globalCommentInReturn, true, returnList));
                response.setReturnList(DocumentUrl.returnList(globalCommentInReturn, globalRecordLevel, returnList));
            }
        }
        return globalResponse;
    }
    private static List<DocumentResponse> methodResponse(HandlerMethod handlerMethod,
                                                         boolean methodCommentInReturn,
                                                         boolean methodRecordLevel) {
        List<DocumentResponse> responseList = new LinkedList<>();
        ApiResponses responses = getAnnotation(handlerMethod, ApiResponses.class);
        if (Tools.isNotBlank(responses)) {
            for (ApiResponse apiResponse : responses.value()) {
                DocumentResponse response = new DocumentResponse(apiResponse);

                String type = getReturnTypeByAnnotation(Tools.first(apiResponse.type()));
                if (Tools.isNotBlank(type)) {
                    String method = handlerMethod.toString();
                    String json = ReturnHandler.handlerReturnJson(method, type);
                    List<DocumentReturn> returnList = ReturnHandler.handlerReturn(method, type);

                    response.setComment(DocumentUrl.commentJson(json, methodCommentInReturn, true, returnList));
                    response.setReturnList(DocumentUrl.returnList(methodCommentInReturn, methodRecordLevel, returnList));
                }

                responseList.add(response);
            }
        }
        return responseList;
    }

    private static boolean hasRequestBody(HandlerMethod handlerMethod) {
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            RequestBody body = parameter.getParameterAnnotation(RequestBody.class);
            if (Tools.isNotBlank(body)) {
                return true;
            }
        }
        return false;
    }

    private static String getExampleUrl(String param) {
        // return exampleUrl.replaceFirst("\\{.*?\\}", param);
        return ID_URL_PATTERN.matcher(PARENT_URL_PREFIX + EXAMPLE_URL).replaceFirst(param);
    }

    private static void addGroup(Map<String, DocumentModule> moduleMap, int index, String group, DocumentUrl url) {
        DocumentModule module = moduleMap.get(group);
        if (Tools.isBlank(module)) {
            module = new DocumentModule(group);
            module.setIndex(index);
        } else if (module.getIndex() > index) {
            // if set multi module and different index, use the smaller
            module.setIndex(index);
        }
        module.addUrl(url);
        moduleMap.put(group, module);
    }

    private static boolean ignoreUrl(Set<String> urlSet, Set<RequestMethod> methodSet, Set<String> ignoreUrlSet) {
        if (Tools.isBlank(ignoreUrlSet)) {
            ignoreUrlSet = Tools.sets();
        }

        List<String> methodList = new LinkedList<>();
        for (RequestMethod method : methodSet) {
            methodList.add(method.name());
        }
        for (String ignoreUrl : ignoreUrlSet) {
            if (!ignoreUrl.startsWith("/")) {
                ignoreUrl = "/" + ignoreUrl;
            }
            if (ignoreUrl.contains("*")) {
                ignoreUrl = ignoreUrl.replace("*", "(.*)?");
                String[] urlAndMethod = ignoreUrl.split("\\|");
                if (urlAndMethod.length == 2) {
                    String tmpUrl = urlAndMethod[0];
                    String tmpMethod = urlAndMethod[1].toUpperCase();
                    if (methodList.contains(tmpMethod)) {
                        for (String url : urlSet) {
                            if (url.matches(tmpUrl)) {
                                return true;
                            }
                        }
                    }
                } else {
                    for (String url : urlSet) {
                        if (url.matches(ignoreUrl)) {
                            return true;
                        }
                    }
                }
            } else {
                String[] urlAndMethod = ignoreUrl.split("\\|");
                if (urlAndMethod.length == 2) {
                    String tmpUrl = urlAndMethod[0];
                    String tmpMethod = urlAndMethod[1].toUpperCase();
                    if (urlSet.contains(tmpUrl) && methodList.contains(tmpMethod)) {
                        return true;
                    }
                } else if (urlSet.contains(ignoreUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean wasJsonApi(HandlerMethod handlerMethod) {
        // @ResponseBody can be annotation on method and class
        if (Tools.isNotBlank(getAnnotation(handlerMethod, ResponseBody.class))) {
            return true;
        } else {
            // @RestController just annotation on class
            return Tools.isNotBlank(getAnnotationByClass(handlerMethod, RestController.class));
        }
    }

    private static <T extends Annotation> T getAnnotationByClass(HandlerMethod handlerMethod, Class<T> clazz) {
        return AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), clazz);
    }

    private static <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> clazz) {
        T annotation = handlerMethod.getMethodAnnotation(clazz);
        if (Tools.isBlank(annotation)) {
            annotation = getAnnotationByClass(handlerMethod, clazz);
        }
        return annotation;
    }
}
