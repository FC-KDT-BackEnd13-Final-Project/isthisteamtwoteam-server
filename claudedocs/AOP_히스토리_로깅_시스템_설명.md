# AOP 히스토리 로깅 시스템 설명

## 개요

이 문서는 **AOP(Aspect-Oriented Programming)를 활용한 히스토리 로깅 시스템**에 대한 상세 설명입니다. 게시글의 변경 이력을 자동으로 기록하는 구조를 다룹니다.

---

## 1️⃣ @HistoryLogger 어노테이션

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryLogger {
    ChangeType changeType();  // UPDATE, DELETE
}
```

### 설명

- **`@Target(ElementType.METHOD)`**: 이 어노테이션은 **메서드에만** 붙일 수 있습니다
- **`@Retention(RetentionPolicy.RUNTIME)`**: 런타임(실행 중)에도 어노테이션 정보가 유지됩니다. AOP가 런타임에 이 어노테이션을 감지해야 하므로 필수입니다
- **`ChangeType changeType()`**: 어노테이션 사용 시 변경 타입(UPDATE/DELETE)을 지정하는 **필수 속성**입니다

### 사용 예시

```java
@HistoryLogger(changeType = ChangeType.UPDATE)  // UPDATE 타입 지정
public void updatePost(...) { }
```

---

## 2️⃣ HistoryLogAspect - AOP 핵심 로직

```java
@Aspect  // ① 이 클래스가 AOP Aspect임을 선언
@Component  // ② Spring Bean으로 등록
@RequiredArgsConstructor  // ③ final 필드 자동 생성자 주입
@Slf4j  // ④ 로깅용
public class HistoryLogAspect {
    private final ApplicationEventPublisher eventPublisher;
```

### 각 부분 설명

---

### ① Pointcut 정의

```java
@Pointcut("@annotation(org.etmetmy.bn_server.domain.history.aop.HistoryLogger)")
public void historyLoggerPointcut() {}
```

**역할**: AOP가 **어디에 적용될지** 지점을 정의합니다

- `@annotation(...)`: `@HistoryLogger` 어노테이션이 붙은 메서드를 타겟으로 지정
- 메서드 본문은 비어있고, 이름(`historyLoggerPointcut`)만 다른 곳에서 참조용으로 사용

---

### ② @Around Advice - 핵심 로직

```java
@Around("historyLoggerPointcut() && @annotation(historyLogger)")
public Object saveHistoryLog(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger)
        throws Throwable {
```

**역할**: 타겟 메서드 **실행 전후**에 로직을 삽입합니다

**파라미터 설명**:

- **`ProceedingJoinPoint joinPoint`**: 실제 실행될 메서드 정보를 담고 있습니다
  - `joinPoint.getArgs()`: 메서드에 전달된 인자들
  - `joinPoint.proceed()`: **실제 메서드 실행**
- **`HistoryLogger historyLogger`**: 메서드에 붙은 `@HistoryLogger` 어노테이션 객체
  - `historyLogger.changeType()`으로 속성값 접근 가능

---

### ③ 변경 전 데이터 조회

```java
// 1. 변경 전 데이터 조회
Long postId = extractPostId(joinPoint);  // ← 메서드 인자에서 postId 추출
Post originalPost = postRepository.findById(postId).orElseThrow();
```

**흐름**:

1. `joinPoint`에서 메서드 인자를 분석해 `postId`를 추출합니다
2. DB에서 **변경 전 원본 데이터**를 조회합니다
3. 이 데이터는 나중에 "Before" 상태로 저장됩니다

**중요**: 메서드 실행 **전**에 조회해야 변경 전 데이터를 얻을 수 있습니다!

---

### ④ 실제 메서드 실행

```java
// 2. 메서드 실행 (실제 수정/삭제 작업)
Object result = joinPoint.proceed();
```

**핵심**: 이 시점에 **원래 메서드가 실행**됩니다

- 예: `postService.updatePost(...)` 실행 → DB에 변경사항 반영
- `result`: 원래 메서드의 반환값

---

### ⑤ 변경 후 데이터 조회

```java
// 3. 변경 후 데이터 조회 (UPDATE의 경우)
Post updatedPost = null;
if (historyLogger.changeType() == ChangeType.UPDATE) {
    updatedPost = postRepository.findById(postId).orElseThrow();
}
```

**로직**:

- **UPDATE인 경우**: 변경 후 데이터를 다시 조회합니다
- **DELETE인 경우**: 이미 삭제되었으므로 조회하지 않습니다 (null)

---

### ⑥ HTTP 요청 정보 추출

```java
HttpServletRequest request = ((ServletRequestAttributes)
        RequestContextHolder.getRequestAttributes()).getRequest();
```

**역할**: 현재 HTTP 요청 객체를 가져옵니다

- **`RequestContextHolder`**: Spring이 ThreadLocal에 저장한 요청 정보에 접근
- **`ServletRequestAttributes`**: Servlet 환경의 요청 속성 래퍼

---

### ⑦ 히스토리 이벤트 발행

```java
eventPublisher.publishEvent(
    new HistoryPostEvent(
        originalPost,       // 변경 전 데이터
        updatedPost,        // 변경 후 데이터 (DELETE시 null)
        historyLogger.changeType(),  // UPDATE 또는 DELETE
        SessionUtil.getLoginUserId(request.getSession()),  // 변경한 사용자 ID
        IpAddressUtil.getClientIp(request)  // 변경 IP 주소
    )
);
```

**Spring Event 패턴**:

- **비동기 처리 가능**: 히스토리 저장을 별도 스레드에서 처리
- **결합도 감소**: Aspect와 실제 저장 로직이 분리됩니다
- `@EventListener`가 이 이벤트를 받아서 HistoryPost 엔티티를 저장합니다

---

### ⑧ 결과 반환

```java
return result;
```

원래 메서드의 반환값을 그대로 반환합니다 (Controller에서 정상적으로 응답 반환)

---

## 3️⃣ Controller에서 사용

```java
@PatchMapping("/posts/{postId}")
@ActivityLogger(targetType = "Post", action = "UPDATE")
@HistoryLogger(changeType = ChangeType.UPDATE)  // ✅ 여기만 추가!
public CommonResponse<PostCreateResponse> updatePost(
    @PathVariable Long postId,
    @Valid @RequestBody PostUpdateRequest requestDto,
    HttpSession session) {

    PostCreateResponse response = postService.updatePost(postId, requestDto, loginUserId);
    return CommonResponse.success("게시글 수정 성공", response);
}
```

### 동작 순서

1. **요청 도착**: `PATCH /posts/123`
2. **AOP 가로채기**: `@HistoryLogger` 감지 → `HistoryLogAspect.saveHistoryLog()` 실행
3. **변경 전 조회**: Post 123번 데이터 조회 (title="원본 제목")
4. **실제 메서드 실행**: `postService.updatePost()` 실행 → DB 업데이트
5. **변경 후 조회**: Post 123번 데이터 재조회 (title="수정된 제목")
6. **이벤트 발행**: `HistoryPostEvent` 발행
7. **응답 반환**: 원래 Controller 반환값 전달

---

## 🎯 핵심 장점

### 1. 관심사 분리 (Separation of Concerns)

```java
// ❌ 기존 방식 - 비즈니스 로직에 히스토리 코드 섞임
public void updatePost(...) {
    Post original = findPost();  // 히스토리 관련
    post.update();
    saveHistory(original);  // 히스토리 관련
}

// ✅ AOP 방식 - 비즈니스 로직만 집중
@HistoryLogger(changeType = UPDATE)
public void updatePost(...) {
    post.update();  // 핵심 로직만!
}
```

### 2. 재사용성

```java
@HistoryLogger(changeType = UPDATE)
public void updatePost(...) { }

@HistoryLogger(changeType = DELETE)
public void deletePost(...) { }

// ✅ 모든 메서드에 같은 로직 자동 적용!
```

### 3. 유지보수성

- 히스토리 로직 변경 시 Aspect만 수정하면 됨
- 각 Controller는 수정 불필요

---

## ⚠️ 주의사항

### 1. extractPostId() 구현 필요

```java
private Long extractPostId(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    // @PathVariable Long postId를 찾아서 반환
    for (Object arg : args) {
        if (arg instanceof Long) return (Long) arg;
    }
    throw new IllegalArgumentException("postId를 찾을 수 없습니다");
}
```

### 2. 트랜잭션 고려

```java
@Transactional  // ← 이게 없으면 변경 후 조회 시 캐시된 데이터 조회 가능
public void updatePost(...) { }
```

### 3. 이벤트 리스너 구현 필요

```java
@Component
public class HistoryPostEventListener {
    @EventListener
    public void handleHistoryPostEvent(HistoryPostEvent event) {
        // HistoryPost 엔티티 생성 및 저장
    }
}
```

---

## 📊 전체 흐름도

```
1. HTTP 요청
   ↓
2. @HistoryLogger 감지 (AOP)
   ↓
3. 변경 전 데이터 조회 (DB)
   ↓
4. 실제 메서드 실행 (비즈니스 로직)
   ↓
5. 변경 후 데이터 조회 (DB)
   ↓
6. HistoryPostEvent 발행
   ↓
7. EventListener가 이벤트 수신
   ↓
8. HistoryPost 엔티티 저장 (DB)
   ↓
9. HTTP 응답 반환
```

---

## 🔧 구현 체크리스트

- [ ] `@HistoryLogger` 어노테이션 생성
- [ ] `ChangeType` enum 생성 (UPDATE, DELETE)
- [ ] `HistoryLogAspect` 클래스 생성
- [ ] `extractPostId()` 메서드 구현
- [ ] `HistoryPostEvent` 클래스 생성
- [ ] `HistoryPostEventListener` 구현
- [ ] `HistoryPost` 엔티티 생성
- [ ] `HistoryPostRepository` 생성
- [ ] Controller 메서드에 `@HistoryLogger` 적용
- [ ] 테스트 코드 작성

---

## 📝 참고사항

### AOP 동작 원리

Spring AOP는 **프록시 패턴**을 사용합니다:

1. Spring이 빈을 생성할 때 `@Aspect`가 있는 클래스를 찾습니다
2. Pointcut 조건에 맞는 빈의 프록시 객체를 생성합니다
3. 메서드 호출 시 프록시가 먼저 실행되어 Advice 로직을 수행합니다
4. 원본 메서드를 실행하고 결과를 반환합니다

### Event 패턴의 장점

- **비동기 처리**: `@Async` 추가로 성능 향상 가능
- **트랜잭션 분리**: 히스토리 저장 실패가 원본 트랜잭션에 영향 없음
- **확장성**: 여러 리스너가 같은 이벤트를 처리 가능

---

## 🚀 다음 단계

1. 실제 코드로 구현하기
2. 단위 테스트 작성
3. 통합 테스트로 전체 흐름 검증
4. 성능 테스트 (히스토리 저장이 응답 시간에 미치는 영향)
5. 프로덕션 배포 및 모니터링