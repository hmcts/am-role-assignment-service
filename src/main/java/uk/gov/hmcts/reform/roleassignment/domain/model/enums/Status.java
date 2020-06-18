package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum Status {
    CREATED(10),
    APPROVED(11),
    REJECTED(12),
    LIVE(13),
    DELETE_APPROVED(21),
    DELETE_REJECTED(22),
    DELETED(23),
    EXPIRED(41);

    public final Integer sequence;

    Status(Integer sequence) {
        this.sequence = sequence;
    }
}
