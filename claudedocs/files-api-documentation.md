# Files 조회 API 문서

## 개요
프로젝트별 파일 목록을 조회하는 API입니다. 세션 기반 인증을 사용하며, 로그인한 사용자만 접근할 수 있습니다.

## API 엔드포인트

### 프로젝트별 파일 목록 조회

**Endpoint**: `GET /users/projects/{projectId}/files`

**Description**: 특정 프로젝트에 속한 활성 파일 목록을 조회합니다.

**Authentication**: 세션 기반 인증 필수

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| projectId | Long | O | 조회할 프로젝트 ID |

**Request Headers**:
```
Cookie: JSESSIONID={session-id}
```

**Request Example**:
```http
GET /users/projects/1/files HTTP/1.1
Host: api.example.com
Cookie: JSESSIONID=ABC123XYZ
```

---

## 응답 형식

### 성공 응답 (200 OK)

**Response Body**:
```json
{
  "success": true,
  "message": "파일 목록 조회 성공",
  "response": [
    {
      "fileId": 1,
      "fileTitle": "project-plan.pdf",
      "filePath": "/uploads/2025/12/project-plan.pdf",
      "fileType": "application/pdf",
      "fileSize": 2048576,
      "entityTypeName": "Document",
      "postId": 10,
      "uploadUserId": 5,
      "uploadedAt": "2025-12-01T14:30:00"
    },
    {
      "fileId": 2,
      "fileTitle": "design-mockup.png",
      "filePath": "/uploads/2025/12/design-mockup.png",
      "fileType": "image/png",
      "fileSize": 1024000,
      "entityTypeName": "Image",
      "postId": 12,
      "uploadUserId": 7,
      "uploadedAt": "2025-12-02T09:15:00"
    }
  ]
}
```

**Response Fields**:
| 필드 | 타입 | 설명 |
|------|------|------|
| success | Boolean | 성공 여부 (항상 true) |
| message | String | 응답 메시지 |
| response | Array | 파일 목록 (빈 배열 가능) |

**File Object Fields**:
| 필드 | 타입 | 설명 |
|------|------|------|
| fileId | Long | 파일 고유 ID |
| fileTitle | String | 파일명 |
| filePath | String | 파일 저장 경로 |
| fileType | String | 파일 MIME 타입 |
| fileSize | Long | 파일 크기 (bytes) |
| entityTypeName | String | 파일이 속한 엔티티 타입 |
| postId | Long | 파일이 첨부된 게시글 ID |
| uploadUserId | Long | 업로드한 사용자 ID |
| uploadedAt | DateTime | 업로드 일시 |

---

### 에러 응답

#### 1. 로그인하지 않은 경우 (401 Unauthorized)

```json
{
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다.",
  "status": 401,
  "timestamp": "2025-12-02T10:30:00",
  "path": "/users/projects/1/files"
}
```

**발생 조건**:
- 세션이 없거나 만료된 경우
- 로그인하지 않은 상태에서 API 호출

#### 2. 프로젝트 접근 권한이 없는 경우 (403 Forbidden)

```json
{
  "code": "P003",
  "message": "프로젝트 접근 권한이 없습니다.",
  "status": 403,
  "timestamp": "2025-12-02T10:30:00",
  "path": "/users/projects/1/files"
}
```

**발생 조건**:
- 로그인은 했지만 해당 프로젝트의 멤버가 아닌 경우
- ProjectMember 테이블에 사용자가 등록되지 않은 프로젝트에 접근

#### 3. 프로젝트를 찾을 수 없는 경우 (404 Not Found)

```json
{
  "code": "P001",
  "message": "프로젝트를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2025-12-02T10:30:00",
  "path": "/users/projects/999/files"
}
```

**발생 조건**:
- 존재하지 않는 projectId로 요청
- 삭제된 프로젝트에 접근

#### 4. 서버 내부 오류 (500 Internal Server Error)

```json
{
  "code": "C004",
  "message": "서버 오류가 발생했습니다.",
  "status": 500,
  "timestamp": "2025-12-02T10:30:00",
  "path": "/users/projects/1/files",
  "debugMessage": "[개발 환경에서만 표시되는 스택 트레이스]"
}
```

---

## 구현 세부사항

### 1. 아키텍처

```
FileController (Controller Layer)
    ↓
FileService (Service Layer)
    ↓
FileRepository (Repository Layer)
    ↓
Database (MySQL)
```

### 2. 보안 및 검증

**구현된 검증**:
1. **세션 인증**: `SessionUtil.getLoginUserId(session)`을 통한 로그인 사용자 확인
2. **프로젝트 존재 확인**: `projectRepository.findById()` 를 통한 검증
3. **Soft Delete 필터링**: `isDeleted = false` 조건으로 삭제된 파일 제외

**향후 추가 필요**:
- 프로젝트 접근 권한 검증 (ProjectMember 테이블 연동)
- 현재는 로그인만 되어 있으면 모든 프로젝트의 파일을 볼 수 있음

### 3. 성능 최적화

**N+1 문제 해결**:
```java
@Query("SELECT f FROM File f " +
       "JOIN FETCH f.entityType " +
       "JOIN FETCH f.post p " +
       "WHERE p.project.id = :projectId " +
       "AND f.isDeleted = false")
```

