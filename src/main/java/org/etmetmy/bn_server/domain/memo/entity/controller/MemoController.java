package org.etmetmy.bn_server.domain.memo.entity.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.entity.dto.entity.MemoResponse;
import org.etmetmy.bn_server.domain.memo.entity.service.MemoService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects")
public class MemoController {

    private final MemoService memoService;

    @GetMapping("/{projectId}/memo")
    public CommonResponse<List<MemoResponse>> getProjectMemo(
            @PathVariable Long projectId
    ) {
        List<MemoResponse> response = memoService.getProjectMemos(projectId);
        return CommonResponse.success("프로젝트 메모 조회 완료", response);
    }
}