package org.etmetmy.bn_server.domain.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "historyfile", indexes = {
        @Index(name = "idx_original_file_id", columnList = "original_file_id"),
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_id", columnList = "comment_id"),
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistoryFile extends BaseEntity {

    // ============= 기본 정보 =============

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_file_id")
    private Long historyFileId;

    @Column(name = "original_file_id", nullable = false)
    private Long originalFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;  // CREATE, DELETE

    // ============= 파일 정보 =============

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private String fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

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
