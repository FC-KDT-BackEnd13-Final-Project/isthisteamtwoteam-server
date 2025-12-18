package org.etmetmy.bn_server.domain.history.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.history.service.HistoryService;
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

    @GetMapping("/{postId}")
    public void getAllHistory(
            @PathVariable Long postId
    )
    {

    }
}
