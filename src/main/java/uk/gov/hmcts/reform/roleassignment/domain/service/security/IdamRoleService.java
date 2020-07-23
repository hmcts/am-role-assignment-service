package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;

@Service
public class IdamRoleService {


    private IdamRepository idamRepository;

    public IdamRoleService(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    public UserRoles getUserRoles(String userId) {

        UserDetails userDetails = idamRepository.getUserByUserId(idamRepository.getManageUserToken(), userId);
        return UserRoles.builder()
            .uid(userDetails.getId())
            .roles(userDetails.getRoles())
            .build();
    }
}
