package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectDateUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectNameUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectStageUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectStageUpdateResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectTrashResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectUpdateResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectStageRepository;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectStageRepository projectStageRepository;
    @Mock
    private MemoRepository memoRepository;
    @Mock
    private ProjectCheckListRepository projectChecklistRepository;
    @Mock
    private CheckListRepository checkListRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("프로젝트 진행단계 수정 시 요청 사용자 ID가 updatedBy에 반영된다")
    void updateProjectStage_setsUpdatedBy_whenUserExists() {
        Long projectId = 1L;
        Long stageId = 2L;
        Long userId = 10L;

        Stage stage = Stage.builder().id(stageId).stageName("검수").build();
        Project project = Project.builder()
                .id(projectId)
                .projectName("프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .stage(stage)
                .build();

        Company company = Company.builder().companyId(1L).companyName("company").build();
        User user = User.builder()
                .id(userId)
                .company(company)
                .email("user@test.com")
                .password("pass")
                .name("사용자")
                .role(Role.ADMIN)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectStageRepository.findById(stageId)).thenReturn(Optional.of(stage));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.updateProjectStage(projectId, stageId)).thenReturn(1);

        ProjectStageUpdateResponse response = projectService.updateProjectStage(
                projectId,
                new ProjectStageUpdateRequest(stageId),
                userId
        );

        assertThat(response.getUpdatedBy()).isEqualTo(userId);
        verify(projectRepository).updateProjectStage(projectId, stageId);
    }

    @Test
    @DisplayName("권한과 관계없이 요청 사용자 ID가 updatedBy에 세팅된다")
    void updateProjectStage_setsUpdatedBy_forNonAdmin() {
        Long projectId = 2L;
        Long stageId = 3L;
        Long userId = 20L;

        Stage stage = Stage.builder().id(stageId).stageName("개발").build();
        Project project = Project.builder()
                .id(projectId)
                .projectName("프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .stage(stage)
                .build();

        Company company = Company.builder().companyId(1L).companyName("company").build();
        User user = User.builder()
                .id(userId)
                .company(company)
                .email("user@test.com")
                .password("pass")
                .name("사용자")
                .role(Role.DEVELOPER)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectStageRepository.findById(stageId)).thenReturn(Optional.of(stage));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.updateProjectStage(projectId, stageId)).thenReturn(1);

        ProjectStageUpdateResponse response = projectService.updateProjectStage(
                projectId,
                new ProjectStageUpdateRequest(stageId),
                userId
        );

        assertThat(response.getUpdatedBy()).isEqualTo(userId);
        verify(projectRepository).updateProjectStage(projectId, stageId);
    }

    @Test
    @DisplayName("currentUserId가 null이면 updatedBy는 null")
    void updateProjectStage_setsNullWhenNoUser() {
        Long projectId = 5L;
        Long stageId = 6L;

        Stage stage = Stage.builder().id(stageId).stageName("검수").build();
        Project project = Project.builder()
                .id(projectId)
                .projectName("프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .stage(stage)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectStageRepository.findById(stageId)).thenReturn(Optional.of(stage));
        when(projectRepository.updateProjectStage(projectId, stageId)).thenReturn(1);

        ProjectStageUpdateResponse response = projectService.updateProjectStage(
                projectId,
                new ProjectStageUpdateRequest(stageId),
                null
        );

        assertThat(response.getUpdatedBy()).isNull();
        verify(projectRepository).updateProjectStage(projectId, stageId);
    }

    @Test
    @DisplayName("진행단계가 존재하지 않으면 예외가 발생한다")
    void updateProjectStage_throws_whenStageMissing() {
        Long projectId = 1L;
        Long missingStageId = 999L;

        Project project = Project.builder()
                .id(projectId)
                .projectName("프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectStageRepository.findById(missingStageId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> projectService.updateProjectStage(
                projectId,
                new ProjectStageUpdateRequest(missingStageId),
                null
        ));

        verify(projectStageRepository).findById(missingStageId);
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("프로젝트 제목 수정 시 업데이트 결과 반환")
    void updateProjectName_updatesTitle() {
        Long projectId = 12L;
        String newName = "새 프로젝트";

        Project existingProject = Project.builder()
                .id(projectId)
                .projectName("기존 프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 2, 1))
                .build();

        Project updatedProject = Project.builder()
                .id(projectId)
                .projectName(newName)
                .startDate(existingProject.getStartDate())
                .endDate(existingProject.getEndDate())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(existingProject), Optional.of(updatedProject));
        when(projectRepository.updateProjectName(projectId, newName)).thenReturn(1);

        ProjectUpdateResponse response = projectService.updateProjectName(
                projectId,
                new ProjectNameUpdateRequest(newName)
        );

        assertThat(response.getProjectId()).isEqualTo(projectId);
        verify(projectRepository).updateProjectName(projectId, newName);
    }

    @Test
    @DisplayName("빈 제목으로 수정 시 예외 발생")
    void updateProjectName_failsOnBlankName() {
        assertThrows(InvalidInputException.class, () -> projectService.updateProjectName(
                1L,
                new ProjectNameUpdateRequest("   ")
        ));
    }

    @Test
    @DisplayName("프로젝트 날짜 수정 시 시작일과 종료일이 업데이트된다")
    void updateProjectDate_updatesDates() {
        Long projectId = 3L;
        LocalDate newStart = LocalDate.of(2025, 5, 1);
        LocalDate newEnd = LocalDate.of(2025, 6, 1);

        Project project = Project.builder()
                .id(projectId)
                .projectName("테스트 프로젝트")
                .startDate(LocalDate.of(2025, 4, 1))
                .endDate(LocalDate.of(2025, 4, 30))
                .build();

        Project updatedProject = Project.builder()
                .id(projectId)
                .projectName("테스트 프로젝트")
                .startDate(newStart)
                .endDate(newEnd)
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project), Optional.of(updatedProject));
        when(projectRepository.updateProjectDates(projectId, newStart, newEnd)).thenReturn(1);

        ProjectUpdateResponse response = projectService.updateProjectDate(
                projectId,
                new ProjectDateUpdateRequest(newStart.toString(), newEnd.toString())
        );

        assertThat(response.getProjectId()).isEqualTo(projectId);
        verify(projectRepository).updateProjectDates(projectId, newStart, newEnd);
    }

    @Test
    @DisplayName("종료일이 시작일보다 빠르면 예외를 던진다")
    void updateProjectDate_throwsWhenEndBeforeStart() {
        Long projectId = 4L;

        Project project = Project.builder()
                .id(projectId)
                .projectName("프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 2))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(InvalidInputException.class, () -> projectService.updateProjectDate(
                projectId,
                new ProjectDateUpdateRequest("2025-02-10", "2025-02-01")
        ));
    }

    @Test
    @DisplayName("프로젝트 휴지통 이동 성공")
    void deleteProject_movesToTrash() {
        Long projectId = 7L;

        Project project = Project.builder()
                .id(projectId)
                .projectName("삭제 대상 프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .isDeleted(false)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.moveToTrash(eq(projectId), any(LocalDateTime.class))).thenReturn(1);

        ProjectTrashResponse response = projectService.deleteProject(projectId);

        assertThat(response.getProjectId()).isEqualTo(projectId);
        verify(projectRepository).moveToTrash(eq(projectId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("이미 삭제된 프로젝트는 다시 삭제할 수 없다")
    void deleteProject_throwsWhenAlreadyDeleted() {
        Long projectId = 8L;

        Project deletedProject = Project.builder()
                .id(projectId)
                .projectName("삭제됨")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 2))
                .isDeleted(true)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(deletedProject));

        assertThrows(BusinessException.class, () -> projectService.deleteProject(projectId));
    }

    @Test
    @DisplayName("프로젝트 멤버 삭제 성공")
    void removeProjectMember_deletesMapping() {
        Long projectId = 20L;
        Long userId = 30L;

        Project project = Project.builder()
                .id(projectId)
                .projectName("멤버 삭제 프로젝트")
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .isDeleted(false)
                .build();

        User user = User.builder()
                .id(userId)
                .company(Company.builder().companyId(1L).companyName("회사").build())
                .name("사용자")
                .email("user@test.com")
                .password("pw")
                .role(Role.DEVELOPER)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectMemberRepository.deleteByProject_IdAndUser_Id(projectId, userId)).thenReturn(1L);

        projectService.removeProjectMember(projectId, userId);

        verify(projectMemberRepository).deleteByProject_IdAndUser_Id(projectId, userId);
    }

    @Test
    @DisplayName("프로젝트 멤버가 없으면 예외 발생")
    void removeProjectMember_throwsWhenMappingMissing() {
        Long projectId = 21L;
        Long userId = 31L;

        Project project = Project.builder()
                .id(projectId)
                .projectName("멤버 삭제 프로젝트")
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .isDeleted(false)
                .build();

        User user = User.builder()
                .id(userId)
                .company(Company.builder().companyId(1L).companyName("회사").build())
                .name("사용자")
                .email("user@test.com")
                .password("pw")
                .role(Role.DEVELOPER)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectMemberRepository.deleteByProject_IdAndUser_Id(projectId, userId)).thenReturn(0L);

        assertThrows(BusinessException.class, () -> projectService.removeProjectMember(projectId, userId));
    }

    @Test
    @DisplayName("멤버 목록이 없으면 추가하지 않는다")
    void addProjectMembers_returnsZeroWhenEmpty() {
        int addedCount = projectService.addProjectMembers(1L, Collections.emptyList(), 2L);

        assertThat(addedCount).isZero();
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("단계가 지정되지 않은 프로젝트 생성 시 예외 발생")
    void createProject_throwsWhenStageMissing() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "새 프로젝트",
                "2025-01-01",
                "2025-02-01",
                List.of(1L, 2L),
                List.of(),
                1L,
                "메모",
                null
        );

        assertThrows(InvalidInputException.class, () -> projectService.createProject(request, 100L));
    }
}
