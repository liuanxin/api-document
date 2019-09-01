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
                        // response
                        document.setResponseList(handleResponse(handlerMethod));

                        ApiMethod apiMethod = handlerMethod.getMethodAnnotation(ApiMethod.class);
                        String returnType = getReturnType(handlerMethod, apiMethod);
                        String method = handlerMethod.toString();
                        // return param
                        document.setReturnList(ReturnHandler.handlerReturn(method, returnType));
                        // return json
                        document.setReturnJson(ReturnHandler.handlerReturnJson(method, returnType));

                        // meta info
                        boolean globalCommentInReturn = copyright.isCommentInReturnExample();
                        boolean globalRecordLevel = copyright.isReturnRecordLevel();
                        if (Tools.isNotBlank(apiMethod)) {
                            document.setTitle(apiMethod.value());
                            document.setDesc(apiMethod.desc());
                            document.setDevelop(apiMethod.develop());
                            document.setIndex(apiMethod.index());
                            document.setCommentInReturnExampleWithLevel(apiMethod.commentInReturnExampleWithLevel());

                            boolean[] commentInReturn = apiMethod.commentInReturnExample();
                            document.setCommentInReturnExample(commentInReturn.length == 0 ? globalCommentInReturn : commentInReturn[0]);
                            boolean[] recordLevel = apiMethod.returnRecordLevel();
                            document.setReturnRecordLevel(recordLevel.length == 0 ? globalRecordLevel : recordLevel[0]);
                        } else {
                            document.setCommentInReturnExample(globalCommentInReturn);
                            document.setReturnRecordLevel(globalRecordLevel);
                        }
                        document.setExampleUrl(getExampleUrl(document.getId()));

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
                .setResponseList(copyright.getGlobalResponse())
                .setEnumInfo(Tools.allEnumInfo())
                .setModuleList(moduleList);
        return new DocumentInfoAndUrlMap(documentInfo, documentMap);
    }

    private static String getReturnType(HandlerMethod handlerMethod, ApiMethod apiMethod) {
        String returnType = Tools.EMPTY;
        if (Tools.isNotBlank(apiMethod)) {
            ApiReturnType customReturn = Tools.first(apiMethod.returnType());
            if (Tools.isNotBlank(customReturn)) {
                StringBuilder sbd = new StringBuilder();
                sbd.append(customReturn.value().getName());
                Class<?> parent = customReturn.genericParent();
                if (parent != Void.class) {
                    sbd.append("<").append(parent.getName());
                }

                Class<?>[] generics = customReturn.generic();
                int genericLen = generics.length;
                if (genericLen > 0) {
                    Class<?>[] genericChild = customReturn.genericChild();
                    int genericChildLen = genericChild.length;
                    if (genericChildLen > 0 && genericLen > 1) {
                        genericLen = 1;
                    }

                    sbd.append("<");
                    for (int i = 0; i < genericLen; i++) {
                        if (i > 0) {
                            sbd.append(", ");
                        }
                        sbd.append(generics[i].getName());
                    }
                    if (genericChildLen > 0) {
                        sbd.append("<");
                        for (int i = 0; i < genericChildLen; i++) {
                            if (i > 0) {
                                sbd.append(", ");
                            }
                            sbd.append(genericChild[i].getName());
                        }
                        sbd.append(">");
                    }
                    sbd.append(">");
                }

                if (parent != Void.class) {
                    sbd.append(">");
                }
                returnType = sbd.toString();
            }
        }
        if (Tools.isEmpty(returnType)) {
            returnType = handlerMethod.getMethod().getGenericReturnType().toString();
        }
        return returnType;
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

    private static List<DocumentResponse> handleResponse(HandlerMethod handlerMethod) {
        List<DocumentResponse> responseList = new LinkedList<>();
        ApiResponses responses = getAnnotation(handlerMethod, ApiResponses.class);
        if (Tools.isNotBlank(responses)) {
            for (ApiResponse response : responses.value()) {
                responseList.add(new DocumentResponse(response.code(), response.msg()));
            }
        }
        return responseList;
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
