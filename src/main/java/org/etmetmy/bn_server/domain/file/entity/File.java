package org.etmetmy.bn_server.domain.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
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
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_check_list_id")
    private ProjectCheckList projectCheckList;

    @Column(name = "file_title", nullable = false, length = 255)
    private String fileTitle;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "is_temp")
    private Boolean isTemp = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;


    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    // 임시 파일 상태 변경
    public void attachToPost(Post post,Long uploadedBy) {
        this.post = post;
        this.isTemp = false;
        this.uploadedBy = uploadedBy;
    }
}