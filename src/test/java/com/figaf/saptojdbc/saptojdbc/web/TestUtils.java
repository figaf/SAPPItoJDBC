package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.dto.RunSqlRequest;
import org.mockito.ArgumentCaptor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {

    public static final String REFERENCE_RUN_QUERY = "select " +
        " DatasaetID, Loengeneration, Loenkoersel, LoengruppeKode " +
        " from dbo.SLS_Forbrug";

    public static String encodeDummyCredentials() {
        return "Basic " +
            Base64.getEncoder().encodeToString("xxx:yyy".getBytes());
    }

    public static String readResourceFile(String filename) throws Exception {
        try (InputStream resourceAsStream = TestUtils.class
            .getClassLoader()
            .getResourceAsStream(filename)) {
            assertNotNull(resourceAsStream);
            return new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        }
    }

    public static void verifyPasswordCorrectness(ArgumentCaptor<Credentials> credentialsCaptor) {
        Credentials credentials = credentialsCaptor.getValue();
        assertNotNull(credentials);
        assertEquals("xxx", credentials.getUsername());
        assertEquals("yyy", credentials.getPassword());
    }

    public static RunQueryInputDto createDummyRunQueryInput() {
        RunQueryInputDto runQueryInputDto = new RunQueryInputDto();
        runQueryInputDto.setTopLevelTag("AddressData_jdbc_REQUEST_MT");
        runQueryInputDto.setTopLevelPrefix("ns1");
        runQueryInputDto.setTopLevelSchema("https://figaf.com");
        RunSqlRequest request = new RunSqlRequest();
        request.setFirstLevelTag("AddressData");
        request.setAccessStatement(REFERENCE_RUN_QUERY);
        runQueryInputDto.add(request);

        return runQueryInputDto;
    }
}
