package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
public class UserItems<T> {
    private final int total;
    private final List<T> items;

    private UserItems(List<T> items) {
        this.items = items;
        this.total = items.size();
    }

    // 서비스에서 사용할 정적 팩토리 메서드
    public static <T> UserItems<T> create(List<T> items) {
        return new UserItems<>(items);
    }
}