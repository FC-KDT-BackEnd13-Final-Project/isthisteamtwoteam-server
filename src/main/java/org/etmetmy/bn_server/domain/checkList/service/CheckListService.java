package org.etmetmy.bn_server.domain.checkList.service;

import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.springframework.data.domain.Page;

public interface CheckListService {

    CheckListResponse save();

    CheckListResponse update(Long checkListId, CheckListUpdateRequest request);

    void delete(Long checkListId);

    Page<CheckListResponse> getCheckLists(PageRequest pageRequest);

    Page<CheckListResponse> searchCheckLists(String keyword, PageRequest pageRequest);
}
