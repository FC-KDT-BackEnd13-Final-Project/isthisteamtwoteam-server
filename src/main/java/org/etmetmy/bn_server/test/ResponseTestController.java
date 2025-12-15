package org.etmetmy.bn_server.test;

import static org.etmetmy.bn_server.global.StatusCode.POST_CREATED;
import static org.etmetmy.bn_server.global.StatusCode.POST_FOUND;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "테스트 API")
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class ResponseTestController {

    @Operation(summary = "메시지 전송", description = "메시지를 전송하는 테스트 API")
    @PostMapping()
    public ResponseEntity<CommonResponse<String>> postString(@RequestBody MessageReqDto dto) {
        return ResponseEntity.ok(CommonResponse.success(POST_CREATED.getMessage(), dto.getMessage()));
    }

    @Operation(summary = "메시지 조회", description = "메시지를 조회하는 테스트 API")
    @GetMapping()
    public ResponseEntity<CommonResponse<Object>> getString(){
        return ResponseEntity.ok(CommonResponse.success(POST_FOUND.getMessage()));
    }
}
