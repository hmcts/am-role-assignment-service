package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Service
public class IdamRoleService {

    private SecurityUtils securityUtils;

    private IdamRepository idamRepository;

    public IdamRoleService(SecurityUtils securityUtils, IdamRepository idamRepository) {
        this.securityUtils = securityUtils;
        this.idamRepository = idamRepository;
    }


    public UserRoles getUserRoles(String userId) throws Exception {

        UserDetails userDetails = idamRepository.getUserByUserId(idamRepository.getManageUserToken(), userId);
        return UserRoles.builder()
            .uid(userDetails.getId())
            .roles(userDetails.getRoles())
            .build();
    }
}
