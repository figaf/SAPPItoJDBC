package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.service.DataTransformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/run-query")
@Slf4j
public class RunQueryController implements SecurityAwareController {
    private final RunQueryParser runQueryParser;
    private final DataTransformationService dataTransformationService;

    
    public RunQueryController(RunQueryParser runQueryParser, 
                              DataTransformationService dataTransformationService) {
        this.runQueryParser = runQueryParser;
        this.dataTransformationService = dataTransformationService;
    }

    @PostMapping(
        produces = MediaType.APPLICATION_XML_VALUE,
        consumes = MediaType.APPLICATION_XML_VALUE
    )
    public String query(@RequestHeader HttpHeaders headers,
                                     @RequestBody String input) throws Exception {
        Credentials credentials = processAuthHeader(headers.get("Authorization"));
        RunQueryInputDto parsedInput = runQueryParser.parse(input);
        return dataTransformationService.executeRunQueryStatement(credentials, parsedInput);
    }
    
    @GetMapping(
        produces = APPLICATION_JSON_VALUE
    )
    public Map<String, String> ping(@RequestHeader HttpHeaders headers) {
        Credentials credentials = processAuthHeader(headers.get("Authorization"));
        boolean connectionResult = dataTransformationService.ping(credentials);
        Map<String, String> result = new HashMap<>();
        result.put("status", connectionResult ? "up" : "down");
        return result;
    }
}
