package org.etmetmy.bn_server.domain.checkList.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.StatusCode;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.etmetmy.bn_server.global.page.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/checklists")
public class CheckListController {

    private  final CheckListService checkListService;

    // 체크리스트 생성
    @PostMapping
    public CheckListResponse createCheckList(){
        return checkListService.save();
    }

    // 체크리스트 수정
    @PatchMapping("/{checkListId}")
    public CheckListResponse update(@PathVariable("checkListId") Long checkListId,
                                    @RequestBody CheckListUpdateRequest request){

        return checkListService.update(checkListId, request);
    }

    // 체크리스트 삭제
    @DeleteMapping("/{checkListId}")
    public void deleteCheckList(@PathVariable("checkListId") Long checkListId){

        checkListService.delete(checkListId);
    }

    @GetMapping
    public CommonResponse<PageResponse<CheckListResponse>> getAllCheckLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "checkListId") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String keyword
    ) {
        // 동적 정렬 생성
        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // 검색어가 있으면 검색, 없으면 전체 조회
        Page<CheckListResponse> result;
        if (keyword != null && !keyword.isEmpty()){
            result = checkListService.searchCheckLists(keyword, pageRequest);
        }
        else{
            result = checkListService.getCheckLists(pageRequest);
        }

        // PageResponse로 변환하여 깔끔한 응답 반환
        return CommonResponse.success(StatusCode.CHECKLISTS_FOUND.getMessage(), PageResponse.of(result));
    }
}
