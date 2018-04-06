package com.github.liuanxin.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentInfo {

    private List<DocumentResponse> responseList;
    private List<DocumentModule> moduleList;
}
