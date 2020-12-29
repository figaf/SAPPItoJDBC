package com.figaf.saptojdbc.saptojdbc.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.createDummyRunQueryInput;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunQueryResponseBuilderTest {
    
    public static final String[] REFERENCE_RESPONSE_LINES = new String[] {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<ns1:AddressData_jdbc_REQUEST_MT_response xmlns:ns1=\"https://figaf.com\">",
            "<AddressData_response>",
            "<row>",
            "<LoengruppeKode>SAPPI</LoengruppeKode>", 
            "<DatasaetID>1</DatasaetID>",
            "<Loenkoersel/>",
            "<Loengeneration>2020</Loengeneration>",
            "</row>", 
            "</AddressData_response>", 
            "</ns1:AddressData_jdbc_REQUEST_MT_response>"
    };
    
    @Test
    void verifyDocumentCreation() throws Exception {

        RunQueryResponseBuilder runQueryResponseBuilder = new RunQueryResponseBuilder(createDummyRunQueryInput());
        runQueryResponseBuilder.addSection(createDummyRunQueryInput().getRunSqlRequest().get(0), createDummyResults());
        String responseDocument = runQueryResponseBuilder.transformDocumentToString();
        for (String line: REFERENCE_RESPONSE_LINES) {
            assertTrue(responseDocument.contains(line));
        }
    }

    private List<Map<String, String>> createDummyResults() {
        ArrayList<Map<String, String>> allRows = new ArrayList<>();
        Map<String, String> entry0 = new HashMap<>();
        entry0.put("LoengruppeKode","SAPPI");
        entry0.put("DatasaetID","1");
        entry0.put("Loenkoersel", null);
        entry0.put("Loengeneration",  "2020"); 
        allRows.add(entry0);
        return allRows;
    }
}