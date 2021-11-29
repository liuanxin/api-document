package com.github.liuanxin.api.model;

public class ReturnInfo {

    private DocumentInfo document;
    private DocumentCopyright copyright;


    public ReturnInfo() {
    }
    public ReturnInfo(DocumentInfo document, DocumentCopyright copyright) {
        this.document = document;
        this.copyright = copyright;
    }


    public DocumentInfo getDocument() {
        return document;
    }
    public ReturnInfo setDocument(DocumentInfo document) {
        this.document = document;
        return this;
    }

    public DocumentCopyright getCopyright() {
        return copyright;
    }
    public ReturnInfo setCopyright(DocumentCopyright copyright) {
        this.copyright = copyright;
        return this;
    }


    public DocumentInfo fillModule(String moduleName, String moduleUrl) {
        for (DocumentModule module : document.getModuleList()) {
            module.fillModule(moduleName);
            module.fillExampleUrl(moduleUrl);
        }
        return document;
    }
}
