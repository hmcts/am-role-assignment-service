package uk.gov.hmcts.reform.roleassignment.config;


//import org.hibernate.boot.model.FunctionContributions;
//import org.hibernate.dialect.PostgreSQL95Dialect;
//import org.hibernate.boot.model.FunctionContributions;
//import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;
//import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
//import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Component;

@Component
//public class RasPostgreSQL9Dialect extends PostgreSQL95Dialect {
public class RasPostgreSQL9Dialect extends PostgreSQLDialect {

    @Override
    public void initializeFunctionRegistry(QueryEngine queryEngine) {
        super.initializeFunctionRegistry(queryEngine);
        BasicTypeRegistry basicTypeRegistry = queryEngine.getTypeConfiguration().getBasicTypeRegistry();
        SqmFunctionRegistry functionRegistry = queryEngine.getSqmFunctionRegistry();
        functionRegistry.registerPattern(
            "contains_jsonb",
            "?1 @> ?2::jsonb",
            basicTypeRegistry.resolve(StandardBasicTypes.BOOLEAN)
        );
    }

}
