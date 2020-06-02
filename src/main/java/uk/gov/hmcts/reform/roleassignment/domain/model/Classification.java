package uk.gov.hmcts.reform.roleassignment.domain.model;

public enum Classification implements Comparable<Classification> {
    // TODO: this just uses natural ordering.  Needs to be specified.
    PUBLIC, PRIVATE, RESTRICTED;
}
