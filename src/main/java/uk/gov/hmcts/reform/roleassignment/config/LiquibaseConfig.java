package uk.gov.hmcts.reform.roleassignment.config;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Autowired
    DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseConfig.class);

    @PostConstruct
    public void method() throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            new JdbcConnection(dataSource.getConnection()));
        try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                                                 new ClassLoaderResourceAccessor(), database
        )) {
            liquibase.update(new Contexts());
        } catch (LiquibaseException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
