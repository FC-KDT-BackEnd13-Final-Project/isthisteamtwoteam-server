# 프로젝트 기반 이력서 작성 가이드

## 프로젝트 개요

### 프로젝트명
**B&N - 개발사-고객사 협업 프로젝트 관리 플랫폼**

### 한 줄 소개
> 개발사와 고객사 간의 프로젝트 진행 상황을 실시간으로 공유하고, 요청사항 승인/거절 프로세스를 통해 효율적인 협업을 지원하는 웹 기반 프로젝트 관리 시스템

### 프로젝트 기간
- 기간: [프로젝트 기간 입력]
- 팀 구성: 백엔드 [N]명, 프론트엔드 [N]명

### GitHub
- [GitHub 링크 입력]

---

## 기술 스택

### Backend
| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| ORM | Spring Data JPA, QueryDSL 5.0 |
| Security | Spring Security, BCrypt |
| Database | PostgreSQL 15 |
| Cloud | AWS S3, AWS RDS |
| API 문서화 | Springdoc OpenAPI (Swagger) |
| Build Tool | Gradle |
| 기타 | Lombok, Spring AOP |

---

## 이력서용 프로젝트 설명 (복사용)

### 버전 1: 간략 버전 (2~3줄)
```
Spring Boot 3 기반의 개발사-고객사 협업 프로젝트 관리 플랫폼 백엔드 개발
- AWS S3/RDS 연동, QueryDSL 동적 쿼리, AOP 기반 활동 로그 시스템 구현
- 역할 기반 접근 제어(RBAC) 및 승인 요청 워크플로우 설계
```

### 버전 2: 상세 버전 (기술 면접용)
```
개발사와 고객사 간 프로젝트 협업을 위한 웹 기반 관리 플랫폼의 백엔드 API 서버 개발

[담당 업무]
- Spring Boot 3.5 + Java 21 기반 RESTful API 설계 및 구현
- JPA + QueryDSL을 활용한 복잡한 조건 검색 및 페이지네이션 구현
- Spring AOP를 활용한 활동 로그 자동화 시스템 설계
- AWS S3 파일 업로드/관리 및 RDS(PostgreSQL) 연동
- 역할 기반 접근 제어(RBAC) 및 세션 인증 구현
- Soft Delete 패턴을 적용한 데이터 복구 기능 구현
- 변경 이력 추적 시스템으로 데이터 무결성 보장
```

---

## 핵심 기능 및 기술적 성과 (면접 대비)

### 1. AOP 기반 활동 로그 시스템
**문제 상황**
- 사용자 활동 추적을 위해 모든 서비스 메서드에 로깅 코드 삽입 필요
- 비즈니스 로직과 로깅 로직의 결합도 증가

**해결 방안**
- `@ActivityLogger` 커스텀 어노테이션 + Spring AOP 적용
- `@Around` 어드바이스로 메서드 실행 전후 데이터 캡처
- ApplicationEventPublisher를 통한 비동기 이벤트 처리

**성과**
- 비즈니스 로직과 로깅 로직 완전 분리 (관심사 분리)
- 어노테이션 하나로 자동 로깅 적용 가능
- 비동기 처리로 API 응답 속도에 영향 없음

```java
// 적용 예시
@ActivityLogger(action = ActionType.CREATE, targetType = "POST")
public PostResponseDTO createPost(PostRequestDTO request) { ... }
```

---

### 2. Soft Delete 패턴 및 데이터 복구 시스템
**문제 상황**
- 실수로 삭제된 데이터 복구 요청 빈번
- 물리적 삭제 시 관련 데이터 무결성 문제 발생

**해결 방안**
- `isDeleted`, `deletedAt`, `deletedBy` 필드로 논리적 삭제 구현
- `restore()` 메서드로 삭제 데이터 복구 기능 제공
- JPA `@Where` 어노테이션으로 자동 필터링

