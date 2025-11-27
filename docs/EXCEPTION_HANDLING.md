# 예외 처리 (Exception Handling)

## 목차
- [개요](#개요)
- [아키텍처](#아키텍처)
- [에러 코드 체계](#에러-코드-체계)
- [커스텀 예외 클래스](#커스텀-예외-클래스)
- [예외 처리 방법](#예외-처리-방법)
- [에러 응답 구조](#에러-응답-구조)
- [환경별 응답 차이](#환경별-응답-차이)
- [테스트 방법](#테스트-방법)
- [새로운 예외 추가하기](#새로운-예외-추가하기)

---

## 개요

이 프로젝트는 일관되고 체계적인 예외 처리를 위해 계층화된 예외 처리 구조를 사용합니다.

### 주요 특징
- **통합된 에러 코드 체계**: 카테고리별로 구분된 에러 코드
- **커스텀 예외 클래스**: 비즈니스 로직에 특화된 예외 클래스
- **전역 예외 처리**: `@RestControllerAdvice`를 통한 중앙 집중식 예외 처리
- **상세한 에러 응답**: 클라이언트에게 유용한 에러 정보 제공
- **유효성 검사 통합**: Spring Validation 통합 처리
- **환경별 디버깅 정보**: 개발 환경에서만 스택 트레이스 제공 (프로덕션에서는 제외)

---

## 아키텍처

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (비즈니스 로직에서 예외 발생)           │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│      GlobalExceptionHandler             │
│  (@RestControllerAdvice)                │
│  - BusinessException 처리               │
│  - MethodArgumentNotValidException 처리 │
│  - Exception 처리 (fallback)            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         ErrorResponse                   │
│  (클라이언트에게 반환되는 JSON)          │
└─────────────────────────────────────────┘
```

### 패키지 구조

```
org.etmetmy.bn_server.exception
├── code
│   └── ErrorCode.java                 # 에러 코드 enum
├── custom
│   ├── BusinessException.java         # 최상위 비즈니스 예외
│   ├── ProjectNotFoundException.java  # 프로젝트 관련 예외
│   ├── UserNotFoundException.java     # 사용자 관련 예외
│   ├── BoardNotFoundException.java    # 게시글 관련 예외
│   ├── UnauthorizedException.java     # 인증 관련 예외
│   └── InvalidInputException.java     # 입력값 관련 예외
├── dto
│   └── ErrorResponse.java             # 에러 응답 DTO
└── handler
    └── GlobalExceptionHandler.java    # 전역 예외 처리기
```

---

## 에러 코드 체계

### 카테고리 분류

| 카테고리 | Prefix | 설명 | 예시 |
|---------|--------|------|------|
| Common | C | 공통 에러 | C001, C002, C003 |
| Project | P | 프로젝트 관련 에러 | P001, P002, P003 |
| Board | B | 게시판 관련 에러 | B001, B002, B003 |
| User | U | 사용자 관련 에러 | U001, U002, U003 |
| Auth | A | 인증/인가 관련 에러 | A001, A002, A003 |
| File | F | 파일 관련 에러 | F001, F002, F003 |

### 주요 에러 코드

#### 공통 에러 (C)
```java
INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다.")
INVALID_TYPE_VALUE(400, "C002", "잘못된 타입입니다.")
INVALID_NOT_ALLOWED(405, "C003", "허용되지 않은 HTTP 메서드입니다.")
INTERNAL_SERVER_ERROR(500, "C004", "서버 오류가 발생했습니다.")
ENTITY_NOT_FOUND(404, "C005", "요청한 리소스를 찾을 수 없습니다.")
```

#### 프로젝트 에러 (P)
```java
PROJECT_NOT_FOUND(404, "P001", "프로젝트를 찾을 수 없습니다.")
PROJECT_ALREADY_EXISTS(409, "P002", "이미 존재하는 프로젝트입니다.")
PROJECT_PERMISSION_DENIED(403, "P003", "프로젝트 접근 권한이 없습니다.")
PROJECT_CANNOT_DELETE(400, "P004", "진행 중인 프로젝트는 삭제할 수 없습니다.")
PROJECT_NAME_DUPLICATE(409, "P005", "중복된 프로젝트명입니다.")
```

#### 게시판 에러 (B)
```java
BOARD_POST_NOT_FOUND(404, "B001", "게시글을 찾을 수 없습니다.")
BOARD_COMMENT_NOT_FOUND(404, "B002", "댓글을 찾을 수 없습니다.")
BOARD_PERMISSION_DENIED(403, "B003", "게시글 수정/삭제 권한이 없습니다.")
BOARD_ALREADY_DELETED(400, "B004", "이미 삭제된 게시글입니다.")
```

#### 사용자 에러 (U)
```java
USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다.")
DUPLICATE_EMAIL(409, "U002", "이미 사용 중인 이메일입니다.")
USER_ALREADY_DELETED(400, "U003", "이미 탈퇴한 사용자입니다.")
```

#### 인증/인가 에러 (A)
```java
UNAUTHORIZED(401, "A001", "인증이 필요합니다.")
FORBIDDEN(403, "A002", "접근 권한이 없습니다.")
INVALID_TOKEN(401, "A003", "유효하지 않은 토큰입니다.")
EXPIRED_TOKEN(401, "A004", "만료된 토큰입니다.")
INVALID_PASSWORD(401, "A005", "비밀번호가 일치하지 않습니다.")
```

#### 파일 에러 (F)
```java
FILE_NOT_FOUND(404, "F001", "파일을 찾을 수 없습니다")
FILE_SIZE_EXCEEDED(400, "F002", "파일 크기가 제한을 초과했습니다")
INVALID_FILE_TYPE(400, "F003", "지원하지 않는 파일 형식입니다")
FILE_UPLOAD_FAILED(500, "F004", "파일 업로드에 실패했습니다")
```

---

## 커스텀 예외 클래스

### BusinessException (최상위 예외)

모든 커스텀 예외의 부모 클래스입니다.

```java
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### 하위 예외 클래스들

각 도메인별로 특화된 예외 클래스를 제공합니다.

#### ProjectNotFoundException
```java
// 기본 메시지 사용
throw new ProjectNotFoundException();
// → "프로젝트를 찾을 수 없습니다."

// 커스텀 메시지 사용
throw new ProjectNotFoundException("ID 123번 프로젝트를 찾을 수 없습니다");
// → "ID 123번 프로젝트를 찾을 수 없습니다"
```

#### UserNotFoundException
```java
throw new UserNotFoundException();
throw new UserNotFoundException("이메일 user@example.com의 사용자를 찾을 수 없습니다");
```

#### BoardNotFoundException
```java
throw new BoardNotFoundException();
throw new BoardNotFoundException("게시글 ID 456을 찾을 수 없습니다");
```

#### UnauthorizedException
```java
throw new UnauthorizedException();
throw new UnauthorizedException("로그인이 필요한 서비스입니다");
```

#### InvalidInputException
```java
throw new InvalidInputException();
throw new InvalidInputException("프로젝트명은 2자 이상이어야 합니다");
```

---

## 예외 처리 방법

### Service Layer에서 예외 발생

```java
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectDto getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(
                "프로젝트 ID " + projectId + "를 찾을 수 없습니다"
            ));

        return ProjectDto.from(project);
    }

    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(ProjectNotFoundException::new);

        // 권한 검증
        if (!project.isOwner(userId)) {
            throw new UnauthorizedException("프로젝트 삭제 권한이 없습니다");
        }

        // 상태 검증
        if (project.isInProgress()) {
            throw new InvalidInputException("진행 중인 프로젝트는 삭제할 수 없습니다");
        }

        projectRepository.delete(project);
    }
}
```

### Controller Layer에서 예외 전파

```java
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long projectId) {
        // Service에서 발생한 예외는 GlobalExceptionHandler가 자동으로 처리
        ProjectDto project = projectService.getProject(projectId);
        return ResponseEntity.ok(project);
    }
}
```

### 유효성 검사 예외

```java
@PostMapping
public ResponseEntity<ProjectDto> createProject(
    @Valid @RequestBody CreateProjectRequest request) {
    // @Valid 검사 실패 시 MethodArgumentNotValidException 자동 발생
    // GlobalExceptionHandler에서 자동 처리
    ProjectDto project = projectService.createProject(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(project);
}
```

---

## 에러 응답 구조

### 기본 에러 응답

```json
{
  "code": "P001",
  "message": "프로젝트를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects/123"
}
```

### 유효성 검사 실패 응답

```json
{
  "code": "C001",
  "message": "잘못된 입력값입니다.",
  "status": 400,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects",
  "fieldErrors": [
    {
      "field": "projectName",
      "value": "",
      "reason": "프로젝트명은 필수입니다"
    },
    {
      "field": "email",
      "value": "invalid-email",
      "reason": "유효한 이메일 형식이어야 합니다"
    }
  ]
}
```

### ErrorResponse DTO 구조

```java
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String code;              // 에러 코드
    private final String message;           // 에러 메시지
    private final int status;               // HTTP 상태 코드
    private final LocalDateTime timestamp;  // 에러 발생 시간
    private final String path;              // 에러 발생 API 경로
    private final List<FieldError> fieldErrors;  // 유효성 검사 실패 시
    private final String debugMessage;      // 디버그 정보 (개발 환경에서만)

    @Getter
    @Builder
    public static class FieldError {
        private final String field;   // 에러 발생 필드명
        private final String value;   // 잘못된 입력값
        private final String reason;  // 에러 이유
    }
}
```

**참고**: `@JsonInclude(JsonInclude.Include.NON_NULL)`로 인해 null 값인 필드는 JSON 응답에 포함되지 않습니다.

---

## 환경별 응답 차이

### 개요

`GlobalExceptionHandler`는 `spring.profiles.active` 설정값에 따라 다른 응답을 제공합니다:
- **개발 환경** (dev, local, default): `debugMessage` 필드에 상세한 디버깅 정보 포함
- **프로덕션 환경** (prod): `debugMessage` 필드 제외 (보안을 위해)

### 환경 감지 로직

```java
@Value("${spring.profiles.active:dev}")
private String activeProfile;

private boolean isDevelopmentMode() {
    return !activeProfile.equals("prod");
}
```

### 개발 환경 응답 (dev, local, default)

#### BusinessException
```json
{
  "code": "P001",
  "message": "프로젝트를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects/123",
  "debugMessage": "org.etmetmy.bn_server.exception.custom.ProjectNotFoundException: 프로젝트를 찾을 수 없습니다.\n\tat org.etmetmy.bn_server.service.ProjectService.getProject(ProjectService.java:25)\n\tat ..."
}
```

#### MethodArgumentNotValidException
```json
{
  "code": "C001",
  "message": "잘못된 입력값입니다.",
  "status": 400,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects",
  "fieldErrors": [...],
  "debugMessage": "Validation errors: 2 field(s)"
}
```

#### Exception (Unexpected)
```json
{
  "code": "C004",
  "message": "서버 오류가 발생했습니다.",
  "status": 500,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects",
  "debugMessage": "java.lang.RuntimeException: Unexpected error\n\tat ..."
}
```

### 프로덕션 환경 응답 (prod)

#### 모든 예외
```json
{
  "code": "P001",
  "message": "프로젝트를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2024-11-27T14:30:00",
  "path": "/api/projects/123"
}
```

**특징**:
- `debugMessage` 필드가 완전히 제외됨
- 민감한 서버 정보 노출 방지
- 클라이언트에게 필요한 정보만 제공

### 환경 설정 방법

#### application.yml (기본값: dev)
```yaml
spring:
  profiles:
    active: dev  # 또는 local, prod
```

#### 환경변수로 설정
```bash
# 개발 환경
export SPRING_PROFILES_ACTIVE=dev

# 프로덕션 환경
export SPRING_PROFILES_ACTIVE=prod
```

#### Docker 환경
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

### 장점

1. **개발 편의성**: 개발 중 상세한 스택 트레이스로 빠른 디버깅 가능
2. **보안 강화**: 프로덕션에서는 내부 구조 노출 방지
3. **자동 전환**: 환경 설정만으로 자동 적용
4. **일관성**: 모든 예외 핸들러에서 동일한 로직 적용

---

## 테스트 방법

### 1. 단위 테스트 실행

```bash
# 모든 예외 처리 테스트 실행
./gradlew test --tests GlobalExceptionHandlerTest

# 특정 테스트만 실행
./gradlew test --tests GlobalExceptionHandlerTest.handleProjectNotFoundException
```

### 2. 테스트 케이스 구성

프로젝트의 `GlobalExceptionHandlerTest.java`에는 다음 테스트가 포함되어 있습니다:

#### 2.1 ProjectNotFoundException 테스트
```java
@Test
@DisplayName("ProjectNotFoundException 발생 시 404 응답과 에러 정보 반환")
void handleProjectNotFoundException() throws Exception {
    mockMvc.perform(get("/test/project-not-found"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("P001"))
        .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다."))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.path").value("/test/project-not-found"))
        .andExpect(jsonPath("$.timestamp").exists());
}
```

**테스트 엔드포인트**: `GET /test/project-not-found`

**기대 결과**:
- HTTP Status: `404 NOT FOUND`
- Response Body:
  ```json
  {
    "code": "P001",
    "message": "프로젝트를 찾을 수 없습니다.",
    "status": 404,
    "path": "/test/project-not-found",
    "timestamp": "2024-11-27T14:30:00"
  }
  ```

#### 2.2 UnauthorizedException 테스트
```java
@Test
@DisplayName("UnauthorizedException 발생 시 401 응답과 에러 정보 반환")
void handleUnauthorizedException() throws Exception {
    mockMvc.perform(get("/test/unauthorized"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("A001"))
        .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
        .andExpect(jsonPath("$.status").value(401));
}
```

**테스트 엔드포인트**: `GET /test/unauthorized`

**기대 결과**:
- HTTP Status: `401 UNAUTHORIZED`
- Error Code: `A001`

#### 2.3 커스텀 메시지 예외 테스트
```java
@Test
@DisplayName("커스텀 메시지를 가진 예외 처리")
void handleCustomMessageException() throws Exception {
    mockMvc.perform(get("/test/custom-message"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("P001"))
        .andExpect(jsonPath("$.message").value("ID 999번 프로젝트를 찾을 수 없습니다"))
        .andExpect(jsonPath("$.status").value(404));
}
```

**테스트 엔드포인트**: `GET /test/custom-message`

**기대 결과**:
- 기본 메시지 대신 커스텀 메시지가 반환됨
- `"ID 999번 프로젝트를 찾을 수 없습니다"`

#### 2.4 예상치 못한 예외 테스트
```java
@Test
@DisplayName("예상치 못한 예외 발생 시 500 응답 반환")
void handleUnexpectedException() throws Exception {
    mockMvc.perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("C004"))
        .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
        .andExpect(jsonPath("$.status").value(500));
}
```

**테스트 엔드포인트**: `GET /test/unexpected`

**기대 결과**:
- HTTP Status: `500 INTERNAL SERVER ERROR`
- Error Code: `C004`
- 일반적인 RuntimeException이 fallback 처리됨

#### 2.5 유효성 검사 실패 테스트
```java
@Test
@DisplayName("유효성 검사 실패 시 400 응답과 필드 에러 반환")
void handleValidationException() throws Exception {
    String invalidRequest = """
    {
        "projectName": "",
        "email": "invalid-email"
    }
    """;

    mockMvc.perform(post("/test/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"))
        .andExpect(jsonPath("$.fieldErrors").isArray())
        .andExpect(jsonPath("$.fieldErrors[0].field").exists())
        .andExpect(jsonPath("$.fieldErrors[0].reason").exists());
}
```

**테스트 엔드포인트**: `POST /test/validate`

**요청 Body**:
```json
{
  "projectName": "",
  "email": "invalid-email"
}
```

**기대 결과**:
- HTTP Status: `400 BAD REQUEST`
- Response Body:
  ```json
  {
    "code": "C001",
    "message": "잘못된 입력값입니다.",
    "status": 400,
    "path": "/test/validate",
    "timestamp": "2024-11-27T14:30:00",
    "fieldErrors": [
      {
        "field": "projectName",
        "value": "",
        "reason": "프로젝트명은 필수입니다"
      },
      {
        "field": "email",
        "value": "invalid-email",
        "reason": "유효한 이메일 형식이어야 합니다"
      }
    ]
  }
  ```

### 3. Postman/cURL로 테스트

#### 예시 1: ProjectNotFoundException
```bash
curl -X GET http://localhost:8080/test/project-not-found
```

#### 예시 2: 유효성 검사 실패
```bash
curl -X POST http://localhost:8080/test/validate \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "",
    "email": "invalid-email"
  }'
```

### 4. 테스트 결과 확인

테스트 실행 시 콘솔에서 다음과 같은 로그를 확인할 수 있습니다:

```
MockHttpServletRequest:
      HTTP Method = GET
      Request URI = /test/project-not-found

MockHttpServletResponse:
           Status = 404
    Error message = null
          Headers = [Content-Type:"application/json"]
     Content type = application/json
             Body = {"code":"P001","message":"프로젝트를 찾을 수 없습니다.","status":404,"timestamp":"2024-11-27T14:30:00","path":"/test/project-not-found"}
```

### 5. 로그 확인

예외 발생 시 다음과 같은 로그가 기록됩니다:

```
# BusinessException 발생
WARN  - BusinessException: code=P001, message=프로젝트를 찾을 수 없습니다., path=/test/project-not-found, method=GET

# 유효성 검사 실패
WARN  - Validation failed: path=/test/validate, errors=projectName: 프로젝트명은 필수입니다, email: 유효한 이메일 형식이어야 합니다

# 예상치 못한 예외
ERROR - Unexpected error: RuntimeException at /test/unexpected [GET]
java.lang.RuntimeException: 예상치 못한 에러
	at org.etmetmy.bn_server.GlobalExceptionHandlerTest$TestController.throwUnexpectedException(GlobalExceptionHandlerTest.java:134)
	...
```

**로그 개선사항**:
- 일관된 경로 형식 사용 (`request.getRequestURI()`)
- 예상치 못한 예외의 로그 메시지 간결화
- 스택 트레이스 자동 포함 (ERROR 레벨)

---

## 새로운 예외 추가하기

새로운 예외를 추가하려면 다음 단계를 따르세요:

### 1단계: ErrorCode에 에러 코드 추가

`ErrorCode.java`에 새로운 에러 코드를 추가합니다.

```java
public enum ErrorCode {
    // 기존 코드...

    // 새로운 에러 코드 추가 예시
    TASK_NOT_FOUND(404, "T001", "작업을 찾을 수 없습니다."),
    TASK_ALREADY_COMPLETED(400, "T002", "이미 완료된 작업입니다."),
}
```

### 2단계: 커스텀 예외 클래스 생성

`exception/custom/` 디렉토리에 새로운 예외 클래스를 생성합니다.

```java
package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

public class TaskNotFoundException extends BusinessException {

    public TaskNotFoundException() {
        super(ErrorCode.TASK_NOT_FOUND);
    }

    public TaskNotFoundException(String message) {
        super(ErrorCode.TASK_NOT_FOUND, message);
    }
}
```

### 3단계: Service Layer에서 사용

```java
@Service
public class TaskService {

    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(
                "작업 ID " + taskId + "를 찾을 수 없습니다"
            ));
    }
}
```

### 4단계: 테스트 작성

```java
@Test
@DisplayName("TaskNotFoundException 발생 시 404 응답 반환")
void handleTaskNotFoundException() throws Exception {
    mockMvc.perform(get("/api/tasks/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("T001"))
        .andExpect(jsonPath("$.message").value("작업을 찾을 수 없습니다."));
}
```

---

## 추가 정보

### GlobalExceptionHandler 처리 우선순위

1. **BusinessException** (가장 구체적) - 커스텀 비즈니스 예외
2. **MethodArgumentNotValidException** - 유효성 검사 실패
3. **Exception** (가장 일반적) - 모든 예상치 못한 예외 (fallback)

### 주의사항

- 모든 커스텀 예외는 `BusinessException`을 상속해야 합니다
- 에러 코드는 중복되지 않도록 주의하세요
- 커스텀 메시지는 사용자에게 노출될 수 있으므로 민감한 정보를 포함하지 마세요
- 서버 내부 에러 정보는 로그에만 기록하고 클라이언트에는 일반적인 메시지만 반환하세요

### 로깅 레벨

- `BusinessException`: **WARN** - 예상된 비즈니스 예외
- `MethodArgumentNotValidException`: **WARN** - 유효성 검사 실패
- `Exception`: **ERROR** - 예상치 못한 시스템 오류