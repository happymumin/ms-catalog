# Catalog (무신사-전시 백앤드 과제)

## 목차

1. [시스템 설계 시 고려한 점](#1-시스템-설계-시-고려한-점)
2. [기술스택](#2-기술스택)
3. [데이터베이스 설계](#3-데이터베이스-설계)
4. [애플리케이션 실행](#4-어플리케이션-실행)
5. [Swagger](#5-swagger)
6. [API 실행 예시](#6-api-실행-예시)

## 1. 시스템 설계 시 고려한 점

### **1. 무신사의 카테고리 시스템이라고 가정**

- 무신사 카테고리 시스템을 모델링한다는 가정을 전제로 설계
- 단순 카테고리 관리뿐 아니라 실제 서비스 맥락에서 확장 가능한 구조를 고려

### **2. 확장성 (메타 정보 관리)**

- 카테고리 외에도 브랜드, 상품 옵션 등 다양한 메타 정보를 함께 관리해야 한다는 가정
- 이 가정을 바탕으로 프로젝트명을 `catalog`로 정의하고 패키지 구조를 DDD(Domain-Driven Design) 스타일로 설계

### **3. 조회/갱신 패턴**

- 카테고리는 변경 빈도가 낮고, 조회 빈도는 서비스 및 내부 MSA 환경(상품, 추천, 광고 등)에서 사용하기 때문에 높다고 가정
    - 조회
        - 조회가 높다는 전제를 바탕으로 조회 성능 최적화를 위해 Redis 캐싱 적용
    - 갱신
        - 갱신 시 동시성 이슈를 방지하기 위해 `modified_at` 컬럼을 활용한 낙관적 Lock(Optimistic Lock)을 도입
        - 불일치 시 충돌로 처리하고 트랜잭션을 중단하여 데이터 정합성을 보장

#### 3.2 조회 로직 고도화 대안

##### 3.2.1 Local Cache

- 카테고리 변경 빈도가 좀 더 낮다고 가정한다면 Redis 대신 Local Cache 사용 가능
    - 예: Caffeine, Ehcache
- 캐시 무효화 전략:
    - eventual consistency 허용 시 Redis Pub/Sub 기반 무효화
    - 더 엄격한 일관성이 필요하다면 Kafka, Kinesis 등 이벤트 시스템(queue) 기반 무효화 적용
        - 이벤트 재처리 지원으로 Pub/Sub보다 안정적

##### 3.2.2 WebFlux

- 요청이 좀 더 많다고 가정한다면 Spring MVC 대신 Spring WebFlux를 도입하는 방안 고려

## 2. 기술스택

- **Backend**: Spring Boot 3.5.5, Java 17, Kotlin 1.9.25
- **Build Tool**: Gradle
- **Database**: H2 (In-Memory), Redis
- **ORM**: Spring Data JPA
- **Testing**: JUnit 5

## 3. 데이터베이스 설계

### category

| Column        | Type                     | Nullable | Constraints  | Description           |
|---------------|--------------------------|----------|--------------|-----------------------|
| `id`          | INT (PK, AUTO_INCREMENT) | NO       | PRIMARY KEY  | 카테고리 ID               |
| `parent_id`   | INT                      | YES      |              | 부모 카테고리 ID (루트는 NULL) |
| `code`        | VARCHAR (`CategoryCode`) | NO       | UNIQUE       | 카테고리 코드               |
| `name`        | VARCHAR                  | NO       |              | 카테고리 이름               |
| `enabled`     | BOOLEAN                  | NO       | DEFAULT TRUE | 카테고리 활성 여부            |
| `created_at`  | TIMESTAMP                | NO       |              | 생성 일시                 |
| `modified_at` | TIMESTAMP                | NO       |              | 수정 일시                 |

### 고려한 점

1. **parent_id 단독 사용의 한계**
    1. `parent_id`만으로 계층 구조를 표현하면, 특정 카테고리의 하위 카테고리를 조회할 때 재귀 쿼리를 작성해야 하는 불편함이 있음.
    2. 또한 DB 테이블만 보았을 때 계층 구조가 한눈에 드러나지 않아 파악하기 어려움.
2. **code 도입의 장단점**
    1. 장점:
        1. 테이블만 보더라도 계층 구조가 직관적으로 드러남.
        2. 조회 시 `LIKE` 문으로 손쉽게 하위 카테고리를 조회 가능.
    2. 단점: 부모 카테고리가 변경되면 하위 카테고리의 `code`도 모두 갱신해야 함.
3. **ID를 code로 관리 시 문제점**
    1. 만약 `id`를 계층형 `code`로 관리하면, 계층이 변경될 때 상품이 참조하는 카테고리 ID까지 모두 갱신해야 하는 불편함 발생.

### 최종 선택한 방법

1. **id**
    1. 불변(immutable) 값으로 관리하여 카테고리 구조 변경 시에도 상품에는 영향이 없도록 함.
2. **parent_id**
    1. 트리 구조 표현 및 계층 이동 용도.
3. **code**
    1. code의 단점보다 장점을 좀 더 높이 사고, code를 도입하기로 결정함.
    2. depth별 3자리 숫자로 구성 (예: `100`, `200`, `300`)
    3. `parent_id`의 code + 현재 카테고리의 고유 code를 조합하여 관리
        1. 예: 부모 카테고리 `100`(CategorySegmentCode), 자식 카테고리 `200`(CategorySegmentCode) → 최종 code = `100200`(CategoryCode)
        2. 계층 구조를 직관적으로 드러내고, `LIKE` 조회 최적화에 활용

```sql
-- '상의(100200)' 카테고리의 하위 카테고리 전체 조회
SELECT *
FROM category
WHERE code LIKE '100200%';
```

#### 샘플 데이터 예시

| id | parent_id | code      | name  | enabled | created_at          | modified_at         |
|----|-----------|-----------|-------|---------|---------------------|---------------------|
| 1  | NULL      | 100       | 남성 의류 | true    | 2025-09-18 10:00:00 | 2025-09-18 10:00:00 |
| 2  | 1         | 100200    | 상의    | true    | 2025-09-18 10:01:00 | 2025-09-18 10:01:00 |
| 3  | 2         | 100200300 | 티셔츠   | true    | 2025-09-18 10:02:00 | 2025-09-18 10:02:00 |

## 4. 어플리케이션 실행

### 1. Redis 환경 구성이 되어있는 경우

```bash
./gradlew bootRun --args='--spring.profiles.active=dev,redis'
```

- Redis 기본 설정 정보
    - host: localhost
    - port: 6379
- Redis 설정 정보 변경 필요시, `application-redis.yml` 나 property를 수정 후 실행해주세요.

### 2. Redis 환경 구성이 안되어있는 경우

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

- 캐싱 기능이 동작하지 않습니다. (`NoOpCacheService` 참고)

## 5. Swagger

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 6. API 실행 예시

### 카테고리 등록

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/categories' \
  -H 'Content-Type: application/json' \
  -d '{
  "code": "100",
  "name": "여성의류",
  "parentId": null
}'
```

### 카테고리 수정

```bash
curl -X 'PUT' \
  'http://localhost:8080/api/v1/categories/1' \
  -H 'Content-Type: application/json' \
  -d '{
  "code": "100",
  "name": "남성의류",
  "parentId": null
}'
```

### 카테고리 조회

```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/categories'
 ```

### 카테고리 삭제

```bash
curl -X 'DELETE' \
  'http://localhost:8080/api/v1/categories/1' 
```
