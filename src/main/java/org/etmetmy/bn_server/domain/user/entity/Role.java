package org.etmetmy.bn_server.domain.entity;

public enum Role {
    ADMIN("관리자", "ROLE_ADMIN",3),
    DEVELOPER("개발사", "ROLE_DEVELOPER",2),
    CUSTOMER("고객사", "ROLE_USERS",1);

    private String description;
    private String authority;
    private Integer level;

    Role(String description , String authority, Integer level){
        this.description = description;
        this.authority = authority;
        this.level = level;
    }

    public String getAuthority() {
        return authority;
    }
    public String getDescription(){
        return description;
    }

    public Integer getLevel(){
        return level;
    }


}
