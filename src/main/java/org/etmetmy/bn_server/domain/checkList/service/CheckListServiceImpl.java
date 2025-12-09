package org.etmetmy.bn_server.domain.checkList.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
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
    public CheckListResponse save(CheckListCreateRequest request) {
        CheckList entity = CheckListCreateRequest.Converter.toEntity(request);
        CheckList saved = checkListRepository.save(entity);
        return CheckListResponse.Converter.from(saved);
    }

    @Override
    @Transactional
    public CheckListResponse update(Long checkListId, CheckListUpdateRequest request) {

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(()-> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));
        CheckListUpdateRequest.Converter.updateEntity(request, checkList);

        return CheckListResponse.Converter.from(checkList);
    }

    @Override
    public void delete(Long checkListId) {
        checkListRepository.deleteById(checkListId);
    }

    // 페이지네이션 체크리스트 전체 조회
    @Override
    public Page<CheckListResponse> getCheckLists(PageRequest pageRequest) {
        Page<CheckList> checkListPage = checkListRepository.findAll(pageRequest);
        return checkListPage.map(CheckListResponse.Converter::from);
    }

    // 키워드 체크리스트 조회(페이지네이션)
    @Override
    public Page<CheckListResponse> searchCheckLists(String keyword, PageRequest pageRequest) {
        Page<CheckList> keywrodCheckListPage = checkListRepository.findByKeyword(keyword, pageRequest);
        return keywrodCheckListPage.map(CheckListResponse.Converter::from);
    }

}