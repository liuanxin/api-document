package com.github.liuanxin.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentInfo {

    private List<DocumentParam> tokenList;
    private List<DocumentResponse> responseList;
    private List<DocumentModule> moduleList;
}
