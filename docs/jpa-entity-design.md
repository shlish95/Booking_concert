# 콘서트 예약 서비스 JPA 엔티티 설계

## 1. 문서 목적

이 문서는 ERD 초안을 기준으로 JPA 엔티티 설계 방향, 주요 필드, 매핑 원칙을 정리한다.

핵심 원칙은 아래와 같다.

- JPA 엔티티는 연관관계를 최소화한다.
- 엔티티는 객체 그래프보다 FK ID 중심으로 설계한다.
- DB 외래키 제약은 두지 않고, 참조 무결성은 애플리케이션 서비스와 도메인 규칙에서 검증한다.
- 핵심 정합성은 `SeatInventory`, `Reservation`, `UserBalance` 중심으로 관리한다.

## 2. 공통 설계 원칙

### 2.1 기본 원칙

- 엔티티는 aggregate root 중심으로 조회하고 수정한다.
- 다른 엔티티 참조는 가능하면 `xxxId` 필드로 표현한다.
- 상태 값은 문자열 기반 enum 매핑을 사용한다.
- 생성 시각과 수정 시각은 공통 베이스 엔티티로 관리할 수 있다.
- 복잡한 객체 그래프 탐색 대신 명시적 조회를 사용한다.

### 2.2 권장 공통 컬럼

- `createdAt`
- `updatedAt`

### 2.3 권장 공통 설정

- `@Enumerated(EnumType.STRING)`
- `@Version` for optimistic locking
- `@Table(indexes = ...)`

## 3. 엔티티별 설계

### 3.1 User

역할:

- 사전 생성된 사용자

권장 필드:

```java
Long id;
String uuid;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

비고:

- 다른 엔티티에서 `userId`로 논리 참조한다.

### 3.2 Concert

역할:

- 콘서트 기본 정보 및 고정 좌석 가격 관리

권장 필드:

```java
Long id;
String name;
Long seatPrice;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

비고:

- 다른 엔티티에서 `concertId`로 논리 참조한다.

### 3.3 ConcertSchedule

역할:

- 날짜 단위 회차 정보 관리

권장 필드:

```java
Long id;
Long concertId;
LocalDate concertDate;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 제약:

- `(concert_id, concert_date)` unique

비고:

- `concertId`는 `Concert`에 대한 참조 ID다.

### 3.4 QueueToken

역할:

- 콘서트별 대기열 상태 관리

권장 필드:

```java
Long id;
String token;
Long userId;
Long concertId;
Long queuePosition;
QueueTokenStatus status;
LocalDateTime issuedAt;
LocalDateTime activatedAt;
LocalDateTime expiredAt;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 enum:

```java
WAITING, ACTIVE, USED, EXPIRED
```

비고:

- 예약과 결제 API에서만 사용한다.
- `userId`, `concertId`는 각각 사용자와 콘서트에 대한 참조 ID다.

### 3.5 UserBalance

역할:

- 사용자 현재 잔액 관리

권장 필드:

```java
Long userId;
Long amount;
Long version;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 설정:

```java
@Id
private Long userId;

@Version
private Long version;
```

비고:

- `userId`를 PK로 사용한다.
- 낙관적 락 기본 적용 대상이다.

### 3.6 BalanceTransaction

역할:

- 충전 및 사용 이력 관리

권장 필드:

```java
Long id;
Long userId;
BalanceTransactionType type;
Long amount;
Long relatedPaymentId;
LocalDateTime createdAt;
```

권장 enum:

```java
CHARGE, USE
```

비고:

- `relatedPaymentId`는 결제 성공 건과의 논리 참조 ID다.

### 3.7 SeatInventory

역할:

- 좌석 현재 상태의 단일 기준

권장 필드:

```java
Long id;
Long scheduleId;
Integer seatNumber;
SeatStatus status;
Long heldByUserId;
LocalDateTime holdExpiresAt;
Long reservedByUserId;
Long version;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 enum:

```java
AVAILABLE, HELD, RESERVED
```

권장 설정:

```java
@Version
private Long version;
```

비고:

- `scheduleId`는 회차에 대한 참조 ID다.
- `heldByUserId`, `reservedByUserId`는 사용자에 대한 참조 ID다.
- 좌석 선점은 조건부 업데이트 쿼리로 처리한다.

### 3.8 Reservation

역할:

- 예약 상태 및 이력 관리

권장 필드:

