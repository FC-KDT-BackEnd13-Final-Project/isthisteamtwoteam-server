# Spring Boot 프로젝트 하드 딜리트: 트랜잭션과 외부 시스템의 조화

## 들어가며

프로젝트를 진행하다 보면 데이터 삭제는 필수적인 기능입니다. 특히 GDPR과 같은 개인정보 보호 규정이 강화되면서 "진짜 삭제"(Hard Delete)의 중요성이 더욱 커지고 있습니다. 하지만 단순해 보이는 삭제 기능도 DB와 S3 같은 외부 시스템이 함께 관여하면 예상치 못한 문제가 발생할 수 있습니다.

이 글에서는 실제 프로젝트에서 발견한 하드 딜리트 구현의 문제점과 해결 과정을 공유합니다.

## 문제 발견

프로젝트의 영구 삭제 기능을 리뷰하던 중, 다음과 같은 코드를 발견했습니다:

```java
@Override
@Transactional
public ProjectPermanentDeleteResponse deleteDeletedProject(
    Long loginUserId,
    @Valid ProjectPermanentDeleteRequest request) {

    // 1-3. 검증 로직
    List<Long> projectIds = request.getProjectIds();
    List<Project> projects = projectRepository.findAllById(projectIds);

    // 4. S3와 DB에서 파일(게시글, 댓글, 체크리스트) 삭제
    List<File> files = fileRepository.findByProjectIds(projectIds);

    // 5. DB 삭제 먼저
    projectRepository.deleteAll(projects);
    projectRepository.flush();

    // 6. S3 삭제
    fileService.deleteFilesFromS3(files);

    // 7. 프로젝트 삭제 (Cascade로 연관 엔티티도 삭제)
    projectRepository.deleteAll(projects);  // ⚠️ 중복!

    return ProjectPermanentDeleteResponse.Converter.from(projects);
}
```

## 문제점 분석

### 1. 중복 삭제 호출

```java
// 5단계
projectRepository.deleteAll(projects);  // 첫 번째 삭제
projectRepository.flush();

// ... S3 삭제 ...

// 7단계
projectRepository.deleteAll(projects);  // ⚠️ 두 번째 삭제
```

같은 엔티티를 두 번 삭제하고 있습니다.

**왜 문제인가?**
- 첫 번째 `deleteAll()` 후 `flush()`를 호출하면 영속성 컨텍스트의 변경사항이 DB에 즉시 반영됩니다
- 이후 엔티티는 detached 상태가 되어 두 번째 삭제는 무의미하거나 에러를 유발할 수 있습니다
- 불필요한 DB 쿼리로 성능 저하

### 2. 트랜잭션 일관성 문제

더 심각한 문제는 **삭제 순서**입니다.

```java
// 현재 코드: DB 먼저 삭제
projectRepository.deleteAll(projects);
projectRepository.flush();  // DB에 즉시 반영

// S3 삭제가 실패하면?
fileService.deleteFilesFromS3(files);  // ❌ 예외 발생!
```

**시나리오:**
1. DB에서 프로젝트와 파일 정보 삭제 완료
2. `flush()`로 DB에 즉시 반영
3. S3 삭제 시도 중 네트워크 오류 발생
4. 예외가 발생하지만... **DB는 이미 삭제됨**

**결과:**
- DB: 데이터 없음 ✅
- S3: 파일 남아있음 ❌
- 고아 파일(Orphan Files) 발생 → 스토리지 비용 증가, 데이터 불일치

## 해결 방안

### 핵심 원칙: "롤백 불가능한 작업을 먼저"

트랜잭션과 외부 시스템을 함께 다룰 때는 다음 원칙을 따라야 합니다:

> **롤백할 수 없는 작업(S3)을 먼저 실행하고, 롤백 가능한 작업(DB)을 나중에 실행한다**

### 왜 이 순서가 안전한가?

#### 케이스 1: S3 삭제 실패
```java
// S3 먼저 삭제
fileService.deleteFilesFromS3(files);  // ❌ 실패!
// 예외 발생 → @Transactional에 의해 롤백

// DB 삭제는 실행되지 않음
projectRepository.deleteAll(projects);  // 실행 안 됨

// 결과: 아무것도 삭제되지 않음
// DB: 데이터 유지 ✅
// S3: 파일 유지 ✅
// → 데이터 일관성 유지!
```

#### 케이스 2: S3 성공, DB 실패
```java
// S3 삭제 성공
fileService.deleteFilesFromS3(files);  // ✅ 성공

// DB 삭제 실패
projectRepository.deleteAll(projects);  // ❌ 실패!
// @Transactional에 의해 롤백

// 결과:
// DB: 데이터 유지 ✅
// S3: 파일 삭제됨 ❌
// → S3 파일만 사라짐
```

두 번째 케이스도 문제가 있지만, 첫 번째 순서보다는 낫습니다:
- **고아 파일** (DB는 없는데 S3에 파일) vs **누락 파일** (DB는 있는데 S3에 파일 없음)
- 고아 파일은 찾기 어렵고 비용 발생, 누락 파일은 에러 메시지로 즉시 발견 가능

