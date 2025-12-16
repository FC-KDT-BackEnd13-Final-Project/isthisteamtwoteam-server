package org.etmetmy.bn_server.domain.link.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
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

    // 1. 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글에 한 번에 저장
    @Transactional
    public void saveLinks(Post post, List<String> linkUrls, Long uploadedBy) {
        saveLinksInternal(post, null, linkUrls, uploadedBy);
    }

    // 2. 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 댓글에 한 번에 저장
    @Transactional
    public void saveLinks(Comment comment, List<String> linkUrls, Long uploadedBy) {
        saveLinksInternal(null, comment, linkUrls, uploadedBy);
    }

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글/댓글에 한 번에 저장
    private void saveLinksInternal(Post post, Comment comment, List<String> linkUrls, Long uploadedBy) {
        if (linkUrls == null || linkUrls.isEmpty()) {
            return;
        }

        List<Link> newLinks;
        if (post != null) {
            newLinks = LinkCreateRequest.Converter.toEntity(post, linkUrls, uploadedBy);
        } else {
            newLinks = LinkCreateRequest.Converter.toEntity(comment, linkUrls, uploadedBy);
        }
        linkRepository.saveAll(newLinks);
    }
}
