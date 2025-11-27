package org.etmetmy.bn_server.domain.checkList.service;

import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;

public interface CheckListService {

    CheckListResponse save();

    CheckListResponse update(Long checkListId, CheckListUpdateRequest request);
}
