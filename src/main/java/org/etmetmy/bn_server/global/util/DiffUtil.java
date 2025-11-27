package org.etmetmy.bn_server.global.util;

import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.LogDetail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DiffUtil {

    /**
     * 두 객체를 비교하여 변경된 필드 목록을 반환합니다.
     * @param oldObject 변경 전 객체
     * @param newObject 변경 후 객체
     * @return 변경된 내용 리스트 (List<LogDetail>)
     */
    public static <T> List<LogDetail> extractDiff(T oldObject, T newObject) {
        List<LogDetail> details = new ArrayList<>();

        // 둘 다 null이면 변경 없음
        if (oldObject == null && newObject == null) return details;

        // 클래스 정보 가져오기
        Class<?> clazz = (oldObject != null) ? oldObject.getClass() : newObject.getClass();

        // 모든 필드(변수)를 하나씩 꺼내서 비교
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // private 변수도 읽을 수 있게 허용

            try {
                // 필드 이름 (나중에 @Column(name="...") 같은 거 읽어서 한글로 바꿀 수도 있음)
                String fieldName = field.getName();

                // 값 꺼내기
                Object oldValue = (oldObject != null) ? field.get(oldObject) : null;
                Object newValue = (newObject != null) ? field.get(newObject) : null;

                // 값이 다르면 리스트에 추가 (Objects.equals는 null 안전한 비교 메서드)
                if (!Objects.equals(oldValue, newValue)) {
                    // ID나 생성시간 같은 건 보통 로그에 안 남기므로 제외 (필요시 로직 추가)
                    if(fieldName.equals("id") || fieldName.equals("createdAt") || fieldName.equals("updatedAt")) {
                        continue;
                    }

                    details.add(LogDetail.builder()
                            .field(fieldName)
                            .oldValue(String.valueOf(oldValue)) // 문자열로 변환
                            .newValue(String.valueOf(newValue))
                            .build());
                }

            } catch (IllegalAccessException e) {
                log.error("DiffUtil 필드 접근 오류: {}", field.getName(), e);
            }
        }

        return details;
    }
}