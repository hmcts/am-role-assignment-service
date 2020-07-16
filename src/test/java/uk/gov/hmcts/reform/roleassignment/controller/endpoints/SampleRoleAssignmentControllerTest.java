package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class SampleRoleAssignmentControllerTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ValidationModelService validationModelService;

    @InjectMocks
    private SampleRoleAssignmentController sut = new SampleRoleAssignmentController(
        requestRepository,
        validationModelService
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void insertEntity() {
        Request request = TestDataBuilder.buildRequest(Status.CREATED);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(request);
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(requestEntity);

        requestEntity.setStatus(Status.APPROVED.toString());
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(requestEntity);
        sut.insertEntity();
        verify(requestRepository, times(2)).save(any(RequestEntity.class));
    }
}
