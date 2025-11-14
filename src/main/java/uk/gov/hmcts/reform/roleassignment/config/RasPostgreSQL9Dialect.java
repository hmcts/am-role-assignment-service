package uk.gov.hmcts.reform.roleassignment.config;


import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Component;

@Component
public class RasPostgreSQL9Dialect extends PostgreSQLDialect {

    public RasPostgreSQL9Dialect() {
        super();
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();
        BasicType<Boolean> resultType = functionContributions
            .getTypeConfiguration()
            .getBasicTypeRegistry()
            .resolve(StandardBasicTypes.BOOLEAN);

        functionRegistry.registerPattern(
            "contains_jsonb",
            "?1 @> ?2::jsonb",
            resultType
        );
    }
}
