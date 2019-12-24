package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.ApiIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.*;
import com.github.liuanxin.api.util.Tools;
import com.github.liuanxin.api.util.WebUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApiIgnore
@RestController(ApiConst.BEAN_NAME)
@RequestMapping(ApiConst.URL_PREFIX)
public class DocumentController {

    private static final Lock LOCK = new ReentrantLock();

    // local cache
    private static String return_info_cache = null;
    private static Map<String, DocumentUrl> url_map_cache = null;
    // local cache


    private final RequestMappingHandlerMapping mapping;
    private final DocumentCopyright copyright;
    public DocumentController(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping,
                              DocumentCopyright copyright) {
        this.mapping = mapping;
        this.copyright = copyright;
    }


    @PostMapping(value = ApiConst.URL_CLEAR, produces = ApiConst.PRODUCES)
    public int clear() {
        return_info_cache = null;
        url_map_cache = null;
        return 1;
    }

    @GetMapping(value = ApiConst.URL_EXAMPLE, produces = ApiConst.PRODUCES)
    public String urlExample(@PathVariable(ApiConst.PLACEHOLDER) String id) {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect();
            if (Tools.isBlank(url_map_cache)) {
                return ApiConst.EMPTY;
            } else {
                DocumentUrl document = url_map_cache.get(id);
                return Tools.isBlank(document) ? ApiConst.EMPTY : document.getReturnJson();
            }
        }
    }

    @GetMapping(value = ApiConst.URL_INFO, produces = ApiConst.PRODUCES)
    public String url() {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect();
            return return_info_cache;
        }
    }

    private void collect() {
        if (Tools.isBlank(return_info_cache) && Tools.isBlank(url_map_cache)) {
            LOCK.lock();
            try {
                if (Tools.isBlank(return_info_cache) && Tools.isBlank(url_map_cache)) {
                    DocumentInfoAndUrlMap infoAndUrlMap = WebUtil.infoAndUrlMap(mapping, copyright);
                    infoAndUrlMap.appendDocument(WebUtil.getProjects(copyright.getProjectMap()));
                    DocumentInfo document = infoAndUrlMap.getDocumentInfo();
                    DocumentCopyright documentCopyright = WebUtil.copyright(copyright, document.getModuleList());

                    return_info_cache = Tools.toJson(new ReturnInfo(document, documentCopyright));
                    url_map_cache = infoAndUrlMap.getDocumentMap();
                }
            } finally {
                LOCK.unlock();
            }
        }
    }
}
