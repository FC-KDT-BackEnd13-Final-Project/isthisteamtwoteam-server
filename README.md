# B&N - 개발사-고객사 협업 프로젝트 관리 플랫폼

> 개발사와 고객사 간의 프로젝트 진행 상황을 실시간으로 공유하고, 승인/거절 요청 프로세스를 통해 효율적인 협업을 지원하는 웹 기반 프로젝트 관리 시스템

## 프로젝트 개요

B&N은 소프트웨어 외주 개발 프로젝트에서 발생하는 커뮤니케이션 문제를 해결하기 위해 개발되었습니다. 개발사는 프로젝트 진행 상황을 게시글로 공유하고, 고객사는 이를 검토하여 승인 또는 거절할 수 있습니다. 모든 변경 사항은 자동으로 기록되어 투명한 프로젝트 관리가 가능합니다.

### 핵심 기능

- **프로젝트 관리**: Stage 기반 진행상황 관리 (요구사항 정의 → 화면 설계 → 디자인/퍼블리싱 → 개발 → 검수 → 유지보수)
- **승인 요청 워크플로우**: 개발사의 요청 → 고객사의 승인/거절 프로세스
- **활동 로그 시스템**: AOP 기반 자동 활동 로그 기록
- **변경 이력 추적**: Before/After 데이터를 포함한 완전한 감사 추적
- **휴지통 기능**: Soft Delete 기반 데이터 복구 지원
- **역할별 대시보드**: ADMIN, DEVELOPER, CUSTOMER 맞춤형 뷰

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.5.7 |
| **ORM** | Spring Data JPA + QueryDSL 5.0 |
| **Security** | Spring Security (세션 기반 인증) |
| **Database** | PostgreSQL 15 (AWS RDS) |
| **File Storage** | AWS S3 SDK v2 |
| **Email** | Spring Mail (Gmail SMTP) |
| **API Documentation** | Springdoc OpenAPI (Swagger) |
| **Build Tool** | Gradle |

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Web)                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐    │
│  │Controller │→ │  Service  │→ │Repository │→ │  Entity   │    │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘    │
│        │              │                                         │
│        ▼              ▼                                         │
│  ┌───────────┐  ┌───────────┐                                  │
│  │  AOP      │  │  Event    │                                  │
│  │ (Logging) │  │ (Async)   │                                  │
│  └───────────┘  └───────────┘                                  │
└─────────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
   ┌──────────┐        ┌──────────┐        ┌──────────┐
   │PostgreSQL│        │  AWS S3  │        │Gmail SMTP│
   │  (RDS)   │        │ (파일)   │        │ (메일)   │
   └──────────┘        └──────────┘        └──────────┘
```

---

## 주요 기술적 구현

### 1. AOP 기반 활동 로그 시스템

비즈니스 로직과 로깅 코드의 관심사 분리를 위해 커스텀 어노테이션과 AOP를 활용했습니다.

```java
// 사용 예시 - 어노테이션 하나로 자동 로깅
@ActivityLogger(targetType = "Post", action = "CREATE")
public PostCreateResponse createPost(Long projectId, PostCreateRequest request) {
    // 비즈니스 로직만 작성
}
```

**구현 포인트**:
- `@ActivityLogger` 커스텀 어노테이션 정의
- `@Around` 어드바이스로 메서드 실행 전후 데이터 캡처
- `ApplicationEventPublisher` + `@Async`로 비동기 처리 (API 응답 속도 영향 없음)
- JSON 형식으로 상세 정보 저장

### 2. Soft Delete & 휴지통 시스템

실수로 삭제된 데이터의 복구를 지원하고, 삭제 이력을 추적합니다.

```java
public void softDelete(Long deletedBy) {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
}

public void restore() {
    this.isDeleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
}
```

**구현 포인트**:
- `isDeleted`, `deletedAt`, `deletedBy` 필드로 삭제 상태 관리
- JPA `@Where` 어노테이션으로 조회 시 자동 필터링
- 휴지통 API로 UI에서 복구/영구삭제 가능

### 3. 변경 이력 추적 시스템

모든 데이터 변경에 대한 Before/After 기록을 자동으로 생성합니다.

```java
// HistoryPost 엔티티 - 변경 전후 데이터 저장
@Column(name = "be_title")  // Before - 변경 전
private String beTitle;

@Column(name = "af_title")  // After - 변경 후
private String afTitle;

@Column(name = "change_type")  // CREATE, UPDATE, DELETE
@Enumerated(EnumType.STRING)
private ChangeType changeType;
```

### 4. 역할 기반 접근 제어 (RBAC)

계층적 권한 체계로 URL별 접근을 제어합니다.

```java
public enum Role {
    ADMIN("관리자", "ROLE_ADMIN", 3),
    DEVELOPER("개발사", "ROLE_DEVELOPER", 2),
    CUSTOMER("고객사", "ROLE_USERS", 1);

