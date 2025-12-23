package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replit_user_id")
    private User responder;

    @Column(name = "approve_status", length = 255)
    private RequestStatus approveStatus;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();

    public void updateStatus(RequestStatus status, User responder, String rejectReason) {
        this.approveStatus = status;
        this.rejectReason = rejectReason;
        this.replyTime = LocalDateTime.now();
        this.responder = responder;
    }

}
