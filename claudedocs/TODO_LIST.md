# 프로젝트 개선 및 구현 TODO 리스트

> **분석일자**: 2025-12-13
> **프로젝트**: isthisteamtwoteam-server
> **기술 스택**: Spring Boot 3.5.7, JPA, PostgreSQL, AWS S3

---

## 🔴 긴급 (Critical) - 즉시 수정 필요

### 1. 보안 설정 복구 - SecurityConfig
**파일**: `src/main/java/org/etmetmy/bn_server/config/SecurityConfig.java` (20-22줄)
**심각도**: 🔴 CRITICAL

**문제점**:
```java
.csrf(csrf -> csrf.disable())  // CSRF 완전 비활성화
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // 모든 요청 허용
)
```

**영향**:
- 모든 API 엔드포인트가 인증 없이 접근 가능
- CSRF 보호 비활성화
- 누구나 생성/삭제/수정 가능

**해결방안**:
- [ ] 역할 기반 접근 제어(RBAC) 구현
- [ ] 상태 변경 작업에 CSRF 보호 활성화
- [ ] 공개 엔드포인트와 보호된 엔드포인트 분리
- [ ] JWT 또는 세션 기반 인증 활성화

---

### 2. 인증/인가 인터셉터 활성화 - WebConfig
**파일**: `src/main/java/org/etmetmy/bn_server/config/WebConfig.java` (13-31줄)
**심각도**: 🔴 CRITICAL

**문제점**:
```java
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new UserAuthorizationInterceptor())...
//        registry.addInterceptor(new RoleAuthorizationInterceptor())...
//    }
```

**영향**:
- 인증/인가 체크가 전혀 수행되지 않음
- `/api/v1/admin/**` 엔드포인트에 일반 사용자 접근 가능
- 역할 기반 접근 제어 무력화

**해결방안**:
- [ ] 주석 해제하여 인터셉터 활성화
- [ ] 인터셉터 동작 테스트
- [ ] 예외 경로 재검토

---

### 3. 하드코딩된 리다이렉트 URL
**파일**: `src/main/java/org/etmetmy/bn_server/web/UserAuthorizationInterceptor.java` (20줄)
**심각도**: 🔴 CRITICAL

**문제점**:
```java
response.sendRedirect("http://localhost:5173/home");
```

**영향**:
- 프로덕션 환경에서 작동하지 않음
- 개발 환경 정보 노출

**해결방안**:
- [ ] application.yml에 설정 속성 추가
- [ ] 환경별 다른 URL 사용 (dev/prod)
- [ ] Origin 기반 동적 리다이렉트 구현

---

### 4. JPA DDL Auto 설정 변경
**파일**: `src/main/resources/application.yml` (15줄)
**심각도**: 🔴 CRITICAL

**문제점**:
```yaml
hibernate:
  ddl-auto: update  # 프로덕션에서 위험
```

**영향**:
- 프로덕션에서 자동 스키마 변경 가능
- 데이터 손실 위험

**해결방안**:
- [ ] 프로덕션 프로파일에서 "validate"로 변경
- [ ] 개발 환경은 "update" 유지
- [ ] 마이그레이션 도구 도입 고려 (Flyway, Liquibase)

---

## 🟡 높음 (High) - 빠른 시일 내 수정

### 5. 비밀번호 변경 DTO 검증 누락
**파일**: `src/main/java/org/etmetmy/bn_server/domain/user/dto/request/UserChangePasswordRequest.java`
**심각도**: 🟡 HIGH

**문제점**:
```java
public class UserChangePasswordRequest {
    private String currentPassword;  // 검증 없음
    private String newPassword;       // 검증 없음
    private String confirmPassword;   // 검증 없음
}
```

**해결방안**:
- [ ] `@NotBlank` 추가
- [ ] 비밀번호 최소 길이 검증
- [ ] `newPassword`와 `confirmPassword` 일치 확인 로직
- [ ] 비밀번호 강도 검증 (선택적)

