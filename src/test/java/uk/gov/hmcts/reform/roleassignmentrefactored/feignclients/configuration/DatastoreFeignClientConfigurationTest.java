package uk.gov.hmcts.reform.roleassignmentrefactored.feignclients.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignmentrefactored.feignclients.configuration.DatastoreFeignClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class DatastoreFeignClientConfigurationTest {

    @InjectMocks
    DatastoreFeignClientConfiguration datastoreFeignClientConfiguration = new DatastoreFeignClientConfiguration();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void client() {
        assertNotNull(datastoreFeignClientConfiguration.client());
    }


}
