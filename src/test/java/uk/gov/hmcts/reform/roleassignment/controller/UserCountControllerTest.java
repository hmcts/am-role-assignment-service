package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserCountControllerTest {

    @Mock
    private TelemetryClient telemetryClientMock;

    @Mock
    private RoleAssignmentRepository roleAssignmentRepositoryMock;

    @InjectMocks
    @Spy
    private final UserCountController sut = new UserCountController();

    RoleAssignmentRepository.JurisdictionRoleCategoryAndCount userCountCategoryIA;
    RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount userCountCategoryNameIA;
    RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount userCountCategoryNameCIVIL;
    RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount userCountCategoryNameNull;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userCountCategoryIA =
            new RoleAssignmentRepository.JurisdictionRoleCategoryAndCount() {

                @Override
                public String getJurisdiction() {
                    return "IA";
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.TEN;
                }
            };

        userCountCategoryNameIA =
            new RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount() {

                @Override
                public String getJurisdiction() {
                    return "IA";
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public String getRoleName() {
                    return "some role name";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.TWO;
                }
            };

        userCountCategoryNameCIVIL =
            new RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount() {

                @Override
                public String getJurisdiction() {
                    return "CIVIL";
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public String getRoleName() {
                    return "some role name";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.ONE;
                }
            };

        userCountCategoryNameNull =
            new RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount() {

                @Override
                public String getJurisdiction() {
                    return null;
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public String getRoleName() {
                    return "some role name";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.valueOf(3);
                }
            };

    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetUserCountResponse() throws SQLException, JsonProcessingException {

        doReturn(List.of(userCountCategoryIA)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdiction();
        doReturn(List.of(userCountCategoryNameIA)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdictionAndRoleName();

        ResponseEntity<Map<String, Object>> response = sut.getOrgUserCount();
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> responseOrgUserCountByJurisdiction =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount>)
                response.getBody().get("OrgUserCountByJurisdiction");
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>
            responseOrgUserCountByJurisdictionAndRoleName =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>)
                response.getBody().get("OrgUserCountByJurisdictionAndRoleName");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigInteger.TEN, responseOrgUserCountByJurisdiction.get(0).getCount());
        assertEquals("some role category", responseOrgUserCountByJurisdiction.get(0).getRoleCategory());
        assertEquals(BigInteger.TWO, responseOrgUserCountByJurisdictionAndRoleName.get(0).getCount());
        assertEquals("IA", responseOrgUserCountByJurisdictionAndRoleName.get(0).getJurisdiction());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetUserCountResponseJurisdictionCIVIL() throws SQLException, JsonProcessingException {

        doReturn(List.of(userCountCategoryIA)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdiction();
        doReturn(List.of(userCountCategoryNameCIVIL)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdictionAndRoleName();

        ResponseEntity<Map<String, Object>> response = sut.getOrgUserCount();
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> responseOrgUserCountByJurisdiction =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount>)
                response.getBody().get("OrgUserCountByJurisdiction");
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>
            responseOrgUserCountByJurisdictionAndRoleName =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>)
                response.getBody().get("OrgUserCountByJurisdictionAndRoleName");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigInteger.TEN, responseOrgUserCountByJurisdiction.get(0).getCount());
        assertEquals("some role category", responseOrgUserCountByJurisdiction.get(0).getRoleCategory());
        assertEquals(BigInteger.ONE, responseOrgUserCountByJurisdictionAndRoleName.get(0).getCount());
        assertEquals("CIVIL", responseOrgUserCountByJurisdictionAndRoleName.get(0).getJurisdiction());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetUserCountResponseJurisdictionNull() throws SQLException, JsonProcessingException {

        doReturn(List.of(userCountCategoryIA)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdiction();
        doReturn(List.of(userCountCategoryNameNull)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdictionAndRoleName();

        ResponseEntity<Map<String, Object>> response = sut.getOrgUserCount();
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> responseOrgUserCountByJurisdiction =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount>)
                response.getBody().get("OrgUserCountByJurisdiction");
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>
            responseOrgUserCountByJurisdictionAndRoleName =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>)
                response.getBody().get("OrgUserCountByJurisdictionAndRoleName");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigInteger.TEN, responseOrgUserCountByJurisdiction.get(0).getCount());
        assertEquals("some role category", responseOrgUserCountByJurisdiction.get(0).getRoleCategory());
        assertEquals(BigInteger.valueOf(3), responseOrgUserCountByJurisdictionAndRoleName.get(0).getCount());
        assertEquals(null, responseOrgUserCountByJurisdictionAndRoleName.get(0).getJurisdiction());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetUserCountResponseList() throws SQLException, JsonProcessingException {

        doReturn(List.of(userCountCategoryIA)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdiction();
        doReturn(List.of(userCountCategoryNameIA, userCountCategoryNameCIVIL, userCountCategoryNameNull)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdictionAndRoleName();

        ResponseEntity<Map<String, Object>> response = sut.getOrgUserCount();
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> responseOrgUserCountByJurisdiction =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount>)
                response.getBody().get("OrgUserCountByJurisdiction");
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>
            responseOrgUserCountByJurisdictionAndRoleName =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>)
                response.getBody().get("OrgUserCountByJurisdictionAndRoleName");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigInteger.TEN, responseOrgUserCountByJurisdiction.get(0).getCount());
        assertEquals("some role category", responseOrgUserCountByJurisdiction.get(0).getRoleCategory());
        assertEquals(BigInteger.TWO, responseOrgUserCountByJurisdictionAndRoleName.get(0).getCount());
        assertEquals("IA", responseOrgUserCountByJurisdictionAndRoleName.get(0).getJurisdiction());
        assertEquals(BigInteger.ONE, responseOrgUserCountByJurisdictionAndRoleName.get(1).getCount());
        assertEquals("CIVIL", responseOrgUserCountByJurisdictionAndRoleName.get(1).getJurisdiction());
        assertEquals(BigInteger.valueOf(3), responseOrgUserCountByJurisdictionAndRoleName.get(2).getCount());
        assertEquals(null, responseOrgUserCountByJurisdictionAndRoleName.get(2).getJurisdiction());

    }



}
