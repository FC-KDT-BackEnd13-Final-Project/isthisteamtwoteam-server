package org.etmetmy.bn_server.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ApprovalRequestResponse {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("post_title")
    private String postTitle;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_ip")
    private String createdIp;

    @JsonProperty("author_name")
    private String authorName;

    private RequestStatus requestStatus;
    private String postStageName;

    private String projectName;

    private String companyName;

    private String approveStatus;

    @JsonProperty("responder_name")
    private String responderName;

    @JsonProperty("reply_time")
    private LocalDateTime replyTime;

    @JsonProperty("reject_reason")
    private String rejectReason;

    private List<FileInfo> files;

    private List<String> links;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FileInfo {
        @JsonProperty("file_id")
        private Long fileId;

        @JsonProperty("original_file_title")
        private String originalFileTitle;

        @JsonProperty("file_path")
        private String filePath;

        @JsonProperty("file_size")
        private String fileSize;

        @JsonProperty("file_type")
        private String fileType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LinkInfo {
        @JsonProperty("link_id")
        private Long linkId;

        @JsonProperty("link_url")
        private String linkUrl;
    }

    public static class Converter {
        public static ApprovalRequestResponse from(Post post, Request request) {
            // author 이름 안전하게 가져오기
            String authorName = null;
            if (post.getUser() != null) {
                try {
                    authorName = post.getUser().getName();
                } catch (Exception e) {
                    // EntityNotFoundException 또는 LazyInitializationException 무시
                    authorName = null;
                }
            }

            // 승인/거절 여부 확인 (대기 중이 아닐 때만 관련 정보 설정)
            boolean isProcessed = request != null &&
                                  request.getApproveStatus() != null &&
                                  request.getApproveStatus() != RequestStatus.STATUS_PENDING;

            String responderName = null;
            LocalDateTime replyTime = null;
            String rejectReason = null;
            List<FileInfo> files = null;
            List<String> links = null;

            if (isProcessed) {
                // responder 이름 안전하게 가져오기
                if (request.getResponder() != null) {
                    try {
                        responderName = request.getResponder().getName();
                    } catch (Exception e) {
                        // EntityNotFoundException 또는 LazyInitializationException 무시
                        responderName = null;
                    }
                }

                replyTime = request.getReplyTime();
                rejectReason = request.getRejectReason();

                // 파일 목록
                if (request.getFiles() != null) {
                    files = request.getFiles().stream()
                        .filter(file -> !file.getIsDeleted())
                        .map(file -> FileInfo.builder()
                            .fileId(file.getFileId())
                            .originalFileTitle(file.getOriginalFileTitle())
                            .filePath(file.getFilePath())
                            .fileSize(file.getFileSize())
                            .fileType(file.getFileType())
                            .build())
                        .collect(Collectors.toList());
                }

                // 링크 목록 (URL만 추출)
                if (request.getLinks() != null) {
                    links = request.getLinks().stream()
                        .filter(link -> !link.getIsDeleted())
                        .map(link -> link.getLinkUrl())
                        .collect(Collectors.toList());
                }
            }

            return ApprovalRequestResponse.builder()
                    .projectId(post.getProject().getId())
                    .postId(post.getPostId())
                    .postTitle(post.getTitle())
                    .projectName(post.getProject().getProjectName())
                    .companyName(post.getProject().getCompany().getCompanyName())
                    .postStageName(post.getStage().getStageName())
                    .approveStatus(request != null && request.getApproveStatus() != null ? request.getApproveStatus().getDescription() : null)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .createdIp(post.getCreatedIp())
                    .authorName(authorName)
                    .requestStatus(post.getRequest().getApproveStatus())
                    .responderName(responderName)
                    .replyTime(replyTime)
                    .rejectReason(rejectReason)
                    .files(files)
                    .links(links)
                    .build();
        }

        public static List<ApprovalRequestResponse> from(List<Post> posts) {
            return posts.stream()
                    .map(post -> from(post, post.getRequest()))
                    .collect(Collectors.toList());
        }
    }
}
