package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.*;
import com.github.liuanxin.api.model.*;
import com.github.liuanxin.api.util.ParamHandler;
import com.github.liuanxin.api.util.Requests;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
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

@Configuration("document-collect-controller")
@ConditionalOnBean({ DocumentCopyright.class })
@ApiIgnore
@ResponseBody
@RequestMapping(DocumentController.PARENT_URL_PREFIX)
public class DocumentController {

    static final String PARENT_URL_PREFIX = "/api";

    private static final String VERSION_URL = "/version";
    private static final String VERSION_CLEAR = "/clear";
    private static final String INFO_URL = "/info";
    private static final String EXAMPLE_URL = "/example/{id}.json";
    private static final String PRODUCES = "application/json; charset=UTF-8";

    private static final String CLASS_SUFFIX = "Controller";

    // local cache
    private static String document_str = null;
    private static DocumentInfo document_info = null;
    private static Map<String, DocumentUrl> url_map = null;

    private static final Lock LOCK = new ReentrantLock();

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapping;

    @Autowired
    private DocumentCopyright documentCopyright;

    @GetMapping(value = VERSION_URL, produces = PRODUCES)
    public String urlVersion() {
        if (Tools.isBlank(documentCopyright) || documentCopyright.isOnline()) {
            return null;
        } else {
            if (Tools.isBlank(document_str)) {
                init(mapping, documentCopyright);

                if (Tools.isNotBlank(document_info)) {
                    List<DocumentModule> moduleList = document_info.getModuleList();
                    if (Tools.isNotEmpty(moduleList)) {
                        int apiCount = 0;
                        for (DocumentModule module : moduleList) {
                            apiCount += module.getUrlList().size();
                        }
                        documentCopyright.setGroupCount(moduleList.size()).setApiCount(apiCount);
                        // don't need this
                        document_info = null;
                    }
                }
            }
            return Tools.toJson(documentCopyright);
        }
    }

    @PostMapping(value = VERSION_CLEAR, produces = PRODUCES)
    public String clear() {
        if (document_str != null || document_info != null || url_map != null) {
            LOCK.lock();
            try {
                if (document_str != null || document_info != null || url_map != null) {
                    document_str = null;
                    document_info = null;
                    url_map = null;
                }
            } finally {
                LOCK.unlock();
            }
        }
        return "clear";
    }

    @GetMapping(value = INFO_URL, produces = PRODUCES)
    public String url() {
        if (Tools.isBlank(documentCopyright) || documentCopyright.isOnline()) {
            return Tools.EMPTY;
        }
        if (Tools.isBlank(document_str)) {
            init(mapping, documentCopyright);
        }
        return document_str;
    }

    @GetMapping(value = EXAMPLE_URL, produces = PRODUCES)
    public String urlExample(@PathVariable("id") String id) {
        if (Tools.isBlank(documentCopyright) || documentCopyright.isOnline()) {
            return Tools.EMPTY;
        }
        if (Tools.isEmpty(url_map)) {
            init(mapping, documentCopyright);
        }
        return url_map.get(id).getReturnJson();
    }

