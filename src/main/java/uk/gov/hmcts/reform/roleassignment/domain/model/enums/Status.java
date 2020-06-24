package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum Status {
    CREATED(10),
    VALIDATED(11),
    APPROVED(12),
    REJECTED(13),
    LIVE(14),
    DELETE_APPROVED(21),
    DELETE_REJECTED(22),
    DELETED(23),

    EXPIRED(41);

    public final Integer sequence;

    Status(Integer sequence) {
        this.sequence = sequence;
    }
}