- `JOIN FETCH`를 사용하여 EntityType과 Post를 한 번에 로드
- 파일 100개 조회 시: 201개 쿼리 → 1개 쿼리로 개선

### 4. 데이터베이스 관계

```
Project (1) ──< (N) Post (1) ──< (N) File
                                      ├─< (N) EntityType
```

---

## 사용 예시

### JavaScript (Axios)

```javascript
// 파일 목록 조회
async function getProjectFiles(projectId) {
  try {
    const response = await axios.get(`/users/projects/${projectId}/files`, {
      withCredentials: true  // 세션 쿠키 포함
    });

    if (response.data.success) {
      const files = response.data.response;
      console.log('파일 목록:', files);
      return files;
    }
  } catch (error) {
    if (error.response.status === 401) {
      console.error('로그인이 필요합니다.');
      // 로그인 페이지로 리다이렉트
    } else if (error.response.status === 404) {
      console.error('프로젝트를 찾을 수 없습니다.');
    }
  }
}
```

### Java (Spring RestTemplate)

```java
@Service
public class FileClient {

    @Autowired
    private RestTemplate restTemplate;

    public List<ActiveFileListDTO> getProjectFiles(Long projectId) {
        String url = "http://api.example.com/users/projects/" + projectId + "/files";

        ResponseEntity<CommonResponse<List<ActiveFileListDTO>>> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CommonResponse<List<ActiveFileListDTO>>>() {}
            );

        return response.getBody().getResponse();
    }
}
```

---

## 코드 위치

### Controller
**파일**: `src/main/java/org/etmetmy/bn_server/domain/file/controller/FileController.java`

**메서드**: `getFiles(Long projectId, HttpServletRequest request)`

### Service
**파일**: `src/main/java/org/etmetmy/bn_server/domain/file/service/FileServiceImpl.java`

**메서드**: `findAllByProjectId(Long projectId, HttpSession session)`

### Repository
**파일**: `src/main/java/org/etmetmy/bn_server/domain/file/repository/FileRepository.java`

**메서드**: `findByProjectId(Long projectId)`

### DTO
**파일**: `src/main/java/org/etmetmy/bn_server/domain/file/dto/ActiveFileListDTO.java`

---

## 테스트 시나리오

### 1. 정상 케이스
```bash
# 로그인 후 파일 조회
curl -X GET http://localhost:8080/users/projects/1/files \
  -H "Cookie: JSESSIONID=ABC123" \
  -v

# 예상 결과: 200 OK + 파일 목록
```

### 2. 인증 실패
```bash
# 세션 없이 조회
curl -X GET http://localhost:8080/users/projects/1/files -v

# 예상 결과: 401 Unauthorized
```

### 3. 프로젝트 없음
```bash
# 존재하지 않는 프로젝트
curl -X GET http://localhost:8080/users/projects/999999/files \
  -H "Cookie: JSESSIONID=ABC123" \
  -v

# 예상 결과: 404 Not Found
```

### 4. 빈 파일 목록
```bash
# 파일이 없는 프로젝트
curl -X GET http://localhost:8080/users/projects/5/files \
  -H "Cookie: JSESSIONID=ABC123" \
  -v

# 예상 결과: 200 OK + response: []
```

---

## 향후 개선 사항

### 1. 프로젝트 접근 권한 검증
**현재 상태**: 로그인만 되어 있으면 모든 프로젝트의 파일 조회 가능

**개선 방안**:
```java
// ProjectMember 테이블 추가 후
if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, loginUserId)) {
    throw new UnauthorizedException("프로젝트 접근 권한이 없습니다.");
}
```

### 2. 페이징 처리
**현재 상태**: 전체 파일을 한 번에 조회

**개선 방안**:
```java
public Page<ActiveFileListDTO> findAllByProjectId(
    Long projectId,
    HttpSession session,
    Pageable pageable
)
```

### 3. 필터링 및 정렬
**추가 기능**:
- 파일 타입별 필터링 (이미지, 문서, 비디오 등)
- 업로드 날짜 기준 정렬
- 파일명 검색

### 4. 파일 다운로드 링크
**개선 방안**:
- 응답에 파일 다운로드 URL 포함
- 임시 다운로드 토큰 생성

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2025-12-02 | 초기 문서 작성 | - |
| | | - CommonResponse 적용 | |
| | | - 세션 기반 인증 구현 | |
| | | - N+1 문제 해결 | |

---

## 참고 사항

### CommonResponse 구조
모든 API 응답은 `CommonResponse<T>` 형식을 따릅니다:

```java
@Getter
public class CommonResponse<T> {
    private final Boolean success;  // 성공 여부
    private final T response;       // 실제 데이터
    private final String message;   // 메시지
}
```

### SessionUtil 사용법
로그인 사용자 정보 추출:

```java
// 사용자 ID만 필요한 경우
Long userId = SessionUtil.getLoginUserId(session);

// User 객체 전체가 필요한 경우
User user = SessionUtil.getLoginUserOrThrow(session);

// 로그인 여부만 확인
boolean isLoggedIn = SessionUtil.isLoggedIn(session);
```

---

## 문의 및 지원

구현 관련 문의사항이나 버그 제보는 개발팀에 문의하시기 바랍니다.