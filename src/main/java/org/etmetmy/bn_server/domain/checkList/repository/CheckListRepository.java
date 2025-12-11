package org.etmetmy.bn_server.domain.checkList.repository;

import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {

    // 특정 조건으로 CheckList 조회
    @Query("SELECT c FROM CheckList c WHERE c.content LIKE %:keyword%")
    Page<CheckList> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
