package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;

import java.util.Comparator;

@Component
public class CreatedTimeComparator implements Comparator<RoleAssignment> {

    @Override
    public int compare(RoleAssignment roleAssignment, RoleAssignment t1) {
        return roleAssignment.getCreated().compareTo(t1.getCreated());
    }
}