**성과**
- 삭제된 데이터 100% 복구 가능
- 삭제자 및 삭제 시점 추적으로 감사(Audit) 기능 강화

---

### 3. 변경 이력 추적 시스템 (History)
**문제 상황**
- 데이터 변경 시 이전 상태 확인 불가
- 누가, 언제, 무엇을 변경했는지 추적 필요

**해결 방안**
- HistoryPost, HistoryComment 등 이력 전용 엔티티 설계
- Before/After 데이터를 JSON으로 저장
- AOP와 연동하여 자동 이력 생성

**성과**
- 모든 변경 사항에 대한 완전한 감사 추적(Audit Trail) 구현
- 특정 시점으로의 데이터 롤백 근거 확보

---

### 4. 역할 기반 접근 제어 (RBAC)
**구현 내용**
- 3단계 역할 체계: ADMIN(3) > DEVELOPER(2) > CUSTOMER(1)
- 역할별 레벨 비교를 통한 권한 검증
- 회사(Company) 타입과 연계된 사용자 권한 관리

```java
public enum Role {
    ADMIN(3), DEVELOPER(2), CUSTOMER(1);

    public boolean hasHigherOrEqualLevel(Role other) {
        return this.level >= other.level;
    }
}
```

---

### 5. QueryDSL을 활용한 동적 쿼리
**문제 상황**
- 다양한 검색 조건에 따른 쿼리 분기 처리 복잡
- 문자열 기반 JPQL의 타입 안정성 부재

**해결 방안**
- QueryDSL을 도입하여 타입 안전한 동적 쿼리 작성
- BooleanBuilder를 활용한 조건부 쿼리 구성
- 페이지네이션과 정렬 로직 통합

**성과**
- 컴파일 타임에 쿼리 오류 검출 가능
- 복잡한 검색 조건도 가독성 높은 코드로 구현

---

### 6. AWS S3 파일 관리 시스템
**구현 내용**
- AWS SDK v2를 사용한 S3 연동
- 임시 파일(isTemp) 개념으로 미사용 파일 관리
- UUID 기반 파일명으로 중복 및 보안 문제 해결
- 다중 엔티티(Post, Comment, CheckList) 연결 지원

---

### 7. JPA Auditing을 활용한 자동 감사
**구현 내용**
- BaseEntity 추상 클래스로 공통 감사 필드 관리
- `@CreatedDate`, `@LastModifiedDate` 자동 설정
- `@PrePersist`, `@PreUpdate` 훅으로 사용자 정보 및 IP 기록

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long createdBy;
    private String userIp;
}
```

---

### 8. 승인 요청 워크플로우 (Request)
**구현 내용**
- 게시글 상태 변경에 대한 승인/거절 프로세스
- 요청자(requester)와 응답자(responder) 분리
- 거절 사유(rejectReason) 기록으로 커뮤니케이션 이력 관리
- RequestStatus Enum: PENDING → APPROVED/REJECTED

---

## 기술적 도전 및 해결 과정 (STAR 기법)

### 사례 1: 활동 로그의 비동기 처리

| 항목 | 내용 |
|------|------|
| **Situation** | 활동 로그 저장이 동기적으로 처리되어 API 응답 시간 증가 |
| **Task** | 로깅으로 인한 성능 저하 없이 모든 활동 기록 필요 |
| **Action** | Spring ApplicationEvent + @Async를 활용한 비동기 이벤트 처리 구현 |
| **Result** | API 응답 시간에 영향 없이 완전한 활동 로그 수집 달성 |

### 사례 2: 복잡한 엔티티 관계 설계

| 항목 | 내용 |
|------|------|
| **Situation** | 파일/링크가 여러 엔티티(Post, Comment, CheckList)에 연결 필요 |
| **Task** | 유연하면서도 데이터 무결성을 보장하는 관계 설계 |
| **Action** | Nullable FK를 활용한 다중 연결 + Soft Delete로 관계 데이터 보존 |
| **Result** | 하나의 File/Link 엔티티로 모든 연결 케이스 처리 가능 |

---

## 프로젝트 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Web)                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot API Server                   │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Controller│→│ Service  │→│Repository│→│  Entity  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│         │              │                                    │
│         ▼              ▼                                    │
│  ┌──────────┐  ┌──────────────┐                            │
│  │   AOP    │  │ Event System │                            │
│  │ Logging  │  │   (Async)    │                            │
│  └──────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────┘
          │                              │
          ▼                              ▼
┌──────────────────┐          ┌──────────────────┐
│   AWS RDS        │          │    AWS S3        │
│  (PostgreSQL)    │          │  (File Storage)  │
└──────────────────┘          └──────────────────┘
```

---

## 면접 예상 질문 및 답변

### Q1. 왜 Spring Boot 3와 Java 21을 선택했나요?
> Java 21은 최신 LTS 버전으로 Virtual Thread, Pattern Matching 등 최신 기능을 활용할 수 있습니다. Spring Boot 3는 Jakarta EE 표준을 따르며, 보안 업데이트와 성능 개선이 지속적으로 이루어지고 있어 선택했습니다.

### Q2. QueryDSL을 사용한 이유는?
> JPQL의 문자열 기반 쿼리는 런타임에만 오류를 발견할 수 있습니다. QueryDSL은 컴파일 타임에 쿼리 오류를 검출하고, 동적 쿼리 작성 시 가독성 높은 코드를 유지할 수 있어 선택했습니다.

### Q3. AOP로 로깅을 구현한 이유는?
> 비즈니스 로직에 로깅 코드가 섞이면 코드의 가독성이 떨어지고 유지보수가 어려워집니다. AOP를 통해 관심사를 분리하고, 어노테이션 하나로 일관된 로깅을 적용할 수 있도록 설계했습니다.

### Q4. Soft Delete를 적용한 이유는?
> 물리적 삭제는 복구가 불가능하고, 외래 키 관계에 있는 데이터의 무결성 문제가 발생할 수 있습니다. Soft Delete로 데이터 복구 가능성을 확보하고, 삭제 이력을 추적할 수 있도록 했습니다.

### Q5. 역할(Role) 시스템은 어떻게 설계했나요?
> ADMIN, DEVELOPER, CUSTOMER 3단계 역할에 각각 레벨 값을 부여하고, `hasHigherOrEqualLevel()` 메서드로 권한 비교를 수행합니다. 이를 통해 계층적 권한 검증이 가능합니다.

---

## 배운 점 및 성장

### 기술적 성장
- Spring AOP의 동작 원리와 활용 방법 이해
- JPA 연관관계 매핑 및 N+1 문제 해결 경험
- AWS 클라우드 서비스(S3, RDS) 실무 적용 경험
- RESTful API 설계 원칙 적용

### 협업 경험
- Git Flow 브랜치 전략을 통한 팀 협업
- PR 코드 리뷰를 통한 코드 품질 향상
- API 문서화(Swagger)를 통한 프론트엔드 팀과의 원활한 소통

---

## 개선하고 싶은 점 (향후 계획)

1. **테스트 코드 보강**: 단위 테스트 및 통합 테스트 커버리지 확대
2. **캐싱 도입**: Redis를 활용한 조회 성능 최적화
3. **CI/CD 파이프라인**: GitHub Actions를 통한 자동 배포 구축
4. **실시간 알림**: WebSocket을 활용한 실시간 알림 기능 구현

---

## 참고: 기술 키워드 (ATS 최적화)

```
Java, Spring Boot, Spring Security, Spring Data JPA, QueryDSL,
PostgreSQL, AWS S3, AWS RDS, RESTful API, AOP, Gradle,
Swagger, OpenAPI, Git, GitHub, Lombok, Docker
```
