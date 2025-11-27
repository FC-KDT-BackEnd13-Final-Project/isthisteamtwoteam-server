package org.etmetmy.bn_server.domain.checkList.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Override
    public CheckListResponse update(Long checkListId, CheckListUpdateRequest request) {

        CheckList checkList = checkListRepository.findById(checkListId).orElse(null);
        CheckList updateEntity =CheckListUpdateRequest.Converter.updateEntity(request, checkList);
        CheckList saved = checkListRepository.save(updateEntity);

        return CheckListResponse.Converter.from(saved);
    }

    @Override
    public void delete(Long checkListId) {
        checkListRepository.deleteById(checkListId);
    }
}