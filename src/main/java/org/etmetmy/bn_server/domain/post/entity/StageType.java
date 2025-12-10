package org.etmetmy.bn_server.domain.post.entity;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StageType {
    NOT_STARTED(1,"진행 전"),
    ON_HOLD(2,"진행 중단"),
    REQUIREMENTS(3, "요구사항 정의"),
    SCREEN_DESIGN(4, "화면 설계"),
    DESIGN_PUBLISHING(5, "디자인, 퍼블리싱"),
    DEVELOPMENT(6, "개발"),
    QA(7, "검수"),
    MAINTENANCE(8, "유지보수"),
    FINISHED(9, "완료");

    private final int code;
    private final String description;

    StageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static StageType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findFirst()
                .orElse(null);
    }

}
