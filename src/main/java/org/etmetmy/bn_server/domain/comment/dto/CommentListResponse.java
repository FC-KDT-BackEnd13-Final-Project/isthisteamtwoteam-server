package org.etmetmy.bn_server.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.global.CommonResponse;

import java.util.List;

@Getter
@Builder
public class CommentListResponse {
    private final Long postId;
    private final int totalCount;
    private final List<CommentResponse> comments;
}