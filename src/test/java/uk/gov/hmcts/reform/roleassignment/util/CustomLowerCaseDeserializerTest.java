package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomLowerCaseDeserializerTest {
    private ObjectMapper mapper;
    private CustomLowerCaseDeserializer deserializer;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        deserializer = new CustomLowerCaseDeserializer();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    @Test
    void convertUpperCaseToLowerCase() throws IOException {

        String json = "[\"TRIBUNAL-Caseworker\"]";
        InputStream stream = new ByteArrayInputStream(json.getBytes());

        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext context = mapper.getDeserializationContext();

        List<String> result = deserializer.deserialize(parser, context);
        assertNotNull(result);
        assertEquals("tribunal-caseworker", result.get(0));

    }
}
