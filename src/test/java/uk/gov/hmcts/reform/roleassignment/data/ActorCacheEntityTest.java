package uk.gov.hmcts.reform.roleassignment.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class ActorCacheEntityTest {

    private ActorCacheEntity cacheEntity;

    @Test
    void getId() {
        cacheEntity = new ActorCacheEntity();
        cacheEntity.setActorId("10292");
        assertEquals("10292", cacheEntity.getId());
    }

    @Test
    void isNew() {
        cacheEntity = new ActorCacheEntity();
        assertTrue(cacheEntity.isNew());
    }
}
