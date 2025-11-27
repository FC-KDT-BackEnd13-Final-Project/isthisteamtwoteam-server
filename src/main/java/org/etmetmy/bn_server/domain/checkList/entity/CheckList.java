package org.etmetmy.bn_server.domain.checkList.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "checklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_id")
    private Long checkListId;

    @Column(name = "content", length = 255)
    private String content;

}
