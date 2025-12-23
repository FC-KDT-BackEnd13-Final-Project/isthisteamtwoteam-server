package org.etmetmy.bn_server.domain.checkList.service;

import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListFileLinkCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCheckListReasonRequest;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.springframework.data.domain.Page;

public interface CheckListService {

    CheckListResponse save(CheckListCreateRequest request);

    CheckListResponse update(Long checkListId, CheckListUpdateRequest request);

    void delete(Long checkListId);

    Page<CheckListResponse> getCheckLists(PageRequest pageRequest);

    Page<CheckListResponse> searchCheckLists(String keyword, PageRequest pageRequest);

    void updateChecked(Long projectCheckListId, Long userId);

    void updateContent(Long projectCheckListId, ProjectCheckListReasonRequest reason);

    void saveLink(Long userId, Long projectCheckListId, CheckListFileLinkCreateRequest linkCreateRequest);

    void saveFile(Long userId, Long projectCheckListId, CheckListFileLinkCreateRequest fileCreateRequest);

    void deletefile(Long userId, Long fileId);
}
