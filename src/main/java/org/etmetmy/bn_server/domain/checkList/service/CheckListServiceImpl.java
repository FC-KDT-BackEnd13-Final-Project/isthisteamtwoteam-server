package org.etmetmy.bn_server.domain.checkList.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListFileLinkCreateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.request.CheckListUpdateRequest;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.link.service.LinkService;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCheckListReasonRequest;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectPermissionDeniedException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckListServiceImpl implements CheckListService {
    private final CheckListRepository checkListRepository;
    private final ProjectCheckListRepository projectCheckListRepository;
    private final UserRepository userRepository;

    private final LinkService linkService;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @Override
    public CheckListResponse save(CheckListCreateRequest request) {
        CheckList entity = CheckListCreateRequest.Converter.toEntity(request);
        CheckList saved = checkListRepository.save(entity);
        return CheckListResponse.Converter.from(saved);
    }

    @Override
    @Transactional
    public CheckListResponse update(Long checkListId, CheckListUpdateRequest request) {

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));
        CheckListUpdateRequest.Converter.updateEntity(request, checkList);

        return CheckListResponse.Converter.from(checkList);
    }

    @Override
    public void delete(Long checkListId) {
        checkListRepository.deleteById(checkListId);
    }

    // 페이지네이션 체크리스트 전체 조회
    @Override
    public Page<CheckListResponse> getCheckLists(PageRequest pageRequest) {
        Page<CheckList> checkListPage = checkListRepository.findAll(pageRequest);
        return checkListPage.map(CheckListResponse.Converter::from);
    }

    // 키워드 체크리스트 조회(페이지네이션)
    @Override
    public Page<CheckListResponse> searchCheckLists(String keyword, PageRequest pageRequest) {
        Page<CheckList> keywrodCheckListPage = checkListRepository.findByKeyword(keyword, pageRequest);
        return keywrodCheckListPage.map(CheckListResponse.Converter::from);
    }

    @Override
    public void updateChecked(Long projectId, Long checkListId, Long userId) {
        ProjectCheckList projectCheckList = projectCheckListRepository.findByProject_IdAndCheckListId(projectId, checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        User user = userRepository.findById(userId)
                        .orElseThrow((UserNotFoundException::new));

        projectCheckList.updateChecked(user);
        projectCheckListRepository.save(projectCheckList);
    }

    @Override
    public void updateContent(Long projectId, Long checkListId, ProjectCheckListReasonRequest reason) {
        ProjectCheckList projectCheckList = projectCheckListRepository.findByProject_IdAndCheckListId(projectId, checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        projectCheckList.updateReason(reason.getReason());
        projectCheckListRepository.save(projectCheckList);

    }

    @Override
    public void saveLink(Long userId, Long projectId, Long checkListId, CheckListFileLinkCreateRequest linkCreateRequest) {
        ProjectCheckList projectCheckList = projectCheckListRepository.findByProject_IdAndCheckListId(projectId, checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow((UserNotFoundException::new));

        linkService.saveLinks(projectCheckList, linkCreateRequest.getLinkUrls(), user.getId());
    }

    @Override
    public void saveFile(Long userId, Long projectId, Long checkListId, CheckListFileLinkCreateRequest fileCreateRequest) {
        ProjectCheckList projectCheckList = projectCheckListRepository.findByProject_IdAndCheckListId(projectId, checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow((UserNotFoundException::new));

        fileService.saveFiles(projectCheckList, fileCreateRequest.getFileIds(), user.getId());
    }

    @Override
    public void deletefile(Long userId, Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow((UserNotFoundException::new));

        file.softDelete(user.getId());
    }
}
