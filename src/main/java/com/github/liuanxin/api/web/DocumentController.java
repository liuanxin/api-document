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
@RestController(ApiConst.CONTROLLER_NAME)
@RequestMapping(ApiConst.URL_PREFIX)
public class DocumentController {

    private static final Lock LOCK = new ReentrantLock();

    // local cache
    private static volatile String return_info_cache = null;
    private static volatile Map<String, DocumentUrl> url_map_cache = null;
    // local cache


    private final RequestMappingHandlerMapping mapping;
    private final DocumentCopyright copyright;
    public DocumentController(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping,
            DocumentCopyright copyright
    ) {
        this.mapping = mapping;
        this.copyright = copyright;
    }


    @PostMapping(value = ApiConst.URL_CLEAR, produces = ApiConst.PRODUCES)
    public int clear() {
        if (Tools.isNotNull(return_info_cache) && Tools.isNotNull(url_map_cache)) {
            LOCK.lock();
            try {
                return_info_cache = null;
                url_map_cache = null;
            } finally {
                LOCK.unlock();
            }
        }
        return 1;
    }

    @GetMapping(value = ApiConst.URL_PROJECT, produces = ApiConst.PRODUCES)
    public String getProjectInfo(String p) {
        return copyright.getProjectInfo(p);
    }

    @GetMapping(value = ApiConst.URL_EXAMPLE, produces = ApiConst.PRODUCES)
    public String urlExample(@PathVariable(ApiConst.PLACEHOLDER) String id) {
        if (Tools.isNull(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect();
            if (Tools.isEmpty(url_map_cache)) {
                return ApiConst.EMPTY;
            } else {
                DocumentUrl document = url_map_cache.get(id);
                return Tools.isNull(document) ? ApiConst.EMPTY : document.getReturnJson();
            }
        }
    }

    @GetMapping(value = ApiConst.URL_INFO, produces = ApiConst.PRODUCES)
    public String getUrlInfo() {
        if (Tools.isNull(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect();
            return return_info_cache;
        }
    }

    private void collect() {
        if (Tools.isNull(return_info_cache) && Tools.isNull(url_map_cache)) {
            LOCK.lock();
            try {
                if (Tools.isNull(return_info_cache) && Tools.isNull(url_map_cache)) {
                    DocumentInfoAndUrlMap infoAndUrlMap = WebUtil.infoAndUrlMap(mapping, copyright);
                    if (copyright.isProjectMerge()) {
                        infoAndUrlMap.appendDocument(WebUtil.getProjects(copyright.getProjectMap()));
                    }
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
