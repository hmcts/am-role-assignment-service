
package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.JURISDICTION;

@Service
@Slf4j
public class RetrieveDataService {
    //1. getting required case details(ccd data store)
    //2. getting required role details(some static reference data??)
    //3. getting required ticket details(Authorization table in JRD)
    //4. getting some location reference data


    private final DataStoreFeignClient dataStoreFeignClient;
    private CacheManager cacheManager;
    @Value("${spring.cache.type}")
    protected String cacheType;

    public RetrieveDataService(DataStoreFeignClient dataStoreFeignClient,
                               CacheManager cacheManager) {
        this.dataStoreFeignClient = dataStoreFeignClient;
        this.cacheManager = cacheManager;
    }

    public Case getCaseById(String caseId, Assignment assignment) {
        Case retrievedCase = getCaseById(caseId);
        assignment.getAttributes().put(JURISDICTION, JacksonUtils.convertValueJsonNode(
            retrievedCase.getJurisdiction()));
        assignment.getAttributes().put(CASE_TYPE_ID, JacksonUtils.convertValueJsonNode(retrievedCase.getCaseTypeId()));
        return retrievedCase;
    }

    @Cacheable(value = "case")
    public Case getCaseById(String caseId) {
        Case retrievedCase = dataStoreFeignClient.getCaseDataV2(caseId);
        if (cacheType != null && !cacheType.equals("none")) {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("token");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                .getNativeCache();
            log.info("generating Bearer Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        return retrievedCase;
    }
}
