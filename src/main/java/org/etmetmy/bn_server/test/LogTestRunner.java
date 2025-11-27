package org.etmetmy.bn_server.global.init;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.dto.LogDetail;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogService;
import org.etmetmy.bn_server.global.util.DiffUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogTestRunner implements CommandLineRunner {

    private final ActivityLogService activityLogService;

    // [테스트용 가짜 클래스] 팀원이 Post 만들기 전까지 이걸로 테스트
    @Getter
    @AllArgsConstructor
    static class TempPost {
        private Long id;
        private String title;
        private String content;
        private String status;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============== 자동 변경 감지 테스트 시작 ==============");

        // 1. 상황극: 사용자가 게시글을 수정함
        TempPost oldPost = new TempPost(1L, "안녕하세요", "반갑습니다", "TODO"); // DB에 있던 거
        TempPost newPost = new TempPost(1L, "안녕하세요 (수정)", "반갑습니다", "DONE"); // 수정된 거

        // 2. DiffUtil 작동
        // 두 객체를 비교해서 알아서 리스트를 만들어 줍니다.
        List<LogDetail> diffs = DiffUtil.extractDiff(oldPost, newPost);

        System.out.println(">>> 감지된 변경 사항 개수: " + diffs.size());
        diffs.forEach(d -> System.out.println(
                "필드명: " + d.getField() + " | " + d.getOldValue() + " -> " + d.getNewValue())
        );

        // 3. 실제 DB 저장 (변경된 게 있을 때만)
        if (!diffs.isEmpty()) {
            activityLogService.saveLog(
                    1L,
                    100L,
                    ActivityAction.UPDATE,
                    "Post",
                    oldPost.getId(),
                    diffs // 자동 생성된 diff 리스트를 넣음!
            );
        }

        System.out.println("============== 자동 변경 감지 테스트 종료 ==============");
    }
}