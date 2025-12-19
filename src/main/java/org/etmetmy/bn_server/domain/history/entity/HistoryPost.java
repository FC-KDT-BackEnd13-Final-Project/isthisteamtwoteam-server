package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "historypost", indexes = {
        @Index(name = "idx_original_post_id", columnList = "original_post_id"),
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistoryPost extends BaseEntity {

    // ============= 기본 정보 =============

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_post_id")
    private Long historyPostId;

    @Column(name = "original_post_id", nullable = false)
    private Long originalPostId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    // ============= 변경 전 (Before) 데이터 =============

    @Column(name = "be_title", length = 500)
    private String beTitle;

    @Column(name = "be_content", columnDefinition = "TEXT")
    private String beContent;

    @Column(name = "be_stage_id")
    private Long beStageId;

    @Column(name = "be_stage_name", length = 255)
    private String beStageName;

    @Column(name = "be_is_completed")
    private Boolean beIsCompleted;

    // ============= 변경 후 (After) 데이터 =============

    @Column(name = "af_title", length = 500)
    private String afTitle;

    @Column(name = "af_content", columnDefinition = "TEXT")
    private String afContent;

    @Column(name = "af_stage_id")
    private Long afStageId;

    @Column(name = "af_stage_name", length = 255)
    private String afStageName;

    @Column(name = "af_is_completed")
    private Boolean afIsCompleted;

    // ============= 메타 정보 =============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(name = "change_ip", length = 45)
    private String changeIp;

    // ============= 추가 정보 (조회 최적화) =============

    @Column(name = "project_id")
    private Long projectId;
}