package org.etmetmy.bn_server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.service.UserService;
import org.etmetmy.bn_server.domain.user.service.UserServiceImpl;
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
    public ResponseEntity<String> updateMember(
            @PathVariable Long memberId,
            @RequestBody MemberUpdateRequest request
    ) {
        userService.updateMember(memberId, request);
        return ResponseEntity.ok("회원 정보 수정 완료");
    }

    //회원 삭제 (DELETE)
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable Long memberId) {
        userService.deleteMember(memberId);
        return ResponseEntity.ok("회원 삭제 완료");
    }

    // 회원 조회 & 검색 API
    @GetMapping
    public ResponseEntity<List<User>> getMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String type
    ) {

        List<User> members = userService.searchMembers(name, email, companyName, type);
        return ResponseEntity.ok(members);
    }
}