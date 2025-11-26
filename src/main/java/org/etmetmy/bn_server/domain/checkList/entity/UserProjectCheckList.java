package org.etmetmy.bn_server.domain.checkList.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.User;

@Entity
@Table(name = "userprojectchecklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProjectCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_project_check_list_id")
    private Long userProjectCheckListId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_check_list_id", nullable = false)
    private ProjectCheckList projectCheckList;
}
