package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

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

    @Column(name = "request_time")
    private LocalDateTime requestTime;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @PrePersist
    protected void onCreate() {
        requestTime = LocalDateTime.now();
    }
}