### 개선된 코드

```java
@Override
@Transactional
public ProjectPermanentDeleteResponse deleteDeletedProject(
    Long loginUserId,
    @Valid ProjectPermanentDeleteRequest request) {

    // 1. 삭제 요청한 프로젝트 ID 목록 조회
    List<Long> projectIds = request.getProjectIds();
    List<Project> projects = projectRepository.findAllById(projectIds);

    // 2. 존재 개수 비교
    if (projects.size() != projectIds.size()) {
        throw new ProjectNotFoundException("존재하지 않는 프로젝트가 포함되어 있습니다.");
    }

    // 3. 삭제 여부 체크
    projects.forEach(project -> {
        if (!project.getIsDeleted()) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_DELETED);
        }
    });

    // 4. S3에서 파일 먼저 삭제 (실패 시 트랜잭션 롤백)
    List<File> files = fileRepository.findByProjectIds(projectIds);
    fileService.deleteFilesFromS3(files);

    // 5. DB에서 프로젝트 삭제 (Cascade로 연관 엔티티도 삭제)
    projectRepository.deleteAll(projects);

    return ProjectPermanentDeleteResponse.Converter.from(projects);
}
```

### 주요 변경사항

1. ✅ **중복 삭제 제거**: `deleteAll()` 한 번만 호출
2. ✅ **순서 변경**: S3 삭제 → DB 삭제
3. ✅ **불필요한 flush() 제거**: 트랜잭션 종료 시 자동으로 커밋됨
4. ✅ **Cascade 활용**: Project 삭제 시 연관된 File, Memo 등 자동 삭제

## 추가 고려사항

### 보상 트랜잭션 (Saga Pattern)

더 완벽한 해결책을 원한다면 보상 트랜잭션 패턴을 고려할 수 있습니다:

```java
@Override
@Transactional
public ProjectPermanentDeleteResponse deleteDeletedProject(...) {
    List<File> files = fileRepository.findByProjectIds(projectIds);

    try {
        // S3 삭제
        fileService.deleteFilesFromS3(files);

        // DB 삭제
        projectRepository.deleteAll(projects);

        return ProjectPermanentDeleteResponse.Converter.from(projects);

    } catch (S3Exception e) {
        // S3 삭제 실패 시 롤백 (트랜잭션이 자동 롤백)
        throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, e);

    } catch (DataAccessException e) {
        // DB 삭제 실패 시 S3 복구 시도
        try {
            fileService.restoreFilesToS3(files);
        } catch (Exception restoreEx) {
            log.error("S3 복구 실패, 수동 정리 필요", restoreEx);
        }
        throw new BusinessException(ErrorCode.PROJECT_DELETE_FAILED, e);
    }
}
```

### 배치 작업으로 분리

매우 중요한 데이터라면 삭제를 2단계로 분리할 수도 있습니다:

1. **즉시**: DB에서만 삭제 표시 (deleted_at 업데이트)
2. **배치**: 일정 시간 후 배치 작업으로 S3 파일 정리

```java
// 즉시 실행: Soft delete로 변경
project.markAsDeleted();

// 배치 작업 (매일 새벽 실행)
@Scheduled(cron = "0 0 2 * * *")
public void cleanupOrphanedFiles() {
    // 삭제된 지 7일 이상 지난 프로젝트의 파일들 정리
    List<File> orphanedFiles = fileRepository.findOrphanedFiles(7);
    fileService.deleteFilesFromS3(orphanedFiles);
}
```

## 결론

Spring Boot의 `@Transactional`은 강력하지만, **외부 시스템(S3, Redis, 외부 API 등)은 트랜잭션 범위 밖**이라는 점을 항상 기억해야 합니다.

### 핵심 원칙 정리

1. **롤백 불가능한 작업을 먼저** - S3 → DB 순서
2. **중복 작업 제거** - 한 번만 삭제
3. **Cascade 활용** - JPA가 연관 엔티티 자동 처리
4. **실패 시나리오 고려** - 어느 단계에서 실패해도 데이터 일관성 유지

### 병렬 처리는 어떨까?

"S3와 DB를 병렬로 삭제하면 빠르지 않을까?"라고 생각할 수 있지만:

❌ **병렬 처리 불가능**
- S3와 DB는 같은 데이터를 다루는 **의존적인 작업**
- 실패 시 롤백 및 복구 로직이 필요
- 트랜잭션 일관성을 보장할 수 없음

✅ **병렬 처리 가능한 경우**
- 독립적인 여러 파일 읽기
- 서로 다른 프로젝트의 동시 삭제
- 통계 집계와 리포트 생성

---

이 글이 트랜잭션과 외부 시스템을 다루는 분들에게 도움이 되길 바랍니다. 궁금한 점이나 더 나은 방법이 있다면 댓글로 공유해 주세요!

## 참고 자료

- [Spring Framework Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Saga Pattern for Distributed Transactions](https://microservices.io/patterns/data/saga.html)
- [JPA Cascade Types Explained](https://www.baeldung.com/jpa-cascade-types)