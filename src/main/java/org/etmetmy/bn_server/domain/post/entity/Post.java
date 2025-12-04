package org.etmetmy.bn_server.domain.post.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "parent_post_id")
    private Long parentPostId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "created_ip")
    private String createdIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    // 프로젝트 내에서의 게시글 번호 추가
    @Column(name = "post_number", nullable = false)
    private Long postNumber;

    //파일 목록
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isCompleted == null) isCompleted = false;
    }

    public void updateStage(Long stageId) {
        this.stage = stage;
    }

    /**
     * 게시글 생성 정적 팩토리 메서드
     */
    public static Post createPost(Project project, User user, String title, String content, Stage stage, Long postNumber) {
        return Post.builder()
                .project(project)
                .user(user)
                .authorId(user.getId())
                .title(title)
                .content(content)
                .stage(stage)
                .postNumber(postNumber)
                .isCompleted(false)
                .build();
    }
}