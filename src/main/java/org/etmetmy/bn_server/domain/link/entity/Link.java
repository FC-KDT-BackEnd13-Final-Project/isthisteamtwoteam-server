package org.etmetmy.bn_server.domain.link.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "link")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Link extends BaseEntity { //다른 패키지에서 접근

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "link_url", nullable = false, length = 500)
    private String linkUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkList_id")
    private CheckList checkList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;
}
