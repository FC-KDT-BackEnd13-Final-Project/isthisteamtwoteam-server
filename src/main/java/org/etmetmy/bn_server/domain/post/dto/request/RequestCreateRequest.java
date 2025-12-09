package org.etmetmy.bn_server.domain.post.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

@Getter
@NoArgsConstructor
@Builder
public class RequestCreateRequest {

    public static class Converter{
        public static Request toEntity(Post post,Long loginUserId){
            return Request.builder()
                    .post(post)
                    .requestUserId(loginUserId)
                    .approveStatus(RequestStatus.STATUS_PENDING)
                    .build();
        }
    }
}
