package org.etmetmy.bn_server.domain.checkList.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListFileLinkCreateRequest;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.etmetmy.bn_server.domain.link.service.LinkService;
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
    private final LinkService linkService;

    // todo : 체크리스트 체크 기능
    @Operation(summary = "체크리스트 체크 기능", description = "체크를 누를때마다 true, false 전환")
    @ActivityLogger(targetType = "CheckList", action = "UPDATE")
    @PatchMapping("/{projectCheckListId}/checked")
    public CommonResponse<Object> updateChecked(
            @PathVariable Long projectId,
            @PathVariable Long projectCheckListId,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);

        checkListService.updateChecked(projectCheckListId, userId);

        return CommonResponse.success("성공했습니다", null);
    }

    // todo : 체크리스트 답변 내용 업데이트
    @Operation(summary = "체크리스트 답변 내용 업데이트", description = "답변 내용 업데이트")
    @ActivityLogger(targetType = "CheckList", action = "UPDATE")
    @PatchMapping("/{projectCheckListId}/content")
    public CommonResponse<Object> updateContent(
            @PathVariable Long projectId,
            @PathVariable Long projectCheckListId,
            @RequestBody ProjectCheckListReasonRequest reason
    ) {
        checkListService.updateContent(projectCheckListId, reason);

        return CommonResponse.success("성공했습니다", null);
    }

    // todo : 체크리스트에 파일 추가
    @Operation(summary = "체크리스트 파일 추가", description = "체크리스트 파일 추가")
    @PostMapping("/{projectCheckListId}/file")
    public CommonResponse<Object> createFile(
            @PathVariable Long projectCheckListId,
            @RequestBody CheckListFileLinkCreateRequest fileCreateRequest,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);

        checkListService.saveFile(userId, projectCheckListId, fileCreateRequest);

        return CommonResponse.success("성공했습니다", null);
    }

    // todo : 체크리스트에 링크 추가
    @Operation(summary = "체크리스트 링크 추가", description = "체크리스트 링크 추가")
    @PostMapping("/{projectCheckListId}/link")
    public CommonResponse<Object> createLink(
            @PathVariable Long projectCheckListId,
            @RequestBody CheckListFileLinkCreateRequest linkCreateRequest,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);

        checkListService.saveLink(userId, projectCheckListId, linkCreateRequest);

        return CommonResponse.success("성공했습니다", null);
    }

    // todo : 체크리스트에 파일 삭제(soft)
    @Operation(summary = "체크리스트 파일 소프트 삭제", description = "체크리스트 파일 소프트 삭제")
    @DeleteMapping("/{fileId}")
    public CommonResponse<Object> deleteFile(
            @PathVariable Long fileId,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);
        checkListService.deletefile(userId, fileId);

        return CommonResponse.success("성공했습니다", null);
    }
}
