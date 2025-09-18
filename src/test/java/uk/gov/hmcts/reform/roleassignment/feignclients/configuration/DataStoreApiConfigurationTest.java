package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DataStoreApiConfigurationTest {

    @InjectMocks
    DataStoreApiConfiguration datastoreApiConfiguration = new DataStoreApiConfiguration();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void client() {
        assertNotNull(datastoreApiConfiguration.client());
    }


}