---

### 6. 로그인 DTO 검증 누락
**파일**: `src/main/java/org/etmetmy/bn_server/domain/user/dto/request/UserLoginDto.java`
**심각도**: 🟡 HIGH

**문제점**:
```java
public class UserLoginDto {
    private String email;     // @Email, @NotBlank 없음
    private String password;  // @NotBlank 없음
}
```

**해결방안**:
- [ ] `@Email` 추가
- [ ] `@NotBlank` 추가
- [ ] 이메일 형식 검증

---

### 7. Comment 생성 요청 타입 오류
**파일**: `src/main/java/org/etmetmy/bn_server/domain/comment/dto/request/CommentCreateRequest.java` (18줄)
**심각도**: 🟡 HIGH

**문제점**:
```java
@NotNull(message = "작성자 ID는 필수입니다.")
private User userId;  // ❌ 타입이 User이지만 필드명은 userId
```

**영향**:
- 프론트엔드에서 ID를 보내면 역직렬화 실패
- ClassCastException 발생 가능

**해결방안**:
- [ ] `Long userId`로 타입 변경
- [ ] 세션/인증에서 User 객체 주입
- [ ] 또는 `User user`로 필드명 변경

---

### 8. 파일 업로드 보안 체크 누락
**파일**: `src/main/java/org/etmetmy/bn_server/domain/file/service/FileServiceImpl.java`
**심각도**: 🟡 HIGH

**문제점**:
- 파일 타입 검증 없음 (MIME 타입)
- 파일 크기 제한 없음
- 바이러스 스캔 없음
- 업로드 속도 제한 없음

**해결방안**:
- [ ] 허용된 MIME 타입 화이트리스트
- [ ] 최대 파일 크기 제한 (application.yml)
- [ ] 파일명 sanitization
- [ ] 업로드 속도 제한 (선택적)

---

### 9. 오타 수정 - RoleAuthorizationInterceptor
**파일**: `src/main/java/org/etmetmy/bn_server/web/RoleAuthorizationInterceptor.java` (39줄)
**심각도**: 🟡 HIGH

**문제점**:
```java
sendForbiddenResponse(response, "접근 권한이 없습니w다.");  // "w" 오타
```

**해결방안**:
- [ ] "접근 권한이 없습니다."로 수정

---

## 🟠 중간 (Medium) - 점진적 개선

### 10. Post 업데이트 엔드포인트 활성화
**파일**: `src/main/java/org/etmetmy/bn_server/domain/post/controller/PostController.java` (113-126줄)
**심각도**: 🟠 MEDIUM

**문제점**:
- CRUD 중 Update가 주석 처리됨
- 게시글 수정 기능 없음

**해결방안**:
- [ ] 주석 해제 및 검토
- [ ] 업데이트 로직 완성
- [ ] 테스트 작성

---

### 11. 날짜 범위 검증 추가
**파일**: `src/main/java/org/etmetmy/bn_server/domain/project/dto/request/ProjectCreateRequest.java`
**심각도**: 🟠 MEDIUM

**문제점**:
```java
private LocalDate startDate;
private LocalDate endDate;
// endDate > startDate 검증 없음
```

**해결방안**:
- [ ] 커스텀 검증 어노테이션 생성
- [ ] 서비스 레이어에서 검증
- [ ] 비즈니스 로직 예외 처리

---

### 12. API 응답 일관성 개선
**파일**: 여러 Controller 파일
**심각도**: 🟠 MEDIUM

**문제점**:
```java
// 각기 다른 응답 타입
public void createProject(...)  // void 반환
public ResponseEntity<CommonResponse<Object>> approvePost(...)
public CommonResponse<PostCreateResponse> updatePost(...)
```

