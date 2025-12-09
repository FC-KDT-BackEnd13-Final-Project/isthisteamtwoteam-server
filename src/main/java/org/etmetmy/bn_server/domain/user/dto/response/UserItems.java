package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
//목록 데이터와 그 목록의 총 개수 처리
public class UserItems<T> {
    private final int total;
    private final List<T> items;

    public UserItems(List<T> items) {
        this.items = items;
        this.total = items.size();
    }
}