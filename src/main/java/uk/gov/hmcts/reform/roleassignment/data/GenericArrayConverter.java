package uk.gov.hmcts.reform.roleassignment.data;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Types;

@Converter(autoApply = false)
public class GenericArrayConverter implements AttributeConverter<String[], String> {

    protected static final int[] SQL_TYPES = {Types.ARRAY};

    //@Override
    public int[] sqlTypes() {
        return new int[]{Types.ARRAY};
    }


    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (attribute != null) {
            String output = "{";

            for (String element : attribute) {
                output = output + attribute + ",";
            }

            if (attribute.length > 0) {
                output = output.substring(0, output.length() - 1);
            }

            return output + "}";
        } else {
            return null;
        }
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            String dbDataNoBrackets = dbData.replaceAll("[{}]", "");
            return dbDataNoBrackets.split(",");
        } else {
            return null;
        }
    }
}