**해결방안**:
- [ ] 모든 엔드포인트를 `CommonResponse<T>` 또는 `ResponseEntity<CommonResponse<T>>`로 통일
- [ ] 적절한 HTTP 상태 코드 반환
  - 생성: 201 Created
  - 삭제: 204 No Content
  - 성공: 200 OK
- [ ] void 반환 제거

---

### 13. Comment 엔티티 컬럼명 개선
**파일**: `src/main/java/org/etmetmy/bn_server/domain/comment/entity/Comment.java` (45-46줄)
**심각도**: 🟠 MEDIUM

**문제점**:
```java
@Column(name = "comment_id")
private Long commentId;

@Column(name = "comment_id2")  // ❌ 혼란스러운 명칭
private Long commentId2;  // 실제로는 부모 댓글 ID
```

**해결방안**:
- [ ] `commentId2`를 `parentCommentId`로 변경
- [ ] 컬럼명도 `parent_comment_id`로 변경
- [ ] 마이그레이션 스크립트 작성

---

### 14. 페이지네이션 구현
**파일**: 모든 List 조회 엔드포인트
**심각도**: 🟠 MEDIUM

**문제점**:
- 모든 목록 조회가 전체 데이터 반환
- `PageRequest`, `PageResponse` 클래스는 있으나 미사용
- 대용량 데이터 시 성능 문제

**해결방안**:
- [ ] `Pageable` 파라미터 추가
- [ ] `Page<T>` 반환 타입으로 변경
- [ ] 프론트엔드와 페이지네이션 스펙 협의
- [ ] 기본 페이지 크기 설정 (예: 20)

---

### 15. 데이터베이스 인덱스 추가
**파일**: Entity 클래스들
**심각도**: 🟠 MEDIUM

**문제점**:
- 자주 조회되는 컬럼에 인덱스 없음
- 예: `projectId`, `userId`, `postId` 등

**영향**:
- 데이터 증가 시 쿼리 성능 저하

**해결방안**:
```java
@Table(name = "posts", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_stage_id", columnList = "stage_id")
})
```
- [ ] Post: projectId, stageId
- [ ] Comment: postId, userId
- [ ] File: postId
- [ ] Request: postId, approveStatus

---

### 16. TODO 주석 정리
**파일**: 여러 파일 (41개 발견)
**심각도**: 🟠 MEDIUM

**예시**:
```java
// todo: 게시글 수정 브랜치 다시 파서 지울 예정
// TODO: 파일 업데이트 로직은 별도 API로 구현 필요
// todo: 부모 게시글, 자식 게시글 작성 API
```

**해결방안**:
- [ ] 완료된 TODO 제거
- [ ] 실제 작업이 필요한 TODO를 이슈 트래커로 이동
- [ ] 남은 TODO에 담당자/우선순위 표시

---

### 17. 디버그 코드 제거
**파일**: `src/main/java/org/etmetmy/bn_server/domain/memo/service/MemoServiceImpl.java`
**심각도**: 🟠 MEDIUM

**문제점**:
```java
System.out.println("실행시작");  // 64줄
log.info("메모 조회 전");  // 69줄
log.info("업데이트 전");  // 74줄
log.info("업데이트 후");  // 76줄
```

**해결방안**:
- [ ] `System.out.println` 제거
- [ ] 불필요한 info 로그 제거 또는 debug 레벨로 변경
- [ ] 의미 있는 로그만 유지

---

### 18. 세션 속성 타입 안전성 개선
**파일**: `src/main/java/org/etmetmy/bn_server/domain/project/controller/ProjectAdminController.java` (66줄)
**심각도**: 🟠 MEDIUM

**문제점**:
```java
Long currentUserId = (Long) session.getAttribute("userId");  // 안전하지 않은 캐스팅
```

**해결방안**:
- [ ] 세션 키 일관성 확보 (`SessionConst` 사용)
- [ ] 타입 체크 후 캐스팅
- [ ] 세션 유틸리티 클래스 생성

---

