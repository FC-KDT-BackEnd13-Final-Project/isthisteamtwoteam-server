package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "historycomment", indexes = {
        @Index(name = "idx_original_comment_id", columnList = "original_comment_id"),
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistoryComment extends BaseEntity {

    // ============= 기본 정보 =============

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_comment_id")
    private Long historyCommentId;

    @Column(name = "original_comment_id", nullable = false)
    private Long originalCommentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    // ============= 변경 전 (Before) 데이터 =============

    @Column(name = "be_content", columnDefinition = "TEXT")
    private String beContent;

    // ============= 변경 후 (After) 데이터 =============

    @Column(name = "af_content", columnDefinition = "TEXT")
    private String afContent;

    // ============= 메타 정보 =============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(name = "change_ip", length = 45)
    private String changeIp;

    // ============= 추가 정보 (조회 최적화) =============

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "project_id")
    private Long projectId;
}
