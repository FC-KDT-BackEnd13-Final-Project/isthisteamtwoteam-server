package org.etmetmy.bn_server.domain.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_check_list_id")
    private ProjectCheckList projectCheckList;

    @Column(name = "s3_file_title", nullable = false, length = 255)
    private String s3FileTitle; // UUID_파일명

    @Column(name = "original_file_title", nullable = false, length = 255)
    private String originalFileTitle; // 사용자가 업로드한 이름

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private String fileSize;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploader;

    @Column(name = "is_temp")
    @lombok.Builder.Default
    private Boolean isTemp = true;

    @Column(name = "is_deleted", nullable = false)
    @lombok.Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;


    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        isDeleted = false;
        deletedAt = null;
        this.deletedBy = null;
    }

    // 공통 내부 처리
    private void attach(Post post, Comment comment, ProjectCheckList projectCheckList, User user) {
        this.post = post;
        this.comment = comment;
        this.projectCheckList = projectCheckList;
        this.isTemp = false;
        this.uploadedBy = user.getId();
    }

    // 게시글에 연결
    public void attachToPost(Post post, User user) {
        attach(post, null,null, user);
    }

    // 댓글에 연결
    public void attachToComment(Comment comment, User user) {
        attach(null, comment, null, user);
    }

    public void attachToProjectCheckList(ProjectCheckList projectCheckList, User user) {
        attach(null, null, projectCheckList, user);
    }
}