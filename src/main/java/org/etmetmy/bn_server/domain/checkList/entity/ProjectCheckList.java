package org.etmetmy.bn_server.domain.checkList.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;

@Entity
@Table(name = "projectchecklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_check_list_id")
    private Long projectCheckListId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_check_list_id", nullable = false)
    private AdminCheckList adminCheckList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "checked")
    private Boolean checked;

    @PrePersist
    protected void onCreate() {
        if (checked == null) checked = false;
    }
}