### 19. 불필요한 Null 체크 제거
**파일**: `src/main/java/org/etmetmy/bn_server/domain/memo/service/MemoServiceImpl.java` (39-41줄)
**심각도**: 🟠 MEDIUM

**문제점**:
```java
Memo memo = memoRepository.findById(...)
    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

if (memo == null) {  // ❌ 도달 불가능한 코드
    return null;
}
```

**해결방안**:
- [ ] `.orElseThrow()` 이후 null 체크 제거
- [ ] 불필요한 방어 코드 정리

---

### 20. 중복 요청 방지 로직 추가
**파일**: `src/main/java/org/etmetmy/bn_server/domain/post/service/PostServiceImpl.java`
**심각도**: 🟠 MEDIUM

**문제점**:
- 같은 게시글에 대한 중복 요청 방지 로직 없음
- `PENDING` 상태만 체크

**해결방안**:
- [ ] 동일 사용자의 동일 게시글에 대한 중복 요청 체크
- [ ] 비즈니스 예외 추가
- [ ] 유니크 제약 조건 검토

---

## 🟢 낮음 (Low) - 개선 권장

### 21. 미사용 Import 제거
**파일**: 여러 Service 파일
**심각도**: 🟢 LOW

**예시**:
```java
//todo: 게시글 수정 브랜치 다시 파서 지울 예정
// LinkRepository, FileRepository import는 미사용
```

**해결방안**:
- [ ] IDE 기능으로 미사용 import 정리
- [ ] 코드 포맷팅 실행

---

### 22. Swagger/OpenAPI 문서화
**파일**: 모든 Controller
**심각도**: 🟢 LOW

**문제점**:
- Swagger 설정은 있으나 어노테이션 없음
- API 문서 자동 생성 안 됨

**해결방안**:
```java
@Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다")
@ApiResponse(responseCode = "201", description = "게시글 생성 성공")
public CommonResponse<PostCreateResponse> createPost(...)
```
- [ ] `@Operation`, `@ApiResponse` 추가
- [ ] DTO에 `@Schema` 추가
- [ ] Swagger UI 접근 확인

---

### 23. 테스트 커버리지 향상
**심각도**: 🟢 LOW

**현황**:
- 150개 소스 파일 대비 11개 테스트 파일
- 비즈니스 로직 테스트 부족
- 통합 테스트 부족

**해결방안**:
- [ ] Service 레이어 단위 테스트
- [ ] Controller 통합 테스트
- [ ] 예외 처리 테스트
- [ ] 엣지 케이스 테스트
- [ ] 인증/인가 테스트

---

### 24. 복잡한 JPQL 쿼리 문서화
**파일**: Repository 인터페이스
**심각도**: 🟢 LOW

**해결방안**:
- [ ] 복잡한 쿼리에 주석 추가
- [ ] 쿼리 의도 설명
- [ ] 성능 고려사항 명시

---

### 25. 환경별 로깅 레벨 설정
**파일**: `application.yml`
**심각도**: 🟢 LOW

**해결방안**:
```yaml
logging:
  level:
    root: INFO
    org.etmetmy.bn_server: DEBUG  # 개발 환경
    org.hibernate.SQL: DEBUG      # 개발 환경
---
spring:
  config:
    activate:
      on-profile: prod
logging:
  level:
    root: WARN
    org.etmetmy.bn_server: INFO
```

---

## 📋 누락된 기능 (Missing Features)

### 26. Comment 삭제 엔드포인트
- [ ] `DELETE /api/v1/projects/{projectId}/posts/{postId}/comments/{commentId}` 구현
- [ ] 작성자 본인만 삭제 가능하도록 권한 체크
- [ ] 자식 댓글이 있을 경우 처리 방안 결정 (삭제 방지 or 연쇄 삭제)

### 27. Memo 수정 엔드포인트
- [ ] `PATCH /api/v1/projects/{projectId}/memos` 구현
- [ ] 내용 수정 기능
- [ ] 버전 관리 고려

