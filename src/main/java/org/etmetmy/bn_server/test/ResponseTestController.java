package org.etmetmy.bn_server.test;

import static org.etmetmy.bn_server.global.StatusCode.POST_CREATED;
import static org.etmetmy.bn_server.global.StatusCode.POST_FOUND;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class ResponseTestController {

    @PostMapping()
    public ResponseEntity<CommonResponse<String>> postString(@RequestBody MessageReqDto dto) {
        return ResponseEntity.ok(CommonResponse.success(POST_CREATED.getMessage(), dto.getMessage()));
    }


    @GetMapping()
    public ResponseEntity<CommonResponse<Object>> getString(){
        return ResponseEntity.ok(CommonResponse.success(POST_FOUND.getMessage()));
    }
}
