package org.etmetmy.bn_server.domain.activityLog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.global.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "activity_log", indexes = {
        @Index(name = "idx_project_created", columnList = "project_id, created_at"),
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_target", columnList = "target_type, target_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    // 어떤 프로젝트에서 발생한 일인지
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;
    */

    //테스트용 컬럼 , 임시로 숫자만 사용
    @Column(name = "project_id", nullable = false, updatable = false)
    private Long projectId;

    // 1. 행위자 (User 엔티티 직접 참조 X -> 탈퇴 회원도 로그 유지)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    // 2. 행위의 종류 (create, update, delete, comment_add)
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 255, updatable = false)
    private ActivityAction action;

    //3. 대상 식별자 (Post, Member)
    @Column(name = "target_type", nullable = false, length = 255, updatable = false)
    private String targetType;

    //ex. 해당 post의 ID //ex. 25번 게시글
    @Column(name = "target_id", nullable = false, updatable = false)
    private Long targetId;

    //변경 상세 내용 JSON 형태로 저장
    @Lob
    @Column(name = "detail",columnDefinition = "TEXT", updatable = false)
    private String detail;

    @Column(name = "ip_address", length = 45, nullable = false, updatable = false)
    private String ipAddress;
}