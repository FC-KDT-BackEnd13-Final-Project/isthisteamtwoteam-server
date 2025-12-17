package org.etmetmy.bn_server.domain.memo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.dto.request.MemoUpdateRequestDto;
import org.etmetmy.bn_server.domain.memo.service.MemoService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Memo", description = "메모 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects")
public class MemoController {

    private final MemoService memoService;

    //todo: 프로젝트에서 개인 메모 생성/업데이트
    @Operation(summary = "개인 메모 수정", description = "프로젝트의 개인 메모를 생성/수정합니다")
    @PatchMapping("/{projectId}/user/memos")
    public CommonResponse<Long> updateUserMemo(
            @PathVariable Long projectId,
            @RequestBody MemoUpdateRequestDto memoUpdateRequestDto,
            HttpSession session)
    {
        Long userId = SessionUtil.getLoginUserId(session);
        Long memoId = memoService.updateUserMemo(userId, projectId, memoUpdateRequestDto.getContent());

        return CommonResponse.success("개인 메모 생성/수정 성공", memoId);
    }

    //todo: 프로젝트에서 공통 메모 생성/업데이트
    @Operation(summary = "프로젝트 공통 메모 수정", description = "프로젝트의 공통 메모를 생성/수정합니다")
    @PatchMapping("/{projectId}/main/memos")
    public CommonResponse<Object> updateProjectMemo(
            @PathVariable Long projectId,
            @RequestBody MemoUpdateRequestDto memoUpdateRequestDto,
            HttpSession session)
    {
        Long userId = SessionUtil.getLoginUserId(session);
        Long memoId = memoService.updateProjectMemo(userId, projectId, memoUpdateRequestDto.getContent());

        return CommonResponse.success("공통 메모 생성/수정 성공", memoId);
    }
}