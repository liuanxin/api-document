package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.*;
import com.github.liuanxin.api.model.DocumentCopyright;
import com.github.liuanxin.api.model.DocumentModule;
import com.github.liuanxin.api.model.DocumentResponse;
import com.github.liuanxin.api.model.DocumentUrl;
import com.github.liuanxin.api.util.ParamHandler;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApiIgnore
@RestController
@RequestMapping(DocumentController.PARENT_URL_PREFIX)
public class DocumentController {

    static final String PARENT_URL_PREFIX = "/api";
    private static final String VERSION_URL = "/version";
    private static final String INFO_URL = "/info";
    private static final String EXAMPLE_URL = "/example/{id}.json";

    private static final Set<String> IGNORE_URL_SET = Collections.singleton(
            "/error"
    );
    private static final String CLASS_SUFFIX = "Controller";

    private static List<DocumentModule> module_list = null;
    private static Map<String, DocumentUrl> url_map = null;
    private static final Lock LOCK = new ReentrantLock();

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapping;

    @Autowired
    private DocumentCopyright documentCopyright;

    @GetMapping(VERSION_URL)
    public DocumentCopyright urlVersion() {
        if (Tools.isBlank(documentCopyright) || documentCopyright.isOnline()) {
            return null;
        } else {
            return documentCopyright;
        }
    }

    @GetMapping(INFO_URL)
    public List<DocumentModule> url() {
        if (Tools.isBlank(documentCopyright) || documentCopyright.isOnline()) {
            return Collections.emptyList();
        }
        if (Tools.isEmpty(module_list)) {
            init(mapping, documentCopyright);
        }
        return module_list;
    }

    @GetMapping(EXAMPLE_URL)
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
            if (Tools.isNotEmpty(url_map) && Tools.isNotEmpty(module_list)) {
                return;
            }
            Map<String, DocumentModule> moduleMap = Tools.newLinkedHashMap();
            Map<String, DocumentUrl> urlMap = Tools.newLinkedHashMap();
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                if (Tools.isNotBlank(requestMappingInfo) && Tools.isNotBlank(handlerMethod) && wasJsonApi(handlerMethod)) {
                    // 没忽略的才统计
                    ApiIgnore ignore = getAnnotation(handlerMethod, ApiIgnore.class);
                    if (Tools.isBlank(ignore) || !ignore.value()) {
                        Set<String> urlArray = requestMappingInfo.getPatternsCondition().getPatterns();
                        Set<RequestMethod> methodArray = requestMappingInfo.getMethodsCondition().getMethods();
                        if (!ignore(urlArray, methodArray, copyright.getIgnoreUrlSet())) {
                            DocumentUrl url = new DocumentUrl();
                            // url
                            url.setUrl(Tools.toStr(urlArray));
                            // method : get, post, put...
                            url.setMethod(Tools.toStr(methodArray));
                            // param
                            url.setParamList(ParamHandler.handlerParam(handlerMethod));
                            // response
                            ApiResponses responses = getAnnotation(handlerMethod, ApiResponses.class);
                            if (Tools.isNotBlank(responses)) {
                                ApiResponse[] responseArr = responses.value();
                                if (responseArr.length > 0) {
                                    List<DocumentResponse> responseList = new ArrayList<>();
                                    for (ApiResponse response : responseArr) {
                                        responseList.add(new DocumentResponse(response.code(), response.msg()));
                                    }
                                    url.setResponseList(responseList);
                                }
                            }

                            String method = handlerMethod.toString();
                            // return param
                            url.setReturnList(ReturnHandler.handlerReturn(method, copyright.isReturnRecordLevel()));
                            // return json
                            url.setReturnJson(ReturnHandler.handlerReturnJson(method));

                            // meta info
                            ApiMethod apiMethod = getAnnotationByMethod(handlerMethod, ApiMethod.class);
                            if (Tools.isNotBlank(apiMethod)) {
                                url.setTitle(apiMethod.title());
                                url.setDesc(apiMethod.desc());
                                url.setDevelop(apiMethod.develop());
                                url.setIndex(apiMethod.index());
                            }
                            // 注释是否写在返回示例里面(从全局获取即可, 没有必要在 ApiMethod 上加一个更颗粒的配置来处理)
                            url.setCommentInReturnExample(copyright.isCommentInReturnExample());
                            url.setExampleUrl(getExampleUrl(url.getId()));

                            urlMap.put(url.getId(), url);
                            // add DocumentUrl to DocumentModule
                            ApiGroup apiGroup = getAnnotation(handlerMethod, ApiGroup.class);
                            if (Tools.isBlank(apiGroup)) {
                                // 如果在类上没有标注解, 则使用 类名, 如果类名中包含有 Controller 则去掉
                                String className = handlerMethod.getBeanType().getSimpleName();
                                String info = className;
                                if (className.contains(CLASS_SUFFIX)) {
                                    info = className.substring(0, className.indexOf(CLASS_SUFFIX));
                                }
                                addGroup(moduleMap, 0, info.toLowerCase() + "-" + className, url);
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
            List<DocumentModule> moduleList = new ArrayList<DocumentModule>();
            if (Tools.isNotEmpty(modules)) {
                for (DocumentModule module : modules) {
                    // 模块里面的地址列表从小到大排序
                    Collections.sort(module.getUrlList(), new Comparator<DocumentUrl>() {
                        @Override
                        public int compare(DocumentUrl o1, DocumentUrl o2) {
                            return o1.getIndex() - o2.getIndex();
                        }
                    });
                    moduleList.add(module);
                }
                // 模块从小到大排序
                Collections.sort(moduleList, new Comparator<DocumentModule>() {
                    @Override
                    public int compare(DocumentModule o1, DocumentModule o2) {
                        return o1.getIndex() - o2.getIndex();
                    }
                });
            }
            module_list = moduleList;
        } finally {
            LOCK.unlock();
        }
    }

    private static String getExampleUrl(String param) {
        String domain = Tools.getDomain();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        String exampleUrl = domain + Tools.addPrefix(PARENT_URL_PREFIX) + Tools.addPrefix(EXAMPLE_URL);
        return exampleUrl.replaceFirst("\\{.*?\\}", param);
    }

    /** 添加模块组 */
    private static void addGroup(Map<String, DocumentModule> moduleMap, int index, String group, DocumentUrl url) {
        DocumentModule module = moduleMap.get(group);
        if (Tools.isBlank(module)) {
            module = new DocumentModule(group);
            if (index > 0) {
                module.setIndex(index);
            }
        }
        module.addUrl(url);
        moduleMap.put(group, module);
    }

    /** 某些 url 需要忽略 */
    private static boolean ignore(Set<String> urlSet, Set<RequestMethod> methodSet,
                                  Set<String> ignoreUrlSet) {
        if (Tools.isEmpty(ignoreUrlSet)) {
            ignoreUrlSet = Tools.sets();
        }
        ignoreUrlSet.addAll(IGNORE_URL_SET);

        List<String> methodList = new ArrayList<>();
        for (RequestMethod method : methodSet) {
            methodList.add(method.name());
        }
        for (String ignoreUrl : ignoreUrlSet) {
            if (!ignoreUrl.startsWith("/")) {
                ignoreUrl = "/" + ignoreUrl;
            }
            if (ignoreUrl.contains("*")) {
                // 通配符
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
                // 全匹配
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
