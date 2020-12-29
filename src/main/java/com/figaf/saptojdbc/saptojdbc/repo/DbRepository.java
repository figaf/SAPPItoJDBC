package com.figaf.saptojdbc.saptojdbc.repo;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.errors.DatabaseConnectionError;
import com.figaf.saptojdbc.saptojdbc.errors.QueryExecutionError;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.sql.DriverManager.getConnection;

@Slf4j
public class DbRepository {
    
    private static final String UNKNOWN_COLUMN = "unknown_column";

    private final String repositoryUrl;

    public DbRepository(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
        log.info(" default character set is :" + Charset.defaultCharset().toString());
        log.info(" property: file.encoding is: " + System.getProperty("file.encoding"));
    }
    
    public List<Map<String, String>> executeRunQueryStatement(Credentials credentials, 
                                                              String sqlQuery, 
                                                              List<String> queryParams) {
        try (Connection con = getConnection(repositoryUrl, credentials.getUsername(), credentials.getPassword());
             PreparedStatement preparedStatement = con.prepareStatement(sqlQuery)
        ) {
            prepareParameters(queryParams, preparedStatement);
            long ts1 = System.currentTimeMillis();
            ResultSet rs = preparedStatement.executeQuery();
            long ts2 = System.currentTimeMillis();
            long queryExecutionDuration = ts2 - ts1;
            log.debug(" query was executed successfully in: {} ms ", queryExecutionDuration);
            return processRunQueryResultSet(rs);
        } catch (SQLException e) {
            log.error("exception happened during fetching data, reason:  " + e.getMessage(), e);
            throw new QueryExecutionError(e.getMessage());
        }
    }
    
    public boolean ping(Credentials credentials){
        try (Connection con = getConnection(repositoryUrl, credentials.getUsername(), credentials.getPassword())
        ) {
            return true;
        }
        catch (SQLException e) {
            throw new DatabaseConnectionError(e.getMessage());
        }
    }

    private List<Map<String, String>> processRunQueryResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, String>> result = new ArrayList<>();
        while (rs.next()) {
            addColumnContent(rs, metaData, columnCount)
                .ifPresent(result::add);
        }
        return result;
    }

    private Optional<Map<String, String>> addColumnContent(ResultSet rs,
                                                           ResultSetMetaData metaData,
                                                           int columnCount) {
        try {
            Map<String, String> rowEntry = new LinkedHashMap<>();
            for (int i = 0; i < columnCount; i++ ) {
                int columnIndex = i+1;
                String columnName = determineColumnName(metaData, columnIndex);
                String value = rs.getString(columnIndex);
                debugRowContent(rs, columnIndex, columnName, value);
                rowEntry.put(columnName, value);
            }
            if (log.isDebugEnabled()) {
                log.debug("end of row");
            }
            return Optional.of(rowEntry);
        } catch (Exception t) {
            log.error("Error occurred during traversing results, row will be skipped, reason: " + t.getMessage(), 
                t);
            return Optional.empty();
        }
    }
    
    private String determineColumnName(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        String columnName = metaData.getColumnName(columnIndex);
        if (null == columnName || columnName.length() == 0 ) {
            columnName = UNKNOWN_COLUMN;
        }
        return columnName;
    }

    private void debugRowContent(ResultSet rs, int columnIndex, String columnName, String value) throws SQLException {
        if (log.isDebugEnabled()) {
            byte[] bytes = rs.getBytes(columnIndex);
            if (null != bytes) {
                String encodedBytes = Base64.getEncoder().encodeToString(bytes);
                log.debug("new column: column name: {}, column index: {}, value: {}, rawBytesBase64: {}",
                    columnName, columnIndex, value, encodedBytes);
            }
        }
    }
    
    private void prepareParameters(List<String> queryParams, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < queryParams.size(); i++) {
            String queryValue = queryParams.get(i);
            preparedStatement.setString(i+1, queryValue);
        }
    }
}
