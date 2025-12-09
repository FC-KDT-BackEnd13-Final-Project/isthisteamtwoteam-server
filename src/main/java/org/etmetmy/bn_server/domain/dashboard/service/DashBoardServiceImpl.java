package org.etmetmy.bn_server.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardStatusResponseDTO;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DashBoardStatusResponseDTO> getStatusDashboard(Long loginUserId) {
        // STATUS_PENDING 상태인 Request를 가진 Post 목록 조회
        List<Post> pendingPosts = postRepository.findPostsWithRequestStatus(RequestStatus.STATUS_PENDING);

        // toDTOList 메서드 사용하여 DTO 변환
        return DashBoardStatusResponseDTO.Converter.toDTOList(pendingPosts);
    }
}
