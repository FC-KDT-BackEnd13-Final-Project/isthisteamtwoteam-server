package org.etmetmy.bn_server.domain.checkList.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adminchecklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_check_list_id")
    private Long adminCheckListId;

    @Column(name = "content", length = 255)
    private String content;
}
