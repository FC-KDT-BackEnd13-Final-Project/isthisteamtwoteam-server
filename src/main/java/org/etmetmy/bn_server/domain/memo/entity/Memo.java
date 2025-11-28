package org.etmetmy.bn_server.domain.memo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "memo")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Memo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long memoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "content", length = 1000)
    private String content;
}