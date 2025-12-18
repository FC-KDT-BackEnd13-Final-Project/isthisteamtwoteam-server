package org.etmetmy.bn_server.domain.history.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.history.dto.HistoryListResponse;
import org.etmetmy.bn_server.domain.history.service.HistoryService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "History", description = "히스토리 관리 API")
@RestController
@RequestMapping("/api/v1/users/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "게시글 히스토리 조회", description = "특정 게시글의 모든 히스토리를 시간순으로 조회합니다")
    @GetMapping("/posts/{postId}")
    public CommonResponse<HistoryListResponse> getPostHistory(
            @PathVariable Long postId
    ) {
        HistoryListResponse response = historyService.getAllHistory(postId);
        return CommonResponse.success("히스토리 조회 성공", response);
    }
}
