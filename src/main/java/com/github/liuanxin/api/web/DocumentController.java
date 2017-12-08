package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.ApiGroup;
import com.github.liuanxin.api.annotation.ApiIgnore;
import com.github.liuanxin.api.annotation.ApiMethod;
import com.github.liuanxin.api.model.DocumentCopyright;
import com.github.liuanxin.api.model.DocumentModule;
import com.github.liuanxin.api.model.DocumentUrl;
import com.github.liuanxin.api.util.ParamHandler;
import com.github.liuanxin.api.util.ReturnHandler;
import com.github.liuanxin.api.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApiIgnore
@RestController
@ConditionalOnClass({ DocumentCopyright.class })
public class DocumentController {

    /** 没有办法标注 &#064;ApiIgnore 又需要忽略的请求, 只需要开头就可以了 */
    private static final List<String> IGNORE_URL_LIST = Utils.lists(
            "/error"
    );

    private static final String CLASS_SUFFIX = "Controller";


    private final RequestMappingHandlerMapping mapping;
    private final DocumentCopyright documentCopyright;

    private static List<DocumentModule> module_list = null;
    private static Map<String, DocumentUrl> url_map = null;
    private static final Lock LOCK = new ReentrantLock();

    @Autowired
    public DocumentController(RequestMappingHandlerMapping mapping, DocumentCopyright apiCopyright) {
        this.mapping = mapping;
        this.documentCopyright = apiCopyright;
    }

    @GetMapping("/api-version")
    public DocumentCopyright urlVersion() {
        return documentCopyright;
    }

    @GetMapping("/api-info")
    public List<DocumentModule> url() {
        if (documentCopyright == null || documentCopyright.isOnline()) {
            return Collections.emptyList();
        }
//        if (module_list == null) {
            init(mapping);
//        }
        return module_list;
    }

    @GetMapping("/api-example/{id}.json")
    public String urlExample(@PathVariable("id") String id) {
        if (documentCopyright == null || documentCopyright.isOnline()) {
            return Utils.EMPTY;
        }
//        if (url_map == null) {
            init(mapping);
//        }
        return url_map.get(id).getReturnJson();
    }

    private static void init(RequestMappingHandlerMapping mapping) {
        LOCK.lock();
        try {
//            if (url_map != null && module_list != null) {
//                return;
//            }
            Map<String, DocumentModule> moduleMap = Utils.newLinkedHashMap();
            Map<String, DocumentUrl> urlMap = Utils.newLinkedHashMap();
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                if (requestMappingInfo != null && handlerMethod != null && wasJsonApi(handlerMethod)) {
                    // 没忽略的才统计
                    ApiIgnore ignore = getAnnotation(handlerMethod, ApiIgnore.class);
                    if (ignore == null || !ignore.value()) {
                        Set<String> urlArray = requestMappingInfo.getPatternsCondition().getPatterns();
                        if (!ignore(urlArray)) {
                            DocumentUrl url = new DocumentUrl();
                            // url
                            url.setUrl(Utils.toStr(urlArray));
                            // method : get, post, put...
                            url.setMethod(Utils.toStr(requestMappingInfo.getMethodsCondition().getMethods()));
                            // param
                            url.setParamList(ParamHandler.handlerParam(handlerMethod));
                            // return param
                            url.setReturnList(ReturnHandler.handlerReturn(handlerMethod.toString()));
                            // return json
                            url.setReturnJson(ReturnHandler.handlerReturnJson(handlerMethod.toString()));

                            // meta info
                            ApiMethod apiMethod = getAnnotationByMethod(handlerMethod, ApiMethod.class);
                            if (apiMethod != null) {
                                url.setTitle(apiMethod.title()).setDesc(apiMethod.desc()).setDevelop(apiMethod.develop());
                            }

                            urlMap.put(url.getId(), url);
                            // add DocumentUrl to DocumentModule
                            ApiGroup apiGroup = getAnnotation(handlerMethod, ApiGroup.class);
                            if (apiGroup == null) {
                                // 如果在类上没有标注解, 则使用 类名, 如果类名中包含有 Controller 则去掉
                                String className = handlerMethod.getBeanType().getSimpleName();
                                String info = className;
                                if (className.contains(CLASS_SUFFIX)) {
                                    info = className.substring(0, className.indexOf(CLASS_SUFFIX));
                                }
                                addGroup(moduleMap, info.toLowerCase() + "-" + className, url);
                            } else {
                                for (String group : apiGroup.value()) {
                                    addGroup(moduleMap, group, url);
                                }
                            }
                        }
                    }
                }
            }
            url_map = urlMap;
            module_list = Utils.lists(moduleMap.values());
        } finally {
            LOCK.unlock();
        }
    }

    /** 添加模块组 */
    private static void addGroup(Map<String, DocumentModule> moduleMap, String group, DocumentUrl url) {
        DocumentModule module = moduleMap.get(group);
        if (module == null) {
            module = new DocumentModule(group);
        }
        module.addUrl(url);
        moduleMap.put(group, module);
    }
    /** 某些 url 需要忽略(只匹配 url, 无视 method) */
    private static boolean ignore(Set<String> urlList) {
        for (String url : urlList) {
            for (String ignoreUrl : IGNORE_URL_LIST) {
                if (url.startsWith(ignoreUrl)) {
                    return true;
                }
            }
        }
        return false;
    }
    /** 如果是一个 json api 就返回 true */
    private static boolean wasJsonApi(HandlerMethod handlerMethod) {
        ResponseBody annotation = getAnnotation(handlerMethod, ResponseBody.class);
        if (annotation != null) {
            return true;
        }
        RestController controller = getAnnotationByClass(handlerMethod, RestController.class);
        return controller != null;
    }
    /** 找方法上的注解 */
    private static <T extends Annotation> T getAnnotationByMethod(HandlerMethod handlerMethod, Class<T> clazz) {
        return handlerMethod.getMethodAnnotation(clazz);
    }
    /** 找类上的注解 */
    private static <T extends Annotation> T getAnnotationByClass(HandlerMethod handlerMethod, Class<T> clazz) {
        return AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), clazz);
    }
    /** 先找方法上的注解, 再找类上的注解 */
    private static <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> clazz) {
        T annotation = handlerMethod.getMethodAnnotation(clazz);
        if (annotation == null) {
            annotation = getAnnotationByClass(handlerMethod, clazz);
        }
        return annotation;
    }
}
