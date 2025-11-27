package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "historycomment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_comment_id")
    private Long historyCommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id2")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "be_content", nullable = false, columnDefinition = "TEXT")
    private String beContent;

    @Column(name = "af_content", columnDefinition = "TEXT")
    private String afContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}