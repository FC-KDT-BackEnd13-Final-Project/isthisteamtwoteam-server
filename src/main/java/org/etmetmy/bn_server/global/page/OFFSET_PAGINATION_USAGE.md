# Offset 기반 페이지네이션 사용 가이드

## 📚 개요

`PageRequest`, `PageImpl`, `SliceImpl`을 사용하여 전통적인 Offset 기반 페이지네이션을 구현합니다.
CheckList처럼 **소량 데이터**를 **페이지 번호**(1, 2, 3...)로 탐색하는 경우에 적합합니다.

## 🎯 적합한 사용 케이스

### ✅ Offset 방식 추천
- **페이지 번호 UI**: 1, 2, 3, 4, 5... 버튼으로 이동
- **소량 데이터**: 전체 데이터가 수천 건 이하
- **전체 페이지 수 표시**: "1/10 페이지" 표시 필요
- **특정 페이지 이동**: "3페이지로 바로 가기"
- **예시**: CheckList(5개씩), 게시판(10개씩), 관리자 목록

### ❌ Offset 방식 비추천
- **대용량 데이터**: 수만 건 이상 (마지막 페이지 느림)
- **무한 스크롤**: 계속 아래로 스크롤
- **실시간 데이터**: 데이터가 자주 추가/삭제됨
- **예시**: SNS 피드, 실시간 로그, 대용량 검색 결과

---

## 📦 구성 요소

### 1. `PageRequest`
- 페이지 요청 정보 (Pageable 구현)
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기
- `sort`: 정렬 정보 (선택)

### 2. `PageImpl<T>`
- 전체 페이지네이션 정보 포함
- **COUNT 쿼리 실행** → 전체 데이터 개수 조회
- 전체 페이지 수, 전체 요소 개수 제공
- 페이지 번호 UI에 사용

### 3. `SliceImpl<T>`
- 가벼운 페이지네이션
- **COUNT 쿼리 없음** → 성능 향상
- 다음 페이지 존재 여부만 제공
- "더보기" 버튼에 사용

---

## 💻 사용 예시

## 예시 1: CheckList 조회 (Page 사용 - 전체 페이지 수 필요)

### 1) Controller

```java
@RestController
@RequestMapping("/admin/checklists")
@RequiredArgsConstructor
public class CheckListController {

    private final CheckListService checkListService;

    /**
     * CheckList 목록 조회 (페이지네이션)
     * GET /admin/checklists?page=0&size=5
     */
    @GetMapping
    public Page<CheckListResponse> getCheckLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return checkListService.getCheckLists(pageRequest);
    }
}
```

### 2) Service

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckListServiceImpl implements CheckListService {

    private final CheckListRepository checkListRepository;

    @Override
    public Page<CheckListResponse> getCheckLists(Pageable pageable) {
        // 1. Repository에서 Page<CheckList> 조회
        Page<CheckList> checkListPage = checkListRepository.findAll(pageable);

        // 2. Entity -> DTO 변환
        return checkListPage.map(CheckListResponse.Converter::from);
    }
}
```

### 3) Repository

#### Option A: Spring Data JPA 기본 메서드 사용

```java
public interface CheckListRepository extends JpaRepository<CheckList, Long> {
    // 기본 findAll(Pageable) 사용 - 별도 구현 불필요
}
```

#### Option B: 커스텀 쿼리 (조건 추가)

```java
public interface CheckListRepository extends JpaRepository<CheckList, Long> {

