package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "historypost")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "be_title", length = 255)
    private String beTitle; // before title

    @Column(name = "af_title", nullable = false, length = 500)
    private String afTitle; // after title

    @Column(name = "be_content", length = 255)
    private String beContent;

    @Column(name = "af_content", nullable = false, columnDefinition = "TEXT")
    private String afContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Column(name = "updated_ip", length = 45)
    private String updatedIp;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
