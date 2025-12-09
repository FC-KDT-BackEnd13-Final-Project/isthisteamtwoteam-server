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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "request_user_id")
    private Long requestUserId;

    @Column(name = "replit_user_id")
    private Long replitUserId;

    @Column(name = "approve_status", length = 255)
    private RequestStatus approveStatus;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    public void updateStatus(Long replyUserId, RequestStatus status, String rejectReason) {
        this.replitUserId = replyUserId;
        this.approveStatus = status;
        this.rejectReason = rejectReason;
        this.replyTime = LocalDateTime.now();
    }
}
