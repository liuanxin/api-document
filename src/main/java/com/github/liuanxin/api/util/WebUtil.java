package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.*;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.*;

public final class WebUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtil.class);

    public static List<DocumentInfo> getProjects(Map<String, String> projectMap) {
        if (Tools.isNotEmpty(projectMap)) {
            List<DocumentInfo> returnList = new ArrayList<>();

            ThreadPoolExecutor executor = threadPool(projectMap.size());
            List<Future<DocumentInfo>> futureList = new ArrayList<>();
            for (Map.Entry<String, String> entry : projectMap.entrySet()) {
                final String name = entry.getKey();
                final String url = entry.getValue();
                if (Tools.isNotEmpty(name) && Tools.isNotEmpty(url)) {
                    futureList.add(executor.submit(new Callable<DocumentInfo>() {
                        @Override
                        public DocumentInfo call() {
                            String uri = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
                            String requestInfo = HttpUtil.get(uri + ApiConst.URL_PREFIX + ApiConst.URL_INFO);
                            ReturnInfo projectInfo = Tools.toObject(requestInfo, ReturnInfo.class);
                            return Tools.isBlank(projectInfo) ? null : projectInfo.fillModule(name, url);
                        }
                    }));
                }
            }
            for (Future<DocumentInfo> future : futureList) {
                try {
                    DocumentInfo info = future.get();
                    if (Tools.isNotBlank(info)) {
                        returnList.add(info);
                    }
                } catch (InterruptedException | ExecutionException ignore) {
                }
            }
            return returnList;
        } else {
            return Collections.emptyList();
        }
    }
    public static ThreadPoolExecutor threadPool(int size) {
        int cpus = Runtime.getRuntime().availableProcessors();

        int pool;
        BlockingQueue<Runnable> queue;
        if (size > cpus) {
            pool = cpus;
            queue = new LinkedBlockingQueue<>(size - cpus);
        } else {
            pool = size;
            queue = new SynchronousQueue<>();
        }
        return new ThreadPoolExecutor(pool, pool, 60L, TimeUnit.SECONDS, queue);
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
                    Set<String> urlSet = getUrl(requestMapping);
                    Set<String> methodSet = getMethod(requestMapping);
                    if (!ignoreUrl(urlSet, methodSet, copyright.getIgnoreUrlSet())) {
                        String method = handlerMethod.toString();
                        boolean innerRequestBody = hasInnerParamRequestBody(handlerMethod);
                        DocumentUrl document = new DocumentUrl();
                        // url
                        document.setUrl(String.join(", ", urlSet));
                        // method : get, post, put...
                        document.setMethod(String.join(", ", methodSet));
                        // param
                        List<DocumentParam> paramList;
                        if (innerRequestBody) {
                            String paramType = ReturnType.getRequestBodyParamTypeByMethod(handlerMethod);
                            String requestBodyJson = ReturnHandler.handlerReturnJson(method, paramType);
                            List<DocumentReturn> requestBodyParamList = ReturnHandler.handlerReturn(method, paramType);
                            document.setRequestBodyJson(DocumentUrl.commentJson(requestBodyJson, true, true, requestBodyParamList));
                            paramList = new ArrayList<>();
                        } else {
                            paramList = ParamHandler.handlerParam(handlerMethod);
                        }
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
                        document.setBasicParamRequestBody(hasBasicParamRequestBody(handlerMethod) ? "1" : ApiConst.EMPTY);
                        document.setInnerParamRequestBody(innerRequestBody ? "1" : ApiConst.EMPTY);
                        document.setParamList(paramList);

                        ApiMethod apiMethod = handlerMethod.getMethodAnnotation(ApiMethod.class);
                        String returnType = ReturnType.getReturnTypeByMethod(handlerMethod, apiMethod);
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

                        // ClassName(if className include Controller then remove)
                        String className = handlerMethod.getBeanType().getSimpleName();
                        String info = className;

                        String classSuffix = "controller";
                        String lowerClassName = className.toLowerCase();
                        if (lowerClassName.endsWith(classSuffix)) {
                            info = className.substring(0, lowerClassName.indexOf(classSuffix));
                        }
                        if (Tools.isBlank(apiGroup)) {
                            addGroup(moduleMap, 0, info + ApiConst.HORIZON + className, document);
                        } else {
                            int index = apiGroup.index();
                            for (String group : apiGroup.value()) {
                                if (Tools.isNotBlank(group)) {
                                    String groupName = group.contains(ApiConst.HORIZON)
                                            ? group : (info + ApiConst.HORIZON + group);
                                    addGroup(moduleMap, index, groupName, document);
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

    private static Set<String> getUrl(RequestMappingInfo requestMapping) {
        Set<String> urlSet = new LinkedHashSet<>();
        /*
        PathPatternsRequestCondition pathPatternsCondition = requestMapping.getPathPatternsCondition();
        if (Tools.isNotBlank(pathPatternsCondition)) {
            Set<PathPattern> patterns = pathPatternsCondition.getPatterns();
            if (Tools.isNotEmpty(patterns)) {
                for (PathPattern pattern : patterns) {
                    urlSet.add(pattern.getPatternString());
                }
            }
        }
        */
        try {
            Object condition = requestMapping.getClass().getMethod("getPathPatternsCondition").invoke(requestMapping);
            if (Tools.isNotBlank(condition)) {
                // noinspection unchecked
                Set<PathPattern> patterns = (Set<PathPattern>) condition.getClass().getMethod("getPatterns").invoke(condition);
                if (Tools.isNotEmpty(patterns)) {
                    for (PathPattern pattern : patterns) {
                        urlSet.add(pattern.getPatternString());
                    }
                }
            }
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("getPathPatternsCondition just support with spring-mvc 5.3", e);
            }
        }
        if (urlSet.isEmpty()) {
            PatternsRequestCondition condition = requestMapping.getPatternsCondition();
            if (Tools.isNotBlank(condition)) {
                urlSet.addAll(condition.getPatterns());
            }
        }
        return urlSet;
    }
    private static Set<String> getMethod(RequestMappingInfo requestMapping) {
        Set<String> methodSet = new LinkedHashSet<>();
        Set<RequestMethod> methodArray = requestMapping.getMethodsCondition().getMethods();
        if (Tools.isNotEmpty(methodArray)) {
            for (RequestMethod method : methodArray) {
                methodSet.add(method.name());
            }
        }
        return methodSet;
    }

    private static List<DocumentResponse> globalResponse(List<DocumentResponse> globalResponse,
                                                         boolean globalCommentInReturn,
                                                         boolean globalRecordLevel) {
        if (Tools.isEmpty(globalResponse)) {
            return Collections.emptyList();
        }
        for (DocumentResponse response : globalResponse) {
            String type = ReturnType.getReturnTypeByResponse(response);
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

                String type = ReturnType.getReturnTypeByAnnotation(Tools.first(apiResponse.type()));
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
        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        if (Tools.isNotEmpty(parameters) && parameters.length == 1) {
            MethodParameter parameter = parameters[0];
            RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
            return Tools.isNotBlank(requestBody);
        } else {
            return false;
        }
    }

    private static boolean hasBasicParamRequestBody(HandlerMethod handlerMethod) {
        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        if (Tools.isNotEmpty(parameters) && parameters.length == 1) {
            MethodParameter parameter = parameters[0];
            RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
            return Tools.isNotBlank(requestBody) && Tools.basicType(parameter.getParameterType());
        } else {
            return false;
        }
    }

    private static boolean hasInnerParamRequestBody(HandlerMethod handlerMethod) {
        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        if (Tools.isNotEmpty(parameters) && parameters.length == 1) {
            MethodParameter parameter = parameters[0];
            RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
            // return Tools.isNotBlank(requestBody) &&
            //        (Tools.innerType(parameter.getParameterType()) || Tools.innerType(parameter.getParameter().getParameterizedType()));
            return Tools.isNotBlank(requestBody);
        }
        return false;
    }

    private static String getExampleUrl(String param) {
        // return exampleUrl.replaceFirst("\\{.*?\\}", param);
        String url = ApiConst.URL_PREFIX + ApiConst.URL_EXAMPLE;
        return Requests.getDomain() + ApiConst.ID_URL_PATTERN.matcher(url).replaceFirst(param);
    }

    private static void addGroup(Map<String, DocumentModule> moduleMap, int index, String group, DocumentUrl url) {
        DocumentModule module = moduleMap.get(group);
        if (Tools.isBlank(module)) {
            module = new DocumentModule(group);
            module.setIndex(index);
        } else if (module.getIndex() > index) {
            // if set multiple-module and different index, use the smaller
            module.setIndex(index);
        }
        module.addUrl(url);
        moduleMap.put(group, module);
    }

    private static boolean ignoreUrl(Set<String> urlSet, Set<String> methodSet, Set<String> ignoreUrlSet) {
        if (Tools.isBlank(ignoreUrlSet)) {
            ignoreUrlSet = Tools.sets();
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
                    if (methodSet.contains(tmpMethod)) {
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
                    if (urlSet.contains(tmpUrl) && methodSet.contains(tmpMethod)) {
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
