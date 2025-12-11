package org.etmetmy.bn_server.domain.activityLog.util;

import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 활동 로그에 기록될 한글 설명을 생성하
 */
@Component
public class ActivityDescriptionGenerator {


    public String generate(ActivityAction action, String targetType, Long targetId,
                           User user, Project project) {

        String userName = user != null ? user.getName() : "알 수 없는 사용자";
        String projectName = project != null ? project.getProjectName() : "알 수 없는 프로젝트";

        // targetType을 한글로 변환
        String targetTypeKorean = convertTargetTypeToKorean(targetType);

        // 액션 타입에 따른 메시지 생성
        return switch (action) {
            case CREATE -> String.format("%s님이 [%s] 프로젝트에 새로운 %s을(를) 작성했습니다.",
                    userName, projectName, targetTypeKorean);

            case UPDATE -> String.format("%s님이 [%s] 프로젝트의 %s을(를) 수정했습니다.",
                    userName, projectName, targetTypeKorean);

            case DELETE -> String.format("%s님이 [%s] 프로젝트의 %s을(를) 삭제했습니다.",
                    userName, projectName, targetTypeKorean);

            case APPROVE -> String.format("%s님이 [%s] 프로젝트의 %s 요청을 승인했습니다.",
                    userName, projectName, targetTypeKorean);

            case REJECT -> String.format("%s님이 [%s] 프로젝트의 %s 요청을 거절했습니다.",
                    userName, projectName, targetTypeKorean);

            case COMPLETE -> String.format("%s님이 [%s] 프로젝트의 %s을(를) 완료 처리했습니다.",
                    userName, projectName, targetTypeKorean);

            case ASSIGN -> String.format("%s님이 [%s] 프로젝트의 %s 담당자를 지정했습니다.",
                    userName, projectName, targetTypeKorean);

            case UNASSIGN -> String.format("%s님이 [%s] 프로젝트의 %s 담당자를 해제했습니다.",
                    userName, projectName, targetTypeKorean);

            case STATUS_CHANGE -> String.format("%s님이 [%s] 프로젝트의 %s 상태를 변경했습니다.",
                    userName, projectName, targetTypeKorean);

            case COMMENT_ADD -> String.format("%s님이 [%s] 프로젝트의 %s에 댓글을 작성했습니다.",
                    userName, projectName, targetTypeKorean);

            default -> String.format("%s님이 [%s] 프로젝트의 %s에 대해 작업을 수행했습니다.",
                    userName, projectName, targetTypeKorean);
        };
    }

    private String convertTargetTypeToKorean(String targetType) {
        if (targetType == null) {
            return "항목";
        }

        return switch (targetType.toLowerCase()) {
            case "post" -> "게시글";
            case "comment" -> "댓글";
            case "project" -> "프로젝트";
            case "user" -> "사용자";
            case "file" -> "파일";
            case "link" -> "링크";
            case "task" -> "작업";
            case "checklist" -> "체크리스트";
            default -> targetType; // 알 수 없는 경우 원본 반환
        };
    }
}