    public boolean hasHigherOrEqualLevel(Role other) {
        return this.level >= other.level;
    }
}
```

---

## ERD (Entity Relationship Diagram)

```
User ─────────────┬──── N:1 ──── Company
                  │
                  ├──── 1:N ──── Post ─────────┬──── 1:1 ──── Request
                  │               │            │
                  │               ├──── 1:N ──── Comment ──── 1:N ──── Comment (대댓글)
                  │               │
                  │               └──── N:1 ──── Stage
                  │
                  └──── 1:N ──── ActivityLog

Project ──────────┬──── N:1 ──── Company
                  │
                  ├──── N:1 ──── Stage
                  │
                  ├──── 1:N ──── Post
                  │
                  ├──── 1:N ──── ProjectMember ──── N:1 ──── User
                  │
                  ├──── 1:N ──── CheckList
                  │
                  └──── 1:N ──── File (AWS S3)

File ─────────────┬──── N:1 ──── Post
                  ├──── N:1 ──── Comment
                  ├──── N:1 ──── CheckList
                  └──── N:1 ──── Request
```

---

## API 엔드포인트

### 인증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/login` | 로그인 |
| POST | `/api/v1/logout` | 로그아웃 |

### 프로젝트 관리 (Admin)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/admin/projects` | 프로젝트 생성 |
| GET | `/api/v1/admin/projects` | 프로젝트 목록 조회 |
| POST | `/api/v1/admin/projects/{id}/members` | 멤버 추가 |

### 게시글 관리
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/users/projects/{projectId}/posts` | 게시글 작성 |
| GET | `/api/v1/users/projects/{projectId}/posts` | 게시글 목록 |
| PATCH | `/api/v1/users/projects/posts/{postId}/approval` | 게시글 승인 |
| PATCH | `/api/v1/users/projects/posts/{postId}/reject` | 게시글 거절 |

### 대시보드
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/dashboard` | 관리자 대시보드 |
| GET | `/api/v1/developers/dashboard` | 개발사 대시보드 |
| GET | `/api/v1/customers/dashboard` | 고객사 대시보드 |

> 전체 API 문서: `/swagger-ui`

---

## 프로젝트 구조

```
src/main/java/org/etmetmy/bn_server/
├── config/                    # Spring 설정 (Security, S3, Mail, Swagger 등)
├── domain/
│   ├── user/                  # 사용자 관리
│   ├── company/               # 회사 관리
│   ├── project/               # 프로젝트 관리
│   ├── post/                  # 게시글 관리
│   ├── comment/               # 댓글 관리
│   ├── file/                  # 파일 관리 (AWS S3)
│   ├── activityLog/           # 활동 로그 (AOP)
│   │   ├── aop/               # @ActivityLogger 어노테이션, AOP 구현
│   │   ├── event/             # 비동기 이벤트
│   │   └── handler/           # 이벤트 핸들러
│   ├── history/               # 변경 이력 추적
│   ├── dashboard/             # 역할별 대시보드
│   ├── trash/                 # 휴지통 기능
│   └── ...
├── global/                    # 공통 기능 (BaseEntity, 예외 처리)
└── exception/                 # 커스텀 예외
```

---

## 실행 방법

### 필수 환경
- Java 21
- PostgreSQL 15
- AWS S3 계정
- Gmail 계정 (App Password)

### 환경 변수 설정 (.env)

```env
# Database
RDS_DB_URL=jdbc:postgresql://localhost:5432/bn_db
RDS_DB_USERNAME=your_username
RDS_DB_PASSWORD=your_password

# AWS S3
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
BUCKET_NAME=your_bucket_name

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### API 문서 접근
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI JSON: http://localhost:8080/api-docs

---

## 트러블슈팅 & 학습 포인트

### 1. N+1 문제 해결
**문제**: Post 조회 시 연관된 User, Stage를 개별 쿼리로 조회하여 성능 저하

**해결**: JOIN FETCH 적용
```java
@Query("SELECT p FROM Post p " +
       "JOIN FETCH p.user u " +
       "JOIN FETCH p.stage s " +
       "WHERE p.project.id = :projectId")
List<Post> findAllByProjectId(@Param("projectId") Long projectId);
```

### 2. 비동기 로깅으로 API 응답 속도 개선
**문제**: 활동 로그 저장이 API 응답 시간에 영향

**해결**: `@Async` + `ApplicationEventPublisher` 조합으로 비동기 처리
```java
@Async
@EventListener
public void handleActivityLogEvent(ActivityLogEvent event) {
    activityLogRepository.save(event.getActivityLog());
}
```

### 3. 동적 쿼리의 타입 안전성 확보
**문제**: JPQL 문자열 쿼리의 런타임 오류

**해결**: QueryDSL 도입으로 컴파일 타임 검증

---

## 팀 구성

| 역할 | 이름 | 담당 |
|------|------|------|
| Backend | - | 프로젝트 전체 API 개발 |

---

## 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.
