package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class JsonBConverterTest {

    @InjectMocks
    JsonBConverter sut = new JsonBConverter();

    @Test
    void convertToDatabaseColumn() throws IOException {
        String result = sut.convertToDatabaseColumn(TestDataBuilder.buildNotesFromFile());
        assertEquals(TestDataBuilder.buildNotesFromFile().toString(), result);
    }

    @Test
    void convertToEntityAttribute() throws IOException {
        JsonNode result = sut.convertToEntityAttribute("[{\"userId\":\"S-042\",\"time\":\"2020-01-01T00:00"
                                                           + "\",\"comment\":\"Need Access to case number"
                                                           + " 1234567890123456 for a year\"},{\"userId\":"
                                                           + "\"HMCTS\",\"time\":\"2020-01-02T00:00\",\"comment"
                                                           + "\":\"Access granted for 3 months\"}]");

        assertEquals(TestDataBuilder.buildNotesFromFile(), result);
    }
}
