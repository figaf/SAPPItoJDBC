package com.figaf.saptojdbc.saptojdbc.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RunSqlRequest {

    private String firstLevelTag;

    private String accessStatement;
    private final List<ValueTuple> keysStatement = new ArrayList<>();

    public void addEntry(String propertyName, String propertyValue){
        this.keysStatement.add(new ValueTuple(propertyName, propertyValue));
    }
}
