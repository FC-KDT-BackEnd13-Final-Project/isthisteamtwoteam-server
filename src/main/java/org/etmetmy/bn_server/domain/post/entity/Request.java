package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY) // 승인자 관계 추가
    @JoinColumn(name = "approver_id")
    private User approver;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();

    public void updateStatus(RequestStatus status, Long loginUserId, User approver, String rejectReason) {
        this.replitUserId = loginUserId;
        this.approveStatus = status;
        this.rejectReason = rejectReason;
        this.replyTime = LocalDateTime.now();
        this.approver = approver;
    }

}
