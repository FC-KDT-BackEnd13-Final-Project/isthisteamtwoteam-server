package org.etmetmy.bn_server.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projectchecklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProjectCheckList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_check_list_id")
    private Long projectCheckListId;

    @Column(name = "check_list_id")
    private Long checkListId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id")
    private User answererId;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "reason")
    private String reason;

    //파일 목록
    @OneToMany(mappedBy = "projectCheckList", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<File> files = new ArrayList<>();

    //링크 목록
    @OneToMany(mappedBy = "projectCheckList", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<Link> links = new ArrayList<>();
}