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

}