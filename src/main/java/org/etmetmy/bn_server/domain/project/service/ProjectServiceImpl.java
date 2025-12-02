package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectCheckListRepository projectChecklistRepository;
    private final CheckListRepository checkListRepository;

    @Override
    public List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request) {

        // project 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 여러 checklistId에 대해 ProjectChecklist 엔티티 생성
        List<ProjectCheckList> projectCheckLists = request.getChecklistIds().stream()
                .map(checkListId ->{
                    // 각 CheckList 조회
                    CheckList checkList = checkListRepository.findById(checkListId)
                            .orElseThrow(()-> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

                    // ProjectCheckList 엔티티 생성
                    return ProjectAddCheckListRequest.Converter.toEntity(project, checkList);
                })
                .toList();

        // 일괄 저장
        List<ProjectCheckList> savedCheckLists = projectChecklistRepository.saveAll(projectCheckLists);

        // Response 변환 후 반환
        return ProjectAddCheckListResponse.Converter.from(savedCheckLists);
    }
}