```java
Long id;
Long userId;
Long scheduleId;
Integer seatNumber;
ReservationStatus status;
LocalDateTime reservedAt;
LocalDateTime expiresAt;
LocalDateTime confirmedAt;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 enum:

```java
TEMPORARY, CONFIRMED, EXPIRED
```

비고:

- `userId`, `scheduleId`는 논리 참조 ID다.
- 한 사용자의 활성 임시 배정 확인용 조회 기준이 된다.

### 3.9 Payment

역할:

- 결제 결과 관리

권장 필드:

```java
Long id;
Long userId;
Long reservationId;
Long amount;
PaymentStatus status;
LocalDateTime paidAt;
LocalDateTime createdAt;
LocalDateTime updatedAt;
```

권장 enum:

```java
SUCCESS, FAILED
```

권장 제약:

- `reservation_id` unique

비고:

- `reservationId`는 예약에 대한 논리 참조 ID다.

## 4. FK ID 중심 설계 가이드

### 4.1 기본 방향

- `ConcertSchedule`는 `concertId`를 가진다.
- `QueueToken`은 `userId`, `concertId`를 가진다.
- `UserBalance`는 `userId`를 PK로 가진다.
- `BalanceTransaction`은 `userId`, `relatedPaymentId`를 가진다.
- `SeatInventory`는 `scheduleId`, `heldByUserId`, `reservedByUserId`를 가진다.
- `Reservation`은 `userId`, `scheduleId`를 가진다.
- `Payment`는 `userId`, `reservationId`를 가진다.

### 4.2 장점

- 객체 그래프 로딩 비용을 줄이기 쉽다.
- 조건부 업데이트와 배치 정리에 유리하다.
- 외래키 없이도 애플리케이션 서비스에서 명시적으로 참조 무결성을 검증할 수 있다.
- 테스트에서 엔티티 준비가 단순하다.

### 4.3 주의점

- 참조 대상 존재 여부를 서비스에서 반드시 검증해야 한다.
- API 처리 흐름에서 필요한 엔티티는 명시적으로 추가 조회해야 한다.
- 잘못된 참조 ID 저장을 막기 위한 검증 로직이 누락되지 않아야 한다.

## 5. Repository 설계 초안

### 5.1 QueueTokenRepository

필요 메서드 예시:

- `Optional<QueueToken> findByToken(String token)`
- `Optional<QueueToken> findFirstByConcertIdAndUserIdOrderByIdDesc(...)`
- `List<QueueToken> findByConcertIdAndStatusOrderByQueuePositionAsc(...)`

### 5.2 SeatInventoryRepository

필요 메서드 예시:

- `Optional<SeatInventory> findByScheduleIdAndSeatNumber(...)`
- 조건부 업데이트 선점 메서드
- 만료 좌석 정리 메서드

예시 개념:

```java
@Modifying
@Query("""
update SeatInventory s
   set s.status = 'HELD',
       s.heldByUserId = :userId,
       s.holdExpiresAt = :expiresAt
 where s.scheduleId = :scheduleId
   and s.seatNumber = :seatNumber
   and (
       s.status = 'AVAILABLE'
       or (s.status = 'HELD' and s.holdExpiresAt < :now)
   )
""")
int holdSeat(...);
```

### 5.3 ReservationRepository

필요 메서드 예시:

- `boolean existsByUserIdAndStatusAndExpiresAtAfter(...)`
- `Optional<Reservation> findByIdAndUserId(...)`
- 만료 예약 정리 메서드

### 5.4 UserBalanceRepository

필요 메서드 예시:

- `Optional<UserBalance> findByUserId(Long userId)`
- 필요 시 비관적 락 비교용 메서드

### 5.5 PaymentRepository

필요 메서드 예시:

- `boolean existsByReservationId(Long reservationId)`
- `Optional<Payment> findByReservationId(Long reservationId)`

## 6. 보조 대안

- 일부 조회 최적화가 필요하면 제한적으로 엔티티 연관관계를 둘 수 있다.
- 다만 현재 기본안은 FK ID 중심 설계이며, 연관관계 매핑은 보조 대안일 뿐 기본 방향이 아니다.

## 7. 설계상 주의점

- `SeatInventory`와 `Reservation`의 책임을 섞지 않는다.
- 좌석 상태의 진실은 `SeatInventory`다.
- 결제 성공 판정은 조회 결과가 아니라 트랜잭션 내 상태 검증으로 결정한다.
- 낙관적 락 실패 처리 정책은 서비스 계층에서 명확히 정해야 한다.
