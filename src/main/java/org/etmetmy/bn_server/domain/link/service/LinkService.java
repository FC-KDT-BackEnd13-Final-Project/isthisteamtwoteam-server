package org.etmetmy.bn_server.domain.link.service;

import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;

public interface LinkService {

    // 링크 저장
    public void saveLinks(Post post, List<String> linkUrls, Long uploadedBy);
}
