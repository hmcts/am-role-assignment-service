package uk.gov.hmcts.reform.roleassignment.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentEntityTest {

    @Test
    void isNew() {
        RoleAssignmentEntity assignmentEntity = new RoleAssignmentEntity();
        assignmentEntity.setNewFlag(true);
        assertTrue(assignmentEntity.isNew());

        assignmentEntity.setNewFlag(false);
        assertFalse(assignmentEntity.isNew());
    }

}
