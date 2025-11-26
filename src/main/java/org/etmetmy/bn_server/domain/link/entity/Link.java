package org.etmetmy.bn_server.domain.link.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.EntityType;

import java.time.LocalDateTime;

@Entity
@Table(name = "link")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "link_url", nullable = false, length = 500)
    private String linkUrl;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id", nullable = false)
    private EntityType entityType;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (isDeleted == null) isDeleted = false;
    }
}