    /**
     * 특정 조건으로 CheckList 조회
     */
    @Query("SELECT c FROM CheckList c WHERE c.content LIKE %:keyword%")
    Page<CheckList> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

#### Option C: QueryDSL 사용

```java
@Repository
@RequiredArgsConstructor
public class CheckListRepositoryCustomImpl implements CheckListRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CheckList> findByConditions(String keyword, Pageable pageable) {
        QCheckList checkList = QCheckList.checkList;

        // 1. 데이터 조회 (limit + offset)
        List<CheckList> content = queryFactory
                .selectFrom(checkList)
                .where(checkList.content.contains(keyword))
                .orderBy(checkList.checkListId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회 (COUNT 쿼리)
        Long total = queryFactory
                .select(checkList.count())
                .from(checkList)
                .where(checkList.content.contains(keyword))
                .fetchOne();

        // 3. Page 생성
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
```

---

## 예시 2: Post 조회 (Slice 사용 - "더보기" 버튼)

### 1) Controller

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Post 목록 조회 (Slice - COUNT 쿼리 없음)
     * GET /posts?page=0&size=10
     */
    @GetMapping
    public Slice<PostResponse> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return postService.getPosts(pageRequest);
    }
}
```

### 2) Service

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public Slice<PostResponse> getPosts(Pageable pageable) {
        // 1. size + 1개 조회 (다음 페이지 존재 여부 확인용)
        List<Post> posts = postRepository.findPosts(
                pageable.getOffset(),
                pageable.getPageSize() + 1
        );

        // 2. SliceImpl 생성
        Slice<Post> postSlice = SliceImpl.of(posts, pageable);

        // 3. Entity -> DTO 변환
        return postSlice.map(PostResponse::from);
    }
}
```

### 3) Repository

```java
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Slice용 조회 (size + 1개 조회)
     */
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findPosts(@Param("offset") long offset, @Param("limit") int limit);
}
```

또는 **Native Query**:

```java
@Query(value = "SELECT * FROM post ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
       nativeQuery = true)
List<Post> findPosts(@Param("offset") long offset, @Param("limit") int limit);
```

---

## 📊 응답 예시

### Page 응답 (CheckList)

**요청:**
```
GET /admin/checklists?page=0&size=5
```

