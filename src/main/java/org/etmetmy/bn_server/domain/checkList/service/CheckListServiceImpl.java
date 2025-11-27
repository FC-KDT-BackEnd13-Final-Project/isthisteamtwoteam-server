package org.etmetmy.bn_server.domain.checkList.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckListServiceImpl implements CheckListService {
    private final CheckListRepository checkListRepository;

    @Override
    public CheckListResponse save() {
        CheckList entity = CheckListCreateRequest.Converter.toEntity();
        CheckList saved = checkListRepository.save(entity);
        return CheckListResponse.Converter.from(saved);
    }
}