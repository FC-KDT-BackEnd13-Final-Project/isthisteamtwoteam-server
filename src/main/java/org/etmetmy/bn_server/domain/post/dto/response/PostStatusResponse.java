package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostStatusResponse {

    private Long requestId;
    private Long post;
    private Long replitUserId;
    private RequestStatus requestStatus;
    private LocalDateTime replyTime;
    private String rejectReason;
}
