package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class IdamRoleService {


    private IdamRepository idamRepository;

    public IdamRoleService(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    @SuppressWarnings("unchecked")
    public Set<UserRoles> getUserRoles(String userIds) {
        Set<UserRoles> userRolesEntities = new HashSet<>();
        ResponseEntity<Object> userSearchResponse = idamRepository.searchUserByUserId(
            idamRepository.getManageUserToken(), userIds);
        if (userSearchResponse != null) {
            ArrayList<Object> userDetailsResponse = ((ArrayList) userSearchResponse.getBody());
            if (!userDetailsResponse.isEmpty()) {
                for (int i = 0; i < userDetailsResponse.size(); i++) {
                    LinkedHashMap<String, Object> userDetail =
                        (LinkedHashMap<String, Object>) userDetailsResponse.get(i);
                    String id = userDetail.get("id").toString();
                    List<String> roles = (List<String>) userDetail.get("roles");
                    userRolesEntities.add(UserRoles.builder()
                                              .uid(id)
                                              .roles(roles)
                                              .build());
                }
            }
        }
        return userRolesEntities;
    }
}
