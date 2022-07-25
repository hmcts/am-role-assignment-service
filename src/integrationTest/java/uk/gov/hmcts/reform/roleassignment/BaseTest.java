package uk.gov.hmcts.reform.roleassignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.roleassignment.health.CcdDataStoreHealthIndicator;
import uk.gov.hmcts.reform.roleassignment.health.IdamServiceHealthIndicator;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    IdamServiceHealthIndicator idamServiceHealthIndicator;

    @MockBean
    CcdDataStoreHealthIndicator ccdDataStoreHealthIndicator;

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        StandardCharsets.UTF_8
    );

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
