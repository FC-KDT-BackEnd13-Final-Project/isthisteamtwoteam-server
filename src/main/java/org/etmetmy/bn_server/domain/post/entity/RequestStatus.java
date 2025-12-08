package org.etmetmy.bn_server.domain.post.entity;

public enum RequestStatus {

    STATUS_APPROVED(0, "승인"),
    STATUS_REJECTED(1, "거절"),
    STATUS_PENDING(2, "대기");

    private final int code;
    private final String description;

    RequestStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
