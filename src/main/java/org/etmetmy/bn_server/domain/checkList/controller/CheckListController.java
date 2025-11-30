package org.etmetmy.bn_server.domain.checkList.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/checklists")
public class CheckListController {

    private  final CheckListService checkListService;

    @PostMapping
    public CheckListResponse createCheckList(){
        return checkListService.save();
    }

    @PatchMapping("/{checkListId}")
    public CheckListResponse update(@PathVariable("checkListId") Long checkListId,
                                    @RequestBody CheckListUpdateRequest request){

        return checkListService.update(checkListId, request);
    }

    @DeleteMapping("/{checkListId}")
    public void deleteCheckList(@PathVariable("checkListId") Long checkListId){

        checkListService.delete(checkListId);
    }



}
