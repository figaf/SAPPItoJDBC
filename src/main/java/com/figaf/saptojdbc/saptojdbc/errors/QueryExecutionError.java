package com.figaf.saptojdbc.saptojdbc.errors;

public class QueryExecutionError extends RuntimeException {
    public QueryExecutionError(String message) {
        super(message);
    }
}
