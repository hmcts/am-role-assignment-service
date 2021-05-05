package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class DatastoreApiConfigurationTest {

    @InjectMocks
    DatastoreApiConfiguration datastoreApiConfiguration = new DatastoreApiConfiguration();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void client() {
        assertNotNull(datastoreApiConfiguration.client());
    }


}