### 28. 파일 수정 기능
- [ ] 파일 교체 API
- [ ] 기존 파일 삭제 후 새 파일 업로드
- [ ] S3 리소스 정리

### 29. 감사 로그 (Audit Trail)
- [ ] 중요 작업에 대한 변경 이력 추적
- [ ] 누가, 언제, 무엇을 변경했는지 기록
- [ ] `@CreatedBy`, `@LastModifiedBy` 활용

### 30. 에러 응답 표준화
- [ ] 모든 예외에 대한 일관된 에러 응답 형식
- [ ] 에러 코드, 메시지, 타임스탬프 포함
- [ ] GlobalExceptionHandler 확장

---

## 🎯 우선순위별 실행 계획

### Phase 1: 긴급 보안 (1-2일)
1. ✅ 인터셉터 활성화 (WebConfig)
2. ✅ SecurityConfig 수정
3. ✅ 하드코딩된 URL 제거
4. ✅ JPA DDL 설정 변경

### Phase 2: 검증 및 타입 안전성 (2-3일)
5. ✅ 모든 DTO에 검증 추가
6. ✅ CommentCreateRequest 타입 수정
7. ✅ 파일 업로드 보안 체크
8. ✅ 세션 타입 안전성 개선

### Phase 3: API 일관성 (3-5일)
9. ✅ 응답 타입 통일
10. ✅ HTTP 상태 코드 표준화
11. ✅ Post 업데이트 엔드포인트 활성화
12. ✅ 누락된 CRUD 엔드포인트 구현

### Phase 4: 성능 및 확장성 (1주)
13. ✅ 페이지네이션 구현
14. ✅ 데이터베이스 인덱스 추가
15. ✅ N+1 쿼리 문제 해결
16. ✅ 중복 요청 방지 로직

### Phase 5: 코드 품질 (진행 중)
17. ✅ TODO 주석 정리
18. ✅ 디버그 코드 제거
19. ✅ 불필요한 코드 제거
20. ✅ 오타 수정

### Phase 6: 문서화 및 테스트 (지속적)
21. ✅ Swagger 문서화
22. ✅ 테스트 커버리지 향상
23. ✅ README 업데이트
24. ✅ API 문서 작성

---

## 📊 개선 효과 예상

| 개선 영역 | 현재 상태 | 목표 상태 | 예상 효과 |
|----------|----------|----------|----------|
| **보안** | 인증/인가 미적용 | RBAC 적용 | 취약점 제거 |
| **검증** | DTO 검증 30% | DTO 검증 100% | 잘못된 데이터 유입 방지 |
| **API 일관성** | 60% | 95% | 프론트엔드 개발 효율 향상 |
| **성능** | 인덱스 없음 | 핵심 컬럼 인덱스 | 쿼리 속도 10-100배 향상 |
| **테스트** | 커버리지 ~10% | 커버리지 60%+ | 버그 조기 발견 |
| **문서화** | Swagger 미활용 | 전체 API 문서화 | 협업 효율 향상 |

---

## ✅ 빠른 수정 체크리스트 (Quick Wins)

긴급하게 수정 가능한 항목들 (1-2시간 내):

- [ ] WebConfig 인터셉터 주석 해제
- [ ] RoleAuthorizationInterceptor 오타 수정
- [ ] System.out.println 제거
- [ ] 미사용 import 정리
- [ ] UserLoginDto에 `@Email`, `@NotBlank` 추가
- [ ] UserChangePasswordRequest에 검증 추가
- [ ] CommentCreateRequest userId 타입 수정
- [ ] application.yml에 JPA ddl-auto 프로파일별 설정

---

**참고**: 이 리스트는 전체 코드베이스를 분석한 결과이며, 실제 우선순위는 프로젝트 일정과 비즈니스 요구사항에 따라 조정될 수 있습니다.
