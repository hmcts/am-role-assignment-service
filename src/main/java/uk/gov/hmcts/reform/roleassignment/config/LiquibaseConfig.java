package uk.gov.hmcts.reform.roleassignment.config;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class LiquibaseConfig implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseConfig.class);

    @Autowired
    DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            new JdbcConnection(dataSource.getConnection()));
        try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                                                 new ClassLoaderResourceAccessor(), database
        )) {
            liquibase.update(new Contexts());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}

