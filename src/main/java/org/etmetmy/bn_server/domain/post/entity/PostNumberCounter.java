package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로젝트별 게시글 번호를 관리하는 Counter 엔티티
 * 낙관적 락(Optimistic Locking)을 통해 동시성 제어
 */
@Entity
@Table(name = "post_number_counter")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostNumberCounter {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "current_number", nullable = false)
    private Long currentNumber;

    /**
     * 낙관적 락: 동시에 업데이트되는 것을 방지
     * Version이 변경되면 OptimisticLockException 발생
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 다음 번호 생성 (1씩 증가)
     */
    public Long getNextNumber() {
        this.currentNumber++;
        return this.currentNumber;
    }
}
