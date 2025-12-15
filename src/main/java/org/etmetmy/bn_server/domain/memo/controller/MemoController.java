package org.etmetmy.bn_server.domain.memo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.dto.request.MemoUpdateRequestDto;
import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;
import org.etmetmy.bn_server.domain.memo.service.MemoService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Memo", description = "메모 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects")
public class MemoController {

    private final MemoService memoService;

    /**
     * 프로젝트에서 공통 메모 조회
     * */
    @Operation(summary = "프로젝트 공통 메모 조회", description = "프로젝트의 공통 메모를 조회합니다")
    @GetMapping("/{projectId}/main/memo")
    public CommonResponse<MemoResponse> getProjectMemo(
            @PathVariable Long projectId
    ) {
        MemoResponse response = memoService.getProjectMemos(projectId);
        return CommonResponse.success("프로젝트 메모 조회 완료", response);
    }

    /**
    * 프로젝트에서 개인 메모 조회
    * */
    @Operation(summary = "개인 메모 조회", description = "프로젝트의 개인 메모를 조회합니다")
    @GetMapping("/{projectId}/user/memos")
    public CommonResponse<MemoResponse> getUserMemo(
            @PathVariable Long projectId,
            HttpSession session
    ){

        Long userId = SessionUtil.getLoginUserId(session);
        MemoResponse memo = memoService.getUserMemo(userId,projectId);

        if(memo == null){
            return CommonResponse.success("개인 메모가 존재하지 않습니다.", memo);
        }else{
            return CommonResponse.success("개인 메모 조회를 성공적으로 완료했습니다.", memo);
        }
    }

    /**
     * 프로젝트에서 개인 메모 업데이트
     * */
    @Operation(summary = "개인 메모 수정", description = "프로젝트의 개인 메모를 수정합니다")
    @PatchMapping("/{projectId}/user/memos")
    public CommonResponse<Long> updateUserMemo(
            @PathVariable Long projectId,
            @RequestBody MemoUpdateRequestDto memoUpdateRequestDto,
            HttpSession session

    ){Long userId = SessionUtil.getLoginUserId(session);
       Long memoId = memoService.updateUserMemo(userId, projectId, memoUpdateRequestDto.getContent());

       return CommonResponse.success("성공적으로 메모를 수정하였습니다.", memoId);
    }

    /**
     * 프로젝트에서 공통 메모 업데이트
     * */
    @Operation(summary = "프로젝트 공통 메모 수정", description = "프로젝트의 공통 메모를 수정합니다")
    @PatchMapping("/{projectId}/main/memos")
    public CommonResponse<Object> updateProjectMemo(
            @PathVariable Long projectId,
            @RequestBody MemoUpdateRequestDto memoUpdateRequestDto,
            HttpSession session

    ){
        Long userId = SessionUtil.getLoginUserId(session);

        Long memoId = memoService.updateProjectMemo(userId, projectId, memoUpdateRequestDto.getContent());
        if(memoId != null){
            return CommonResponse.success("성공적으로 메모를 수정하였습니다.", memoId);

        }else{
            return  CommonResponse.success("메모를 수정할 수 있는 권한이 없습니다.");
        }

    }
}