package com.figaf.saptojdbc.saptojdbc.service;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.repo.DbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.REFERENCE_RUN_QUERY;
import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.createDummyRunQueryInput;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataTransformationServiceTest {

    @Mock
    private DbRepository dbRepository;
    
    private DataTransformationService dataTransformationService;
    private static final String DEFAULT_USERNAME = "sa";
    
    private static final Credentials TEST_CREDENTIALS = new Credentials(DEFAULT_USERNAME, "");

    @BeforeEach
    void setUp() {
        assertNotNull(dbRepository);
        dataTransformationService = new DataTransformationService(dbRepository);
    }

    @Test
    void testExecuteRunQueryStatement() throws Exception {
        RunQueryInputDto runQuery = createDummyRunQueryInput();
        dataTransformationService.executeRunQueryStatement(TEST_CREDENTIALS, runQuery);
        verify(dbRepository).executeRunQueryStatement(argThat(this::verifyCredentials), 
            eq(runQuery.getRunSqlRequest().get(0).getAccessStatement()), 
            argThat(List::isEmpty));
    }
    
    @Test
    void testExecuteRunQueryStatementWhenKeysStatementIsPresent() throws Exception {
        RunQueryInputDto runQuery = createDummyRunQueryInput();
        runQuery.getRunSqlRequest().get(0).addEntry("DatasaetID", "932");
        dataTransformationService.executeRunQueryStatement(TEST_CREDENTIALS, runQuery);
        verify(dbRepository).executeRunQueryStatement(argThat(this::verifyCredentials),
            argThat( query -> {
                return query.endsWith(" where DatasaetID = ?");
            }),
            argThat(queryParams -> {
                return queryParams.size() == 1
                    && queryParams.get(0).equals("932");
            }));
    }

    @Test
    void testExecuteRunQueryStatementWhenWhereIsSqlStatmentAndKeysStatementIsPresent() throws Exception {
        RunQueryInputDto runQuery = createDummyRunQueryInput();
        String advancedQuery = REFERENCE_RUN_QUERY + " where Loengeneration = LoengruppeKode;  ";
        runQuery.getRunSqlRequest().get(0).setAccessStatement(advancedQuery);
        runQuery.getRunSqlRequest().get(0).addEntry("DatasaetID", "932");
        dataTransformationService.executeRunQueryStatement(TEST_CREDENTIALS, runQuery);
        verify(dbRepository).executeRunQueryStatement(argThat(this::verifyCredentials),
            argThat( query -> {
                return query.endsWith("where Loengeneration = LoengruppeKode and DatasaetID = ?");
            }),
            argThat(queryParams -> {
                return queryParams.size() == 1
                    && queryParams.get(0).equals("932");
            }));
    }

    private boolean verifyCredentials(Credentials credentials) {
        assertEquals(DEFAULT_USERNAME, credentials.getUsername());
        assertEquals("", credentials.getPassword());
        return true;
    }

}