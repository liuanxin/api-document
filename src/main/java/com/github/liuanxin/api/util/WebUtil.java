package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.*;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.*;

public final class WebUtil {

    public static List<DocumentInfo> getProjects(Map<String, String> projectMap) {
        if (Tools.isNotEmpty(projectMap)) {
            int s = projectMap.size();
            ThreadPoolExecutor executor = new ThreadPoolExecutor(s, s, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            List<DocumentInfo> returnList = new ArrayList<>();
            for (Map.Entry<String, String> entry : projectMap.entrySet()) {
                final String moduleName = entry.getKey();
                final String moduleUrl = entry.getValue();
                if (Tools.isNotEmpty(moduleName) && Tools.isNotEmpty(moduleUrl)) {
                    List<Future<DocumentInfo>> futureList = new ArrayList<>();
                    futureList.add(executor.submit(new Callable<DocumentInfo>() {
                        @Override
                        public DocumentInfo call() {
                            String u = moduleUrl.endsWith("/") ? moduleUrl.substring(0, moduleUrl.length() - 1) : moduleUrl;
                            String requestInfo = HttpUtil.get(u + ApiConst.PARENT_URL_PREFIX + ApiConst.INFO_URL);
                            ReturnInfo projectInfo = Tools.toObject(requestInfo, ReturnInfo.class);
                            return Tools.isBlank(projectInfo) ? null : projectInfo.fillModule(moduleName, moduleUrl);
                        }
                    }));
                    for (Future<DocumentInfo> future : futureList) {
                        try {
                            DocumentInfo info = future.get();
                            if (Tools.isNotBlank(info)) {
                                returnList.add(info);
                            }
                        } catch (InterruptedException | ExecutionException ignore) {
                        }
                    }
                }
            }
            return returnList;
        } else {
            return Collections.emptyList();
        }
    }

    public static DocumentCopyright copyright(DocumentCopyright copyright, List<DocumentModule> moduleList) {
        if (Tools.isNotEmpty(moduleList)) {
            int apiCount = 0;
            for (DocumentModule module : moduleList) {
                apiCount += module.getUrlList().size();
            }
            copyright.setGroupCount(moduleList.size()).setApiCount(apiCount);
        }
        return copyright;
    }

    public static DocumentInfoAndUrlMap infoAndUrlMap(RequestMappingHandlerMapping mapping,
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
                            document.setUseGlobalParam("1");
                        } else {
                            document.setUseGlobalParam(apiTokens.useGlobal() ? "1" : ApiConst.EMPTY);

                            List<DocumentParam> extraParams = new LinkedList<>();
                            for (ApiToken token : apiTokens.value()) {
                                extraParams.add(DocumentParam.buildToken(token));
                            }
                            if (extraParams.size() > 0) {
                                paramList.addAll(0, extraParams);
                            }
                        }
                        document.setRequestBody(hasRequestBody(handlerMethod) ? "1" : ApiConst.EMPTY);
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
                        String id = document.getId();
                        document.setExampleUrl(getExampleUrl(id));
                        // response
                        document.setResponseList(methodResponse(handlerMethod, commentInReturn, recordLevel));

                        documentMap.put(id, document);
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
            returnType = ApiConst.EMPTY;
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
        return Tools.isBlank(res) || Tools.isBlank(res.getResponse())
                ? null
                : getReturnType(res.getResponse(), res.getGenericParent(), res.getGeneric(), res.getGenericChild());
    }

    private static List<DocumentResponse> globalResponse(List<DocumentResponse> globalResponse,
                                                         boolean globalCommentInReturn,
                                                         boolean globalRecordLevel) {
        for (DocumentResponse response : globalResponse) {
            String type = getReturnTypeByResponse(response);
            if (Tools.isNotBlank(type)) {
                String method = response.getCode() + ":" + response.getMsg();
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
        return ApiConst.ID_URL_PATTERN.matcher(ApiConst.PARENT_URL_PREFIX + ApiConst.EXAMPLE_URL).replaceFirst(param);
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
