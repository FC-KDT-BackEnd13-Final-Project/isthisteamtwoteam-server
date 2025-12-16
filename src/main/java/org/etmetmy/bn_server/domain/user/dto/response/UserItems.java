package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserItems<T> {
    private long total;
    private List<T> items;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    public static class Converter {
        // Page 객체로부터 UserItems 생성
        public static <T> UserItems<T> fromPage(Page<?> page, List<T> convertedItems) {
            UserItems<T> userItems = new UserItems<>();
            userItems.setItems(convertedItems);
            userItems.setTotal(page.getTotalElements());
            userItems.setCurrentPage(page.getNumber());
            userItems.setTotalPages(page.getTotalPages());
            userItems.setPageSize(page.getSize());
            return userItems;
        }
    }
}