package com.figaf.saptojdbc.saptojdbc.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RunQueryInputDto {
    
    private String topLevelPrefix;
    private String topLevelTag;
    private String topLevelSchema;
    
    private List<RunSqlRequest> runSqlRequest = new ArrayList<>();
    
    public void add(RunSqlRequest rq) {
        this.runSqlRequest.add(rq);
    }
}
