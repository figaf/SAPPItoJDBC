package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.readResourceFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RunQueryParserTest {
    
    public static final String REFERENCE_RUN_QUERY_STATEMENT = "" +
        "SELECT\n" +
        "                Ans.LoenLoebenr AS Loennummer,\n" +
        "                Dimensions.Person_CPR_T2.Fornavn AS Fornavn,\n" +
        "                Dimensions.Person_CPR_T2.Efternavn AS Efternavn,\n" +
        "                Meta.Now.Now\n" +
        "                FROM Dimensions.Person_CPR_T2\n" +
        "                RIGHT OUTER JOIN Dimensions.SLS_ErAnsatI_T1 ON ( Dimensions.Person_CPR_T2.SLS_ErAnsatIID = Dimensions.SLS_ErAnsatI_T1.SLS_ErAnsatIID )\n" +
        "                       WHERE\n" +
        "                Meta.Now.Now >= Dimensions.Person_CPR_T2.DW_Valid_From\n" +
        "                AND Meta.Now.Now <= Dimensions.Person_CPR_T2.DW_Valid_To";

    private RunQueryParser queryParser;
    public static final String REFERENCE_SELECT_STATEMENT = "SELECT DatasaetID, " +
        "Loengeneration, " +
        "Loenkoersel, " +
        "LoengruppeKode " +
        "from dbo.SLS_Forbrug";

    @BeforeEach
    void setUp() {
        this.queryParser = new RunQueryParser();
    }
    
    @Test
    void testParse() throws Exception {
        String input = readResourceFile("sql_query_input_sample.xml");
        RunQueryInputDto parsedInput = queryParser.parse(input);
        assertEquals("AddressData_jdbc_REQUEST_MT" , parsedInput.getTopLevelTag());
        assertEquals("ns1" , parsedInput.getTopLevelPrefix());
        assertEquals("https://figaf.com" , parsedInput.getTopLevelSchema());
        assertEquals("AddressData" , parsedInput.getRunSqlRequest().get(0).getFirstLevelTag());
        
        compareIgnoringWhiteSpaces(parsedInput.getRunSqlRequest().get(0).getAccessStatement());
        
        assertEquals(2, parsedInput.getRunSqlRequest().get(0).getKeysStatement().size());
        assertEquals("Loenkoersel", parsedInput.getRunSqlRequest().get(0).getKeysStatement().get(1).getName());
        assertEquals("3212", parsedInput.getRunSqlRequest().get(0).getKeysStatement().get(1).getValue());
    }

    @Test
    void testParseWithoutKeys() throws Exception {
        String input = readResourceFile("sql_query_input_without_keys.xml");
        RunQueryInputDto parsedInput = queryParser.parse(input);
        assertNotNull(parsedInput);
    }
    
    @Test
    void testParseQueryTable() throws Exception {
        String input = readResourceFile("select_from_table_sample.xml");
        RunQueryInputDto parsedInput = queryParser.parse(input);
        assertEquals("PersonaleDataKontering_jdbc_REQUEST_MT" , parsedInput.getTopLevelTag());
        assertEquals("ns1" , parsedInput.getTopLevelPrefix());
        assertEquals("https://figaf.com" , parsedInput.getTopLevelSchema());
        assertEquals("StatementName1" , parsedInput.getRunSqlRequest().get(0).getFirstLevelTag());
        assertEquals(REFERENCE_SELECT_STATEMENT, parsedInput.getRunSqlRequest().get(0).getAccessStatement());

        assertEquals(2, parsedInput.getRunSqlRequest().get(0).getKeysStatement().size());
        assertEquals("Loenkoersel", parsedInput.getRunSqlRequest().get(0).getKeysStatement().get(1).getName());
        assertEquals("3212", parsedInput.getRunSqlRequest().get(0).getKeysStatement().get(1).getValue());
    }


    @Test
    void testParseSelectStatementWithMultipleStatements() throws Exception {
        String input = readResourceFile("sql_query_input_multiple_statements.xml");
        RunQueryInputDto runQueryInputDto = queryParser.parse(input);
        assertNotNull(runQueryInputDto);
        assertEquals(2, runQueryInputDto.getRunSqlRequest().size());
    }

    private void compareIgnoringWhiteSpaces(String accessStatement) {
        String[] referencePart = RunQueryParserTest.REFERENCE_RUN_QUERY_STATEMENT.split("\n");
        String[] actualPart = accessStatement.split("\n");
        assertEquals(referencePart.length, actualPart.length);
        for (int i =0; i< referencePart.length; i++) {
            String reference =  referencePart[i].trim();
            String actual =  actualPart[i].trim();
            assertEquals(reference, actual);
            
        }
    }
}