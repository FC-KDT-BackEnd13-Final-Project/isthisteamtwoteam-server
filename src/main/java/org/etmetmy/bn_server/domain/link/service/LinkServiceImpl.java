package org.etmetmy.bn_server.domain.link.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.event.HistoryLinkEvent;
import org.etmetmy.bn_server.domain.link.dto.LinkCreateRequest;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 1. 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글에 한 번에 저장
    @Transactional
    public void saveLinks(Post post, List<String> linkUrls, Long uploadedBy) {
        saveLinksInternal(post, null, null, linkUrls, uploadedBy);
    }

    // 2. 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 댓글에 한 번에 저장
    @Transactional
    public void saveLinks(Comment comment, List<String> linkUrls, Long uploadedBy) {
        saveLinksInternal(null, comment, null, linkUrls, uploadedBy);
    }

    @Override
    public void saveLinks(ProjectCheckList projectCheckList, List<String> linkUrls, Long uploadedBy) {
        saveLinksInternal(null, null, projectCheckList, linkUrls, uploadedBy);
    }

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글/댓글에 한 번에 저장
    private void saveLinksInternal(Post post, Comment comment, ProjectCheckList projectCheckList, List<String> linkUrls, Long uploadedBy) {
        if (linkUrls == null || linkUrls.isEmpty()) {
            return;
        }

        // IP 주소 가져오기
        String clientIp = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = IpAddressUtil.getClientIp(request);
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 (비동기 등) null로 저장
        }

        List<Link> newLinks;
        if (comment == null && projectCheckList == null) {
            newLinks = LinkCreateRequest.Converter.toEntity(post, linkUrls, uploadedBy);
        } else if(post == null && projectCheckList == null) {
            newLinks = LinkCreateRequest.Converter.toEntity(comment, linkUrls, uploadedBy);
        }else{
            newLinks = LinkCreateRequest.Converter.toEntity(projectCheckList, linkUrls, uploadedBy);
        }
        List<Link> savedLinks = linkRepository.saveAll(newLinks);

        // 각 링크에 대해 히스토리 이벤트 발행 (CREATE)
        for (Link link : savedLinks) {
            eventPublisher.publishEvent(
                    new HistoryLinkEvent(link, ChangeType.CREATE, uploadedBy, clientIp)
            );
        }
    }
}
