package org.etmetmy.bn_server.domain.checkList.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.springframework.data.domain.Page;
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

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(()-> new CustomException(StatusCode.CHECKLIST_NOT_FOUND));
        CheckList updateEntity =CheckListUpdateRequest.Converter.updateEntity(request, checkList);
        CheckList saved = checkListRepository.save(updateEntity);

        return CheckListResponse.Converter.from(saved);
    }

    @Override
    public void delete(Long checkListId) {
        checkListRepository.deleteById(checkListId);
    }

    // 페이지네이션 체크리스트 전체 조회
    @Override
    public Page<CheckListResponse> getCheckLists(PageRequest pageRequest) {
        // 1. Repository에서 Page<CheckList> 조회
        Page<CheckList> checkListPage = checkListRepository.findAll(pageRequest);

        // 2. Entity -> DTO 변환
        return checkListPage.map(CheckListResponse.Converter::from);
    }

    // 키워드 체크리스트 조회(페이지네이션)
    @Override
    public Page<CheckListResponse> searchCheckLists(String keyword, PageRequest pageRequest) {
        Page<CheckList> keywrodCheckListPage = checkListRepository.findByKeyword(keyword, pageRequest);

        return keywrodCheckListPage.map(CheckListResponse.Converter::from);
    }

}