package org.etmetmy.bn_server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    @Column(name = "created_ip", length = 45, updatable = false)
//    private String createdIp;
//
//    @Column(name = "updated_ip", length = 45)
//    private String updatedIp;
//
//    public void setCreatedIp(String createdIp) {
//        if (this.createdIp == null) {
//            this.createdIp = createdIp;
//        }
//    }
//
//    public void setUpdatedIp(String updatedIp) {
//        this.updatedIp = updatedIp;
//    }
}
