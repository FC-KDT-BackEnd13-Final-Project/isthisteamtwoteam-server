package org.etmetmy.bn_server.domain.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entitytype")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_type_id")
    private Long entityTypeId;

    @Column(name = "entity_type", length = 255)
    private String entityType;
}
