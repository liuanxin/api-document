package com.github.liuanxin.api.web;

import com.github.liuanxin.api.annotation.ApiIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.*;
import com.github.liuanxin.api.util.Tools;
import com.github.liuanxin.api.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApiIgnore
@RestController("document-collect-controller")
@RequestMapping(ApiConst.PARENT_URL_PREFIX)
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private static final Lock LOCK = new ReentrantLock();

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


    @PostMapping(value = ApiConst.VERSION_CLEAR, produces = ApiConst.PRODUCES)
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

    @GetMapping(value = ApiConst.EXAMPLE_URL, produces = ApiConst.PRODUCES)
    public String urlExample(@PathVariable(ApiConst.PLACEHOLDER) String id) {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect(mapping, copyright);
            if (Tools.isBlank(url_map)) {
                return ApiConst.EMPTY;
            } else {
                DocumentUrl document = url_map.get(id);
                return Tools.isBlank(document) ? ApiConst.EMPTY : document.getReturnJson();
            }
        }
    }

    @GetMapping(value = ApiConst.INFO_URL, produces = ApiConst.PRODUCES)
    public String url() {
        if (Tools.isBlank(copyright) || copyright.isOnline()) {
            return ApiConst.EMPTY;
        } else {
            collect(mapping, copyright);
            return return_info;
        }
    }

    private void collect(RequestMappingHandlerMapping mapping, DocumentCopyright copyright) {
        if (Tools.isBlank(return_info) && Tools.isBlank(url_map)) {
            LOCK.lock();
            try {
                if (Tools.isBlank(return_info) && Tools.isBlank(url_map)) {
                    DocumentInfoAndUrlMap infoAndUrlMap = WebUtil.infoAndUrlMap(mapping, copyright);
                    infoAndUrlMap.appendDocument(WebUtil.getProjects(copyright.getProjectMap()));
                    DocumentInfo document = infoAndUrlMap.getDocumentInfo();
                    DocumentCopyright documentCopyright = WebUtil.copyright(copyright, document.getModuleList());

                    return_info = Tools.toJson(new ReturnInfo(document, documentCopyright));
                    url_map = infoAndUrlMap.getDocumentMap();
                }
            } finally {
                LOCK.unlock();
            }
        }
    }
}
