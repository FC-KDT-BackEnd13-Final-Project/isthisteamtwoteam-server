package org.etmetmy.bn_server.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class DashBoardStatusResponseDTO {

    // 게시글 관련
    private Long postId;                    // 게시글 id
    private String title;                   // 게시글 제목
    private String stageName;               // 게시글 단계 이름
    private RequestStatus requestStatus;    // 요청 대기중인
    private LocalDateTime createdAt;        // 생성일

    // 프로젝트 관련
    private String projectName;             // 프로젝트 이름

    // 회사 관련
    private String companyName;             // 회사 이름

    public static class Converter {

        public static DashBoardStatusResponseDTO toDTO(Post post) {
            return DashBoardStatusResponseDTO.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .stageName(post.getStage().getStageName())
                    .requestStatus(RequestStatus.STATUS_PENDING)
                    .createdAt(post.getCreatedAt())
                    .projectName(post.getProject().getProjectName())
                    .companyName(post.getProject().getCompany().getCompanyName())
                    .build();
        }

        public static List<DashBoardStatusResponseDTO> toDTOList(List<Post> posts) {
            return posts.stream()
                    .map(Converter::toDTO)
                    .collect(Collectors.toList());
        }
    }
}