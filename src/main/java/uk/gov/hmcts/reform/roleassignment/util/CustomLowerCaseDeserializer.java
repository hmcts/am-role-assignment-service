package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.List;

public class CustomLowerCaseDeserializer extends JsonDeserializer<List> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<String> roles = p.readValueAs(new TypeReference<List<String>>(){});
        roles.replaceAll(String::toLowerCase);
        return roles;
    }


}
