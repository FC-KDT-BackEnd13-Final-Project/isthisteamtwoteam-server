package org.etmetmy.bn_server.global.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 깔끔한 페이지네이션 응답 DTO
 * Spring Data Page의 불필요한 필드를 제거
 *
 * @param <T> 데이터 타입
 */
@Getter
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 실제 데이터 리스트
     */
    private List<T> content;

    /**
     * 페이지 메타 정보
     */
    private PageInfo pageInfo;

    /**
     * 페이지 메타 정보 클래스
     */
    @Getter
    @AllArgsConstructor
    public static class PageInfo {
        private int page;              // 현재 페이지 (0부터 시작)
        private int size;              // 페이지 크기
        private int totalElements;     // 전체 데이터 개수
        private int totalPages;        // 전체 페이지 수
        private boolean first;         // 첫 페이지 여부
        private boolean last;          // 마지막 페이지 여부
        private boolean hasNext;       // 다음 페이지 있음
        private boolean hasPrevious;   // 이전 페이지 있음
    }

    /**
     * Spring Data Page를 PageResponse로 변환
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageInfo pageInfo = new PageInfo(
                page.getNumber(),
                page.getSize(),
                (int) page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PageResponse<>(page.getContent(), pageInfo);
    }
}
