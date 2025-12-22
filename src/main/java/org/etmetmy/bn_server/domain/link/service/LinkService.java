package org.etmetmy.bn_server.domain.link.service;

import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;

public interface LinkService {

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 게시글에 한 번에 저장
    public void saveLinks(Post post, List<String> linkUrls, Long uploadedBy);

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 댓글에 한 번에 저장
    public void saveLinks(Comment comment, List<String> linkUrls, Long uploadedBy);

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 프로젝트 체크리스트에 저장
    public void saveLinks(ProjectCheckList projectCheckList, List<String> linkUrls, Long uploadedBy);

    // 링크 URL 리스트를 받아서 Link 엔티티로 변환하고 request에 저장
    public void saveLinks(Request request, List<String> linkUrls, Long uploadedBy);
}
