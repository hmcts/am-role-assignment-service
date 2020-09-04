package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
public class IdamRoleService {

    private IdamRepository idamRepository;

    public IdamRoleService(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    @SuppressWarnings("unchecked")
    public UserRoles getUserRoles(String userId) {
        LinkedHashMap<String, Object> userDetail;
        String id = null;
        List<String> roles = Collections.emptyList();
        ResponseEntity<List<Object>> userDetails = idamRepository.searchUserByUserId(
            idamRepository.getManageUserToken(), userId);
        List<Object> userDetailsList = userDetails != null ? userDetails.getBody() : null;
        if (userDetailsList != null && !userDetailsList.isEmpty()) {
            userDetail = (LinkedHashMap<String, Object>) userDetailsList.get(0);
            id = userDetail.get("id").toString();
            roles = (List<String>) userDetail.get("roles");
        }


        return UserRoles.builder()
            .uid(id)
            .roles(roles)
            .build();
    }
}
