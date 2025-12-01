package org.etmetmy.bn_server.domain.memo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.dto.entity.MemoResponse;
import org.etmetmy.bn_server.domain.memo.service.MemoService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects")
public class MemoController {

    private final MemoService memoService;

    /**
     * 프로젝트에서 공통 메모 조회
     * */
    @GetMapping("/{projectId}/memo")
    public CommonResponse<List<MemoResponse>> getProjectMemo(
            @PathVariable Long projectId
    ) {
        List<MemoResponse> response = memoService.getProjectMemos(projectId);
        return CommonResponse.success("프로젝트 메모 조회 완료", response);
    }

    /**
    * 프로젝트에서 개인 메모 조회
    * */
    @GetMapping("/{projectId}/users/memos")
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
}