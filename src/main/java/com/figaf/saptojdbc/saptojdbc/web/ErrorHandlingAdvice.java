package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.errors.DatabaseConnectionError;
import com.figaf.saptojdbc.saptojdbc.errors.EmptySelectedSectionException;
import com.figaf.saptojdbc.saptojdbc.errors.EmptyTableException;
import com.figaf.saptojdbc.saptojdbc.errors.ErrorResponse;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidFormatForUsernamePasswordToken;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidInputDataException;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidTableNameException;
import com.figaf.saptojdbc.saptojdbc.errors.MissingAuthInfoException;
import com.figaf.saptojdbc.saptojdbc.errors.QueryExecutionError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

import static com.figaf.saptojdbc.saptojdbc.errors.ErrorMessages.AUTH_INFO_MISSED;
import static com.figaf.saptojdbc.saptojdbc.errors.ErrorMessages.INVALID_INPUT_DATA_ERROR;
import static com.figaf.saptojdbc.saptojdbc.errors.ErrorMessages.QUERY_EXECUTION_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
@Slf4j
public class ErrorHandlingAdvice {

    @ExceptionHandler(MissingAuthInfoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorResponse handleBadAuthorization( MissingAuthInfoException e){
        log.warn("Missing authentication info.Please provide valid credentials in order to access this resource", e);
        return new ErrorResponse(AUTH_INFO_MISSED);
    }

    @ExceptionHandler(InvalidFormatForUsernamePasswordToken.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorResponse handleBadAuthToken(InvalidFormatForUsernamePasswordToken e){
        log.error("Invalid format for provided authentication token", e);
        return new ErrorResponse(AUTH_INFO_MISSED);
    }

    @ExceptionHandler(EmptySelectedSectionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleSelectionSectionIsEmpty(EmptySelectedSectionException e){
        log.error("Empty selection props", e);
        return new ErrorResponse(QUERY_EXECUTION_ERROR);
    }

    @ExceptionHandler(EmptyTableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleEmptyTableSectionCase(EmptyTableException e){
        log.error("Table property is mandatory! ", e);
        return new ErrorResponse(QUERY_EXECUTION_ERROR);
    }

    @ExceptionHandler(QueryExecutionError.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleQueryExecutionErrorCase(QueryExecutionError e){
        String errorMessage = QUERY_EXECUTION_ERROR + e.getMessage();
        log.error(errorMessage, e);
        return new ErrorResponse(errorMessage);
    }
    
    @ExceptionHandler(InvalidInputDataException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleInvalidInputData(InvalidInputDataException e){
        String errorMessage = INVALID_INPUT_DATA_ERROR + e.getMessage();
        log.error(errorMessage, e);
        return new ErrorResponse(errorMessage);
    }

    @ExceptionHandler(DatabaseConnectionError.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleDatabaseConnectionErrorsCase(DatabaseConnectionError e){
        log.error("Can't connect to the database, database settings are probably invalid! ", e);
        Map<String, String> result = new HashMap<>();
        result.put("status", "down");
        result.put("message", e.getMessage());
        return result;
    }

    @ExceptionHandler(InvalidTableNameException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleInvalidTableName(InvalidTableNameException e){ 
        return new ErrorResponse("Error occurred near provided table name, submitted table name is invalid");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleDefaultError(Exception e){
        String errorMessage = "Unexpected error occurred, reason:" + e.getMessage();
        log.error(errorMessage, e);
        return new ErrorResponse(errorMessage);
    }
}
