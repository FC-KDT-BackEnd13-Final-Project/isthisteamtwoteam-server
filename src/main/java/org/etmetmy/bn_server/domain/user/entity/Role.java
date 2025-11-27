package org.etmetmy.bn_server.domain.user.entity;

public enum Role {
    ADMIN("관리자", "ROLE_ADMIN"),
    DEVELOPER("개발사", "ROLE_DEVELOPER"),
    CUSTOMER("고객사", "ROLE_CUSTOMER");

    private String description;
    private String authority;

    Role(String description , String authority){
        this.description = description;
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
    public String getDescription(){
        return description;
    }


}