    private static void init(RequestMappingHandlerMapping mapping, DocumentCopyright copyright) {
        LOCK.lock();
        try {
            if (Tools.isNotEmpty(url_map) && Tools.isNotBlank(document_str)) {
                return;
            }
            Map<String, DocumentModule> moduleMap = Tools.newLinkedHashMap();
            Map<String, DocumentUrl> urlMap = Tools.newLinkedHashMap();
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
                            DocumentUrl url = new DocumentUrl();
                            // url
                            url.setUrl(Tools.toStr(urlArray));
                            // method : get, post, put...
                            url.setMethod(Tools.toStr(methodArray));
                            // param
                            url.setParamList(ParamHandler.handlerParam(handlerMethod));
                            // response
                            url.setResponseList(handlerResponse(handlerMethod));

                            String method = handlerMethod.toString();
                            // return param
                            url.setReturnList(ReturnHandler.handlerReturn(method));
                            // return json
                            url.setReturnJson(ReturnHandler.handlerReturnJson(method));

                            // meta info
                            ApiMethod apiMethod = getAnnotationByMethod(handlerMethod, ApiMethod.class);
                            if (Tools.isNotBlank(apiMethod)) {
                                url.setTitle(apiMethod.title());
                                url.setDesc(apiMethod.desc());
                                url.setDevelop(apiMethod.develop());
                                url.setIndex(apiMethod.index());
                                url.setCommentInReturnExample(apiMethod.commentInReturnExample());
                                url.setReturnRecordLevel(apiMethod.returnRecordLevel());
                                url.setCommentInReturnExampleWithLevel(apiMethod.commentInReturnExampleWithLevel());
                            } else {
                                url.setCommentInReturnExample(copyright.isCommentInReturnExample());
                                url.setReturnRecordLevel(copyright.isReturnRecordLevel());
                            }
                            url.setExampleUrl(getExampleUrl(url.getId()));

                            urlMap.put(url.getId(), url);
                            // add DocumentUrl to DocumentModule
                            ApiGroup apiGroup = getAnnotation(handlerMethod, ApiGroup.class);
                            if (Tools.isBlank(apiGroup)) {
                                // if no annotation on class, use ClassName(if className include Controller then remove)
                                String className = handlerMethod.getBeanType().getSimpleName();
                                String info = className;
                                if (className.contains(CLASS_SUFFIX)) {
                                    info = className.substring(0, className.indexOf(CLASS_SUFFIX));
                                }
                                addGroup(moduleMap, 0, info + "-" + className, url);
                            } else {
                                int index = apiGroup.index();
                                for (String group : apiGroup.value()) {
                                    if (Tools.isNotBlank(group)) {
                                        addGroup(moduleMap, index, group, url);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            url_map = urlMap;

            Collection<DocumentModule> modules = moduleMap.values();
            List<DocumentModule> moduleList = new ArrayList<>();
            if (Tools.isNotEmpty(modules)) {
                for (DocumentModule module : modules) {
                    Collections.sort(module.getUrlList());
                    moduleList.add(module);
                }
                Collections.sort(moduleList);
            }
            DocumentInfo documentInfo = new DocumentInfo(copyright.getGlobalResponse(), moduleList);
            document_str = Tools.toJson(documentInfo);
            document_info = documentInfo;
        } finally {
            LOCK.unlock();
        }
    }

    private static List<DocumentResponse> handlerResponse(HandlerMethod handlerMethod) {
        List<DocumentResponse> responseList = new ArrayList<>();
        ApiResponses responses = getAnnotation(handlerMethod, ApiResponses.class);
        if (Tools.isNotBlank(responses)) {
            ApiResponse[] responseArr = responses.value();
            if (responseArr.length > 0) {
                for (ApiResponse response : responseArr) {
                    responseList.add(new DocumentResponse(response.code(), response.msg()));
                }
            }
        }
        return responseList;
    }

    private static final Pattern ID_URL_PATTERN = Pattern.compile("\\{.*?\\}");
    private static String getExampleUrl(String param) {
        String domain = Requests.getDomain();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        String exampleUrl = domain + Tools.addPrefix(PARENT_URL_PREFIX) + Tools.addPrefix(EXAMPLE_URL);
        // return exampleUrl.replaceFirst("\\{.*?\\}", param);
        return ID_URL_PATTERN.matcher(exampleUrl).replaceFirst(param);
    }

    private static void addGroup(Map<String, DocumentModule> moduleMap, int index, String group, DocumentUrl url) {
        DocumentModule module = moduleMap.get(group);
        if (Tools.isBlank(module)) {
            module = new DocumentModule(group);
            if (index > 0) {
                module.setIndex(index);
            }
        } else if (index != 0 && module.getIndex() > index) {
            // if set multi module and different index, use the smaller
            module.setIndex(index);
        }
        module.addUrl(url);
        moduleMap.put(group, module);
    }

    private static boolean ignoreUrl(Set<String> urlSet, Set<RequestMethod> methodSet, Set<String> ignoreUrlSet) {
        if (Tools.isEmpty(ignoreUrlSet)) {
            ignoreUrlSet = Tools.sets();
        }

        List<String> methodList = new ArrayList<>();
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
        ResponseBody annotation = getAnnotation(handlerMethod, ResponseBody.class);
        if (Tools.isNotBlank(annotation)) {
            return true;
        }
        RestController controller = getAnnotationByClass(handlerMethod, RestController.class);
        return Tools.isNotBlank(controller);
    }

    private static <T extends Annotation> T getAnnotationByMethod(HandlerMethod handlerMethod, Class<T> clazz) {
        return handlerMethod.getMethodAnnotation(clazz);
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
