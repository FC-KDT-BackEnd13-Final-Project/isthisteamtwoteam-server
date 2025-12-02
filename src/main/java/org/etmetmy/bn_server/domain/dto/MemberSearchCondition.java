package org.etmetmy.bn_server.domain.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    private String name;        // 회원 이름 검색
    private String companyName; // 회사 이름 검색
    private String email;       // 이메일 검색
    // private Boolean isDeveloper; // 개발사 여부
}
