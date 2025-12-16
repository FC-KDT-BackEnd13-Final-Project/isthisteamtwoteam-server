package org.etmetmy.bn_server.domain.checkList.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCheckListReasonRequest;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CheckList", description = "체크리스트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/checklists/{projectId}")
public class CheckListCustomerController {

    private final CheckListService checkListService;

    @PatchMapping("/{checkListId}/checked")
    public CommonResponse<Object> updateChecked(
            @PathVariable Long projectId,
            @PathVariable Long checkListId,
            HttpSession session
    ){
        Long userId = SessionUtil.getLoginUserId(session);

        checkListService.updateChecked(projectId, checkListId, userId);

        return CommonResponse.success("성공했습니다",null);
    }

    @PatchMapping("/{checkListId}/content")
    public CommonResponse<Object> updateContent(
            @PathVariable Long projectId,
            @PathVariable Long checkListId,
            @RequestBody ProjectCheckListReasonRequest reason
    ){
        checkListService.updateContent(projectId, checkListId, reason);

        return CommonResponse.success("성공했습니다",null);
    }
}
