package org.etmetmy.bn_server.domain.checkList.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CheckList", description = "체크리스트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/checklists/{projectId}")
public class CheckListCustomerController {

    private final ;

    @PatchMapping
    public void CheckedUpdate(
            @PathVariable Long projectId,
            HttpSession session
    ){


    }
}
