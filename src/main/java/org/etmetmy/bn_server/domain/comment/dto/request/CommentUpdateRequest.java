package org.etmetmy.bn_server.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;
@Getter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    // 추가할 파일 ID 목록 (임시 파일 ID, 선택 사항)
    private List<Long> addFileIds;

    // 삭제할 파일 ID 목록 (기존 파일 ID, 선택 사항)
    private List<Long> removeFileIds;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;
}
