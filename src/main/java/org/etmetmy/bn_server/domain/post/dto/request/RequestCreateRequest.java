package org.etmetmy.bn_server.domain.post.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.user.entity.User;

@Getter
@NoArgsConstructor
@Builder
public class RequestCreateRequest {

    public static class Converter{
        public static Request toEntity(Post post, User user){
            return Request.builder()
                    .post(post)
                    .responder(user)
                    .approveStatus(RequestStatus.STATUS_PENDING)
                    .build();
        }
    }
}
