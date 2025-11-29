package org.etmetmy.bn_server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.service.UserService;
import org.etmetmy.bn_server.domain.user.service.UserServiceImpl;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원 정보 수정 (PUT)
    @PutMapping("/{memberId}")
    public CommonResponse<Long> updateMember(
            @PathVariable Long memberId,
            @RequestBody MemberUpdateRequest request
    ) {
        Long userId = userService.updateMember(memberId, request);

        return CommonResponse.success("회원 정보 수정 완료",userId);
    }

    //회원 삭제 (DELETE)
    @DeleteMapping("/{memberId}")
    public CommonResponse<Long> deleteMember(@PathVariable Long memberId) {
        Long userId = userService.deleteMember(memberId);
        return CommonResponse.success("회원 삭제 완료",userId);
    }

    // 회원 조회 & 검색 API
    @GetMapping
    public CommonResponse<List<User>> getMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String type
    ) {

        List<User> members = userService.searchMembers(name, email, companyName, type);
        return CommonResponse.success("회원을 성공적으로 조회하였습니다.", members);
    }
}