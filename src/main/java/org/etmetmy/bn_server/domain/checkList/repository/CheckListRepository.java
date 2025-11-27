package org.etmetmy.bn_server.domain.checkList.repository;

import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {
}
