package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "historylink", indexes = {
        @Index(name = "idx_original_link_id", columnList = "original_link_id"),
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_id", columnList = "comment_id"),
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistoryLink extends BaseEntity {

    // ============= 기본 정보 =============

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_link_id")
    private Long historyLinkId;

    @Column(name = "original_link_id", nullable = false)
    private Long originalLinkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;  // CREATE, DELETE

    // ============= 링크 정보 =============

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    // ============= 메타 정보 =============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(name = "change_ip", length = 45)
    private String changeIp;

    // ============= 추가 정보 (조회 최적화) =============

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "project_check_list_id")
    private Long projectCheckListId;

    @Column(name = "project_id")
    private Long projectId;
}
