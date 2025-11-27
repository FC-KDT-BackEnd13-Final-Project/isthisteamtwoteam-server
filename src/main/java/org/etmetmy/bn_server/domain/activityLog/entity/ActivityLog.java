package org.etmetmy.bn_server.domain.activityLog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "activitylog")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "activity_type", nullable = false, length = 255)
    private String activityType;

    @Column(name = "activity_description", length = 500)
    private String activityDescription;

    @Column(name = "target_type", length = 255)
    private String targetType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}