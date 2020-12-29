package com.figaf.saptojdbc.saptojdbc;

import com.figaf.saptojdbc.saptojdbc.repo.DbRepository;
import com.figaf.saptojdbc.saptojdbc.service.DataTransformationService;
import com.figaf.saptojdbc.saptojdbc.web.RunQueryParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactory {
    
    @Bean
    public DbRepository dbRepository(@Value("${db.url}") String repoUrl) {
        return new DbRepository(repoUrl);
    }
    
    @Bean
    public DataTransformationService personService(DbRepository dbRepository) {
        return new DataTransformationService(dbRepository);
    }
    
    @Bean
    public RunQueryParser runQueryParser(){
        return new RunQueryParser();
    }
}
