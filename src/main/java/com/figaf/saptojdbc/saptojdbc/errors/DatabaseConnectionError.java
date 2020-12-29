package com.figaf.saptojdbc.saptojdbc.errors;

public class DatabaseConnectionError extends RuntimeException {


    public DatabaseConnectionError(String message) {
        super(message);
    }
}
