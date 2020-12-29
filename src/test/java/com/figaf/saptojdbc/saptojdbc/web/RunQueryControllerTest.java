package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.service.DataTransformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.encodeDummyCredentials;
import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.readResourceFile;
import static com.figaf.saptojdbc.saptojdbc.web.TestUtils.verifyPasswordCorrectness;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class RunQueryControllerTest {
    
    @Mock
    private RunQueryParser queryParser;
    
    @Mock
    private DataTransformationService dataTransformationService;
    
    private MockMvc mockMvc;

    @BeforeEach
    public void setup(){
        RunQueryController runQueryController = new RunQueryController(queryParser, dataTransformationService);
        ErrorHandlingAdvice errorHandling = new ErrorHandlingAdvice();
        this.mockMvc = standaloneSetup(runQueryController)
            .setControllerAdvice(errorHandling)
            .build();
    }

    @Test
    void query() throws Exception {
        String sampleData = readResourceFile("sql_query_input_sample.xml");
        ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        doReturn(new RunQueryInputDto()).when(queryParser).parse(eq(sampleData));

        doReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .when(dataTransformationService)
            .executeRunQueryStatement(credentialsCaptor.capture(), any(RunQueryInputDto.class));
       
        mockMvc.perform(
            post("/run-query")
                .contentType(APPLICATION_XML_VALUE)
                .header("Authorization", encodeDummyCredentials())
                .content(sampleData)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());
        
        verifyPasswordCorrectness(credentialsCaptor);
    }
}