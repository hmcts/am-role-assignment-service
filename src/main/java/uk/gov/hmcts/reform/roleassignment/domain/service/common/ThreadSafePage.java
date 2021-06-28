package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;

public class ThreadSafePage {

    public static ThreadLocal<Page<RoleAssignmentEntity>> pageHolder  =  new ThreadLocal<>();
}
