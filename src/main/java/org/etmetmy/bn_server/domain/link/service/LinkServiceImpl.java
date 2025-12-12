package org.etmetmy.bn_server.domain.link.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.link.dto.LinkCreateRequest;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글에 한 번에 저장
    @Transactional
    public void saveLinks(Post post, List<String> linkUrls, Long uploadedBy) {

        if (linkUrls == null || linkUrls.isEmpty()) {
            return;
        }
        List<Link> newLinks = LinkCreateRequest.Converter.toEntity(post, linkUrls, uploadedBy);
        linkRepository.saveAll(newLinks);
    }
}
