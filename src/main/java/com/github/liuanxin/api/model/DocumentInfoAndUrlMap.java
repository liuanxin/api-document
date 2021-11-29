package com.github.liuanxin.api.model;

import java.util.List;
import java.util.Map;

public class DocumentInfoAndUrlMap {

    private DocumentInfo documentInfo;
    private Map<String, DocumentUrl> documentMap;

    public DocumentInfoAndUrlMap() {
    }

    public DocumentInfoAndUrlMap(DocumentInfo documentInfo, Map<String, DocumentUrl> documentMap) {
        this.documentInfo = documentInfo;
        this.documentMap = documentMap;
    }


    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }
    public DocumentInfoAndUrlMap setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
        return this;
    }

    public Map<String, DocumentUrl> getDocumentMap() {
        return documentMap;
    }
    public DocumentInfoAndUrlMap setDocumentMap(Map<String, DocumentUrl> documentMap) {
        this.documentMap = documentMap;
        return this;
    }


    public void appendDocument(List<DocumentInfo> projects) {
        documentInfo.append(projects);
    }
}
