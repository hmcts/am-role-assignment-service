package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum Status {
    CREATED(10),
    REQUEST_VALIDATED(11),
    REQUEST_NOT_VALIDATED(12),
    ROLE_VALIDATED(13),
    STAGE_1_APPROVED(14),
    ROLE_NOT_VALIDATED(15),
    APPROVED(16),
    REJECTED(17),
    LIVE(18),
    DELETE_APPROVED(21),
    DELETE_REJECTED(22),
    DELETED(23),


    EXPIRED(41);

    public final Integer sequence;

    Status(Integer sequence) {
        this.sequence = sequence;
    }
}
