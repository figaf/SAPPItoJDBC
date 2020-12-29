package com.figaf.saptojdbc.saptojdbc.service;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.dto.RunSqlRequest;
import com.figaf.saptojdbc.saptojdbc.dto.ValueTuple;
import com.figaf.saptojdbc.saptojdbc.repo.DbRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class DataTransformationService {
    
    public static final int MAX_LOG_ENTRIES_OUTPUTS = 10;

    private final DbRepository dbRepository;

    public DataTransformationService(DbRepository dbRepository) {
        this.dbRepository = dbRepository;
    }
    
    public String executeRunQueryStatement(Credentials credentials,
                                         RunQueryInputDto inputDto) throws Exception {
        
        RunQueryResponseBuilder runQueryResponseBuilder = new RunQueryResponseBuilder(inputDto);
        
        for (RunSqlRequest request: inputDto.getRunSqlRequest()) {
            String sqlStatement = request.getAccessStatement().trim();
            List<String> queryParams = new ArrayList<>();
            sqlStatement = addWhereStatementIfNeeded(request, sqlStatement, queryParams);
            List<Map<String, String>> queryResults = this.dbRepository.executeRunQueryStatement(credentials,
                sqlStatement,
                queryParams);
            logFirstStatements(queryResults);
            runQueryResponseBuilder.addSection(request, queryResults);
        }
        return runQueryResponseBuilder.transformDocumentToString();
    }

    private void logFirstStatements(List<Map<String, String>> queryResults) {
        int counter = 0;
        for (Map<String, String> rowEntry: queryResults) {
            String rowAsString = rowEntryToString(rowEntry);
            log.debug("debug output for row: " + rowAsString);
            if (counter >= MAX_LOG_ENTRIES_OUTPUTS) {
                log.debug(" rest of result will be skipped ...");
                break;
            } else {
                counter++;    
            }
        }
    }

    private String rowEntryToString(Map<String, String> rowEntry) {
        StringBuilder rowAsString = new StringBuilder("rowEntry:[");
        for(String property: rowEntry.keySet()) {
            rowAsString.append(property)
                .append("=")
                .append(rowEntry.get(property))
                .append(",");
        }
        rowAsString.append("]");
        return rowAsString.toString();
    }

    public boolean ping (Credentials credentials) {
        return this.dbRepository.ping(credentials);
    }

    private String addWhereStatementIfNeeded(RunSqlRequest request, String sqlStatement, List<String> queryParams) {
        if (!request.getKeysStatement().isEmpty()) {
            sqlStatement = removeTrailingTerminatedCharIfPresent(sqlStatement);
            boolean isWhereKeyword = isWhereKeywordIncluded(sqlStatement);
            if(!isWhereKeyword) {
                sqlStatement += " where ";
                ValueTuple firstQueryParam = request.getKeysStatement().get(0);
                sqlStatement += firstQueryParam.getName() + " = ?";
                queryParams.add(firstQueryParam.getValue());
                sqlStatement = populateKeys(request, sqlStatement, queryParams, 1);
            } else {
                sqlStatement = populateKeys(request, sqlStatement, queryParams, 0);
            }
        }
        return sqlStatement;
    }

    private String populateKeys(RunSqlRequest request, 
                                String sqlStatement, 
                                List<String> queryParams, 
                                int startingIndex) {
        for (int i = startingIndex; i < request.getKeysStatement().size(); i++) {
            ValueTuple valueTuple = request.getKeysStatement().get(i);
            sqlStatement += " and " + valueTuple.getName() + " = ?";
            queryParams.add(valueTuple.getValue());
        }
        return sqlStatement;
    }

    private String removeTrailingTerminatedCharIfPresent(String sqlStatement) {
        boolean isExpressionTerminated = isSqlStatementTerminated(sqlStatement);
        if (isExpressionTerminated) {
            return sqlStatement.substring(0, sqlStatement.indexOf(";"));
        } else {
            return sqlStatement;
        }
    }

    private boolean isSqlStatementTerminated(String sqlStatement) {
        return sqlStatement.endsWith(";");
    }

    private boolean isWhereKeywordIncluded(String accessStatement) {
        return accessStatement.contains(" where ") 
            || accessStatement.contains(" WHERE ");
    }
}
