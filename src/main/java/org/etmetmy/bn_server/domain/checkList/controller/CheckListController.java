package org.etmetmy.bn_server.domain.checkList.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/checklists")
public class CheckListController {

    private  final CheckListService checkListService;

    @PostMapping
    public CheckListResponse createCheckList(){
        return checkListService.save();
    }


}
