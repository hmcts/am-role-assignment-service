package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaseService {


    private Map<String, Case> caseById = new HashMap<>();

    public Case getCaseById(String id)
    {
        return caseById.get(id);
    }
}
