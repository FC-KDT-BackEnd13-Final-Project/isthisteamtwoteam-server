package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Request extends BaseEntity {

    @Id
    @Column(name = "key", length = 255)
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "request_user_id")
    private Long requestUserId;

    @Column(name = "replit_user_id")
    private Long replitUserId;

    @Column(name = "approve_status", length = 255)
    private String approveStatus;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;
}
