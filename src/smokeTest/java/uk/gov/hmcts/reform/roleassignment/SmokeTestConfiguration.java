package uk.gov.hmcts.reform.roleassignment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@TestConfiguration
public class SmokeTestConfiguration {

    Connection connection;

    @Bean
    public PostgresTestContainer embeddedPostgres() {
        return PostgresTestContainer
            .builder()
            .start();
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        final PostgresTestContainer pg = embeddedPostgres();

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
        embeddedPostgres().close();
    }
}