**응답:**
```json
{
  "content": [
    {"checkListId": 1, "content": "항목 1"},
    {"checkListId": 2, "content": "항목 2"},
    {"checkListId": 3, "content": "항목 3"},
    {"checkListId": 4, "content": "항목 4"},
    {"checkListId": 5, "content": "항목 5"}
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "offset": 0
  },
  "totalPages": 10,       // 전체 페이지 수
  "totalElements": 47,    // 전체 데이터 개수
  "numberOfElements": 5,  // 현재 페이지 데이터 개수
  "size": 5,
  "number": 0,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Slice 응답 (Post)

**요청:**
```
GET /posts?page=0&size=10
```

**응답:**
```json
{
  "content": [
    {"postId": 1, "title": "게시글 1", "content": "내용 1"},
    {"postId": 2, "title": "게시글 2", "content": "내용 2"},
    ...
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "hasNext": true,        // 다음 페이지 있음
  "hasPrevious": false
}
```

---

## 🔄 Page vs Slice 비교

| 특징 | Page | Slice |
|------|------|-------|
| COUNT 쿼리 | ✅ 실행 | ❌ 실행 안함 |
| 전체 페이지 수 | ✅ 제공 | ❌ 제공 안함 |
| 전체 데이터 개수 | ✅ 제공 | ❌ 제공 안함 |
| 다음 페이지 여부 | ✅ 제공 | ✅ 제공 |
| 성능 | 🔸 COUNT 쿼리로 느림 | ✅ 빠름 |
| 사용 케이스 | 페이지 번호 UI | 더보기 버튼 |

---

## 🎨 프론트엔드 UI 예시

### Page 사용 - 페이지네이션 버튼

```jsx
function CheckListPagination({ page }) {
  const [currentPage, setCurrentPage] = useState(0);
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch(`/admin/checklists?page=${currentPage}&size=5`)
      .then(res => res.json())
      .then(setData);
  }, [currentPage]);

  return (
    <div>
      <ul>
        {data?.content.map(item => (
          <li key={item.checkListId}>{item.content}</li>
        ))}
      </ul>

      {/* 페이지네이션 버튼 */}
      <div>
        <button
          disabled={data?.first}
          onClick={() => setCurrentPage(currentPage - 1)}
        >
          이전
        </button>

        {/* 페이지 번호 */}
        {Array.from({ length: data?.totalPages }, (_, i) => (
          <button
            key={i}
            onClick={() => setCurrentPage(i)}
            style={{ fontWeight: i === currentPage ? 'bold' : 'normal' }}
          >
            {i + 1}
          </button>
        ))}

        <button
          disabled={data?.last}
          onClick={() => setCurrentPage(currentPage + 1)}
        >
          다음
        </button>
      </div>

      {/* 정보 표시 */}
      <p>
        {currentPage + 1} / {data?.totalPages} 페이지
        (전체 {data?.totalElements}개)
      </p>
    </div>
  );
}
```

### Slice 사용 - 더보기 버튼

```jsx
function PostList() {
  const [currentPage, setCurrentPage] = useState(0);
  const [posts, setPosts] = useState([]);
  const [hasNext, setHasNext] = useState(false);

  const loadMore = () => {
    fetch(`/posts?page=${currentPage}&size=10`)
      .then(res => res.json())
      .then(data => {
        setPosts([...posts, ...data.content]);
        setHasNext(data.hasNext);
        setCurrentPage(currentPage + 1);
      });
  };

  useEffect(() => {
    loadMore();
  }, []);

  return (
    <div>
      <ul>
        {posts.map(post => (
          <li key={post.postId}>{post.title}</li>
        ))}
      </ul>

      {hasNext && (
        <button onClick={loadMore}>더보기</button>
      )}
    </div>
  );
}
```

---

## ⚡ 성능 최적화 팁

### 1. 인덱스 생성
```sql
-- 정렬 컬럼에 인덱스 생성
CREATE INDEX idx_checklist_id ON checklist(check_list_id);
CREATE INDEX idx_post_created_at ON post(created_at DESC, post_id DESC);
```

### 2. COUNT 쿼리 캐싱
```java
@Cacheable("checkListCount")
public long getCheckListCount() {
    return checkListRepository.count();
}
```

### 3. Slice 사용 고려
- 전체 페이지 수가 필요 없으면 Slice 사용
- COUNT 쿼리를 실행하지 않아 성능 향상

### 4. 페이지 크기 제한
```java
public static PageRequest of(int page, int size) {
    if (size > 100) {
        throw new IllegalArgumentException("Page size must not exceed 100");
    }
    return new PageRequest(page, size, null);
}
```

---

## 🚀 CheckList 완전한 구현 예시

### Controller
```java
@RestController
@RequestMapping("/admin/checklists")
@RequiredArgsConstructor
public class CheckListController {

    private final CheckListService checkListService;

    @GetMapping
    public Page<CheckListResponse> getCheckLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        if (size > 20) {
            size = 20; // 최대 20개로 제한
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        return checkListService.getCheckLists(pageRequest);
    }
}
```

### Service
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckListServiceImpl implements CheckListService {

    private final CheckListRepository checkListRepository;

    @Override
    public Page<CheckListResponse> getCheckLists(Pageable pageable) {
        Page<CheckList> checkListPage = checkListRepository.findAll(pageable);
        return checkListPage.map(CheckListResponse.Converter::from);
    }
}
```

### Repository
```java
public interface CheckListRepository extends JpaRepository<CheckList, Long> {
    // findAll(Pageable) 사용 - 별도 구현 불필요
}
```

완료! 이제 CheckList를 5개씩 페이지네이션으로 조회할 수 있습니다.

---

## 📌 요약

- **CheckList**: `Page` 사용 (5개씩, 페이지 번호 표시)
- **Post**: `Page` 또는 `Slice` 선택 (전체 개수 필요 여부에 따라)
- **대용량 데이터**: Cursor 기반 페이지네이션 고려 (`CURSOR_PAGINATION_USAGE.md` 참고)
