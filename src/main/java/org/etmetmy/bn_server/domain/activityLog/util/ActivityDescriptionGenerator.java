package org.etmetmy.bn_server.domain.activityLog.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityDescriptionGenerator {

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generate(
            ActivityAction action,
            String targetType,
            Long targetId,
            User user,
            Project project,
            String detailJson
    ) {
        String userName = user != null ? user.getName() : "알 수 없는 사용자";
        String projectName = project != null ? project.getProjectName() : "알 수 없는 프로젝트";
        String actionDesc = action.getDescription();

        return switch (targetType) {
            case "Project" -> {
                if (action == ActivityAction.UPDATE && detailJson != null) {
                    // 삭제 마커
                    if (isChecklistAction(detailJson, "checklist_removed")) {
                        String checkListName = extractContent(detailJson); // JSON에서 '빙글빙글...' 추출
                        if (!"내용 없음".equals(checkListName) && !"상세 내용".equals(checkListName)) {
                            yield String.format("%s님이 프로젝트에서 체크리스트 '%s'을(를) 삭제했습니다.", userName, checkListName);
                        }
                        yield String.format("%s님이 프로젝트에서 체크리스트를 삭제했습니다.", userName);
                    }

                    // 추가/생성 마커
                    if (isChecklistAction(detailJson, "checklist_added")) {
                        String checkListName = extractContent(detailJson);
                        if (!"내용 없음".equals(checkListName)) {
                            yield String.format("%s님이 프로젝트에 새 체크리스트 '%s'을(를) 추가했습니다.", userName, checkListName);
                        }
                        yield String.format("%s님이 프로젝트에 체크리스트를 추가했습니다.", userName);
                    }

                    // 일반 수정
                    String diffMessage = parseDiffMessage(detailJson, "프로젝트 이름");
                    yield String.format("%s님이 %s했습니다.", userName, diffMessage);
                }
                yield String.format("%s님이 프로젝트 '%s'를 %s했습니다.", userName, projectName, actionDesc);
            }

            case "Post" -> {
                if (action == ActivityAction.UPDATE && detailJson != null) {

                    // 1. 거절 여부를 가장 먼저 확인
                    if (isRejection(detailJson)) {
                        String contentSnippet = extractContent(detailJson);
                        String reason = extractRejectReason(detailJson);
                        yield String.format("%s님이 게시글 '%s'을(를) 거절했습니다. (사유: %s)",
                                userName, contentSnippet, reason);
                    }

                    // 2. 승인 여부 확인
                    if (isApproval(detailJson)) {
                        String contentSnippet = extractContent(detailJson);
                        yield String.format("%s님이 게시글 '%s'을(를) 승인했습니다.",
                                userName, contentSnippet);
                    }

                    // 3. 위 두 경우가 아닐 때만 일반 수정 메시지 생성
                    String diffMessage = parseDiffMessage(detailJson, "게시글 제목");
                    yield String.format("%s님이 %s했습니다.", userName, diffMessage);
                }

                String contentSnippet = extractContent(detailJson);
                yield String.format("%s님이 게시글 '%s'를 %s했습니다.",
                        userName, contentSnippet, actionDesc);
            }

            case "Comment" -> {
                if (action == ActivityAction.UPDATE && detailJson != null) {
                    String diffMessage = parseDiffMessage(detailJson, "댓글 내용");
                    yield String.format("%s님이 %s했습니다.", userName, diffMessage);
                }
                String contentSnippet = extractContent(detailJson);
                yield String.format("%s님이 댓글[%s]을 %s했습니다.",
                        userName, contentSnippet, actionDesc);
            }

            case "ProjectMember" -> {
                String memberName = extractMemberName(detailJson);
                String memberRole = extractMemberRole(detailJson);

                if (action == ActivityAction.CREATE) {
                    yield String.format("%s님이 프로젝트 '%s'에 %s(%s)을(를) 추가했습니다.",
                            userName, projectName, memberName, memberRole);
                } else if (action == ActivityAction.DELETE) {
                    yield String.format("%s님이 프로젝트 '%s'에서 %s(%s)을(를) 제거했습니다.",
                            userName, projectName, memberName, memberRole);
                }
                yield String.format("%s님이 프로젝트 멤버를 %s했습니다.",
                        userName, actionDesc);
            }

            case "File" -> {
                String fileName = extractFileName(detailJson);

                if (action == ActivityAction.DELETE) {
                    yield String.format("%s님이 파일 '%s'을(를) 삭제했습니다.",
                            userName, fileName);
                } else if (action == ActivityAction.UPDATE) {
                    yield String.format("%s님이 파일 '%s'을(를) 복원했습니다.",
                            userName, fileName);
                }
                yield String.format("%s님이 파일을 %s했습니다.",
                        userName, actionDesc);
            }

            case "CheckList" -> {
                String checkListName = extractContent(detailJson);

                if (action == ActivityAction.CREATE) {
                    yield String.format("%s님이 체크리스트 템플릿 '%s'을(를) 생성했습니다.",
                            userName, checkListName);
                } else if (action == ActivityAction.UPDATE) {
                    String diffMessage = parseDiffMessage(detailJson, "체크리스트 이름");
                    yield String.format("%s님이 %s했습니다.", userName, diffMessage);
                } else if (action == ActivityAction.DELETE) {
                    yield String.format("%s님이 체크리스트 템플릿 '%s'을(를) 삭제했습니다.",
                            userName, checkListName);
                }

                yield String.format("%s님이 체크리스트를 %s했습니다.",
                        userName, actionDesc);
            }

            default -> String.format("%s님이 %s를 %s했습니다.",
                    userName, targetType, actionDesc);
        };
    }

    private String parseDiffMessage(String json, String defaultFieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String fieldName = defaultFieldName;
            if (node.has("type") && "STAGE".equals(node.get("type").asText())) {
                fieldName = "진행 단계";
            }

            String oldVal = node.path("old").asText("");
            String newVal = node.path("new").asText("");

            if (oldVal.equals(newVal) || oldVal.isEmpty()) {
                return String.format("%s을(를) '%s'(으)로 수정", fieldName, newVal);
            }
            return String.format("%s을(를) '%s'에서 '%s'(으)로 변경", fieldName, oldVal, newVal);
        } catch (Exception e) {
            return "정보를 수정";
        }
    }

    private String extractContent(String json) {
        if (json == null) return "내용 없음";
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("content")) return node.get("content").asText();
            if (node.has("new")) return node.get("new").asText();
            if (node.has("projectName")) return node.get("projectName").asText();
            if (node.has("title")) return node.get("title").asText();
            return "상세 내용";
        } catch (Exception e) {
            return "내용 없음";
        }
    }

    private String extractMemberName(String json) {
        if (json == null) return "알 수 없는 사용자";
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("userName")) {
                return node.get("userName").asText();
            }
        } catch (Exception e) {
            // 예외 무시
        }
        return "알 수 없는 사용자";
    }

    private String extractMemberRole(String json) {
        if (json == null) return "";
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("role")) {
                String role = node.get("role").asText();
                return switch (role) {
                    case "ADMIN" -> "관리자";
                    case "DEVELOPER" -> "개발자";
                    case "CUSTOMER" -> "고객사";
                    default -> role;
                };
            }
        } catch (Exception e) {
            // 예외 무시
        }
        return "";
    }

    private String extractFileName(String json) {
        if (json == null) return "알 수 없는 파일";
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("fileName")) {
                return node.get("fileName").asText();
            }
            if (node.has("content")) {
                return node.get("content").asText();
            }
        } catch (Exception e) {
            // 예외 무시
        }
        return "알 수 없는 파일";
    }

    private boolean isApproval(String json) {
        if (json == null) return false;
        try {
            JsonNode node = objectMapper.readTree(json);
            // AOP에서 명시적으로 넣어준 "action": "approved" 체크
            if (node.has("action") && "approved".equals(node.get("action").asText())) {
                return true;
            }
            return node.path("isApproved").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isRejection(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            // JSON에 "action":"rejected"가 있는지 확인
            return "rejected".equals(node.path("action").asText());
        } catch (Exception e) {
            return false;
        }
    }

    private String extractRejectReason(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            // JSON에 담긴 "rejectReason" 값을 추출
            return node.path("rejectReason").asText("사유 없음");
        } catch (Exception e) {
            return "사유 없음";
        }
    }

    private boolean isChecklistAction(String json, String actionTag) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return actionTag.equals(node.path("action").asText());
        } catch (Exception e) { return false; }
    }
}