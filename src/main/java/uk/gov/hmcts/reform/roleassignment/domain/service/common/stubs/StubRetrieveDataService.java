package uk.gov.hmcts.reform.roleassignment.domain.service.common.stubs;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;

import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

public class StubRetrieveDataService {

	private static final Map<String, Case> CASE_BY_ID = loadCases();

	/**
	 * Load case data from the "cases.json" resource.
	 */
	private static Map<String, Case> loadCases() {
		try {
			Set<Case> cases;
			try (InputStream input = StubRetrieveDataService.class.getResourceAsStream("cases.json")) {
				JavaType type = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(Set.class, Case.class);
				cases = JacksonUtils.MAPPER.readValue(input, type);
			}
			Map<String, Case> caseById = new HashMap<>();
			cases.forEach(c -> caseById.put(c.getId(), c));
			return caseById;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load stub case data.", t);
		}
	}

    public Case getCaseById(String caseId) {
    	return CASE_BY_ID.get(caseId);
    }

    public static void main(String[] args) throws Exception {
    	System.out.println(new StubRetrieveDataService().getCaseById("00000001"));
    }
}
