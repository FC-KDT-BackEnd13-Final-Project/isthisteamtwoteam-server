package org.etmetmy.bn_server.domain.checkList.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.etmetmy.bn_server.global.page.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/checklists")
public class CheckListController {

    private final CheckListService checkListService;

    // todo : 체크리스트 생성
    @PostMapping
    public CommonResponse<CheckListResponse> createCheckList(@RequestBody CheckListCreateRequest request) {
        return CommonResponse.success("체크리스트를 생성했습니다", checkListService.save(request));
    }

    // todo : 체크리스트 수정
    @PatchMapping("/{checkListId}")
    public CommonResponse<CheckListResponse> update(@PathVariable("checkListId") Long checkListId,
                                                    @RequestBody CheckListUpdateRequest request) {

        return CommonResponse.success("체크리스트를 수정했습니다.", checkListService.update(checkListId, request));
    }

    // todo : 체크리스트 삭제
    @DeleteMapping("/{checkListId}")
    public CommonResponse<Object> deleteCheckList(@PathVariable("checkListId") Long checkListId) {

        checkListService.delete(checkListId);
        return CommonResponse.success("체크리스트를 삭제했습니다.");
    }

    // todo : 체크리스트 조회 및 검색
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

        // keyword가 빈 문자열("")로 명시적으로 전달된 경우도 전체 조회로 처리
        String trimmedKeyword = (keyword != null) ? keyword.trim() : null;

        if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            result = checkListService.searchCheckLists(trimmedKeyword, pageRequest);
        } else {
            result = checkListService.getCheckLists(pageRequest);
        }
        // PageResponse로 변환하여 깔끔한 응답 반환
        return CommonResponse.success("성공적으로 페이지를 조회하였습니다.", PageResponse.of(result));
    }
}