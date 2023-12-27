package uk.gov.hmcts.reform.roleassignment.controller;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.roleassignment.BaseTestOriginal;
import uk.gov.hmcts.reform.roleassignment.health.CcdDataStoreHealthIndicator;
import uk.gov.hmcts.reform.roleassignment.health.IdamServiceHealthIndicator;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


@ActiveProfiles("itest")
public abstract class BaseTestIntegration extends BaseTest {

//    protected static final ObjectMapper mapper = new ObjectMapper();
//
//    @MockBean
//    IdamServiceHealthIndicator idamServiceHealthIndicator;
//
//    @MockBean
//    CcdDataStoreHealthIndicator ccdDataStoreHealthIndicator;
//
//    @BeforeClass
//    public static void init() {
//        mapper.registerModule(new JavaTimeModule());
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//    }

    @TestConfiguration
    static class Configuration {
        Connection connection;

        @Bean
        public EmbeddedPostgres embeddedPostgres() throws IOException {
            return EmbeddedPostgres
                    .builder()
                    .start();
        }

        @Bean
        public DataSource dataSource(@Autowired EmbeddedPostgres pg) throws Exception {

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            props.setProperty("user", "postgres");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres"), props);
            return new SingleConnectionDataSource(connection, true);
        }


        @PreDestroy
        public void contextDestroyed() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
