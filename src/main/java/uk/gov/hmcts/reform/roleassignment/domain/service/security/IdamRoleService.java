package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class IdamRoleService {


    private IdamRepository idamRepository;

    public IdamRoleService(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    @SuppressWarnings("unchecked")
    public UserRoles getUserRoles(String userId) {
        LinkedHashMap<String,Object> userDetail;
        String id=null;
        List<String> roles = new ArrayList<>();
        ResponseEntity<Object> userDetails = idamRepository.searchUserByUserId(
            idamRepository.getManageUserToken(), userId);
        if (userDetails != null) {
            userDetail = (LinkedHashMap <String,Object>)((ArrayList) userDetails.getBody()).get(0);
            id = userDetail.get("id").toString();
            roles = (List<String>)userDetail.get("roles");
        }

        return UserRoles.builder()
            .uid(id)
            .roles(roles)
            .build();
    }
}
