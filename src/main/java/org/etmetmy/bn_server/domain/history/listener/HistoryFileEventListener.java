package org.etmetmy.bn_server.domain.history.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.history.entity.HistoryFile;
import org.etmetmy.bn_server.domain.history.event.HistoryFileEvent;
import org.etmetmy.bn_server.domain.history.repository.HistoryFileRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HistoryFileEventListener {

    private final HistoryFileRepository historyFileRepository;
    private final UserRepository userRepository;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleHistoryFileEvent(HistoryFileEvent event) {
        try {
            log.info("Processing HistoryFileEvent - fileId: {}, changeType: {}",
                    event.getFile().getFileId(),
                    event.getChangeType());

            User changedByUser = userRepository.findById(event.getChangedByUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getChangedByUserId()));

            HistoryFile historyFile = createHistoryFile(
                    event.getFile(),
                    event.getChangeType(),
                    changedByUser,
                    event.getChangeIp()
            );

            historyFileRepository.save(historyFile);

            log.info("HistoryFile saved successfully - historyFileId: {}", historyFile.getHistoryFileId());

        } catch (Exception e) {
            log.error("Failed to save HistoryFile - fileId: {}, error: {}",
                    event.getFile().getFileId(),
                    e.getMessage(), e);
        }
    }

    private HistoryFile createHistoryFile(
            File file,
            org.etmetmy.bn_server.domain.history.entity.ChangeType changeType,
            User changedByUser,
            String changeIp
    ) {
        return HistoryFile.builder()
                .originalFileId(file.getFileId())
                .changeType(changeType)
                .fileName(file.getOriginalFileTitle())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .changedByUser(changedByUser)
                .changeIp(changeIp)
                .postId(file.getPost() != null ? file.getPost().getPostId() : null)
                .commentId(file.getComment() != null ? file.getComment().getCommentId() : null)
                .projectCheckListId(file.getProjectCheckList() != null ? file.getProjectCheckList().getProjectCheckListId() : null)
                .projectId(file.getProject() != null ? file.getProject().getId() : null)
                .build();
    }
}
