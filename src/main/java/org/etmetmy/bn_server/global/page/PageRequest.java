package org.etmetmy.bn_server.global.page;

import lombok.Getter;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Offset 기반 페이지 요청 구현 클래스
 * 페이지 번호와 크기를 기반으로 데이터를 조회
 */
@Getter
public class PageRequest implements Pageable {

    private final int page;        // 0부터 시작하는 페이지 번호
    private final int size;        // 페이지당 데이터 개수
    private final Sort sort;       // 정렬 정보 (optional)

    protected PageRequest(int page, int size, Sort sort) {
        if (page < 0) {
            throw new CustomException(StatusCode.NON_INDEX_PAGE);
        }
        if (size < 1) {
            throw new CustomException(StatusCode.NON_SIZE_PAGE);
        }

        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    /**
     * 정렬 없는 페이지 요청 생성
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return PageRequest 객체
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, Sort.unsorted());
    }

    /**
     * 정렬 포함 페이지 요청 생성
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sort 정렬 정보
     * @return PageRequest 객체
     */
    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return (long) page * (long) size;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new PageRequest(page + 1, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return page == 0 ? this : new PageRequest(page - 1, size, sort);
    }

    @Override
    public Pageable first() {
        return new PageRequest(0, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new PageRequest(pageNumber, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return page > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PageRequest)) {
            return false;
        }
        PageRequest that = (PageRequest) obj;
        return this.page == that.page && this.size == that.size;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + page;
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size: %d, sort: %s]", page, size, sort);
    }
}
