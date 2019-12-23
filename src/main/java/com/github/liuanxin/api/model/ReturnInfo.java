package com.github.liuanxin.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ReturnInfo {

    private DocumentInfo document;
    private DocumentCopyright copyright;

    public DocumentInfo fillModule(String moduleName, String moduleUrl) {
        for (DocumentModule module : document.getModuleList()) {
            module.fillModule(moduleName);
            module.fillExampleUrl(moduleUrl);
        }
        return document;
    }
}
