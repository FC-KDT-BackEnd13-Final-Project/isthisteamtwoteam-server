package org.etmetmy.bn_server.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashBoardStatusResponseDTO {

    // 게시글 관련
    private Long postId;                    // 게시글 id
    private String title;                   // 게시글 제목
    private String postStageName;           // 게시글 단계 이름
    private RequestStatus requestStatus;    // 요청 대기중인
    private LocalDateTime createdAt;        // 생성일

    // 프로젝트 관련
    private Long projectId;                 // 프로젝트 id
    private String projectName;             // 프로젝트 이름
    private String projectStageName;        // 프로젝트 단계 이름

    // 회사 관련
    private String companyName;             // 회사 이름

    public static class Converter {
        // Post 반환
        public static DashBoardStatusResponseDTO toDTO(Post post, RequestStatus requestStatus) {
            return DashBoardStatusResponseDTO.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .postStageName(post.getStage().getStageName())
                    .requestStatus(requestStatus)
                    .createdAt(post.getCreatedAt())
                    .projectId(post.getProject().getId())
                    .projectName(post.getProject().getProjectName())
                    .companyName(post.getProject().getCompany().getCompanyName())
                    .build();
        }

        // Post 반환
        public static List<DashBoardStatusResponseDTO> toPostDTOList(List<Post> posts, RequestStatus requestStatus) {
            return posts.stream()
                    .map(post -> toDTO(post, requestStatus))
                    .collect(Collectors.toList());
        }

        // Project 반환
        public static DashBoardStatusResponseDTO toProjectDTO(Project project) {
            return DashBoardStatusResponseDTO.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .companyName(project.getCompany().getCompanyName())
                    .projectStageName(project.getStage().getStageName())
                    .build();
        }

        public static List<DashBoardStatusResponseDTO> toProejctDTOList(List<Project> inProgressProjects) {
            return inProgressProjects.stream()
                    .map(project -> toProjectDTO(project))
                    .collect(Collectors.toList());
        }
    }
}