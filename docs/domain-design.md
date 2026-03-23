# 콘서트 예약 서비스 도메인 설계

## 1. 문서 목적

이 문서는 콘서트 예약 서비스의 핵심 도메인 모델, 역할, 관계, 상태 흐름을 정의한다.

설계 목표는 아래와 같다.

- 좌석 상태를 단일 기준으로 관리한다.
- 예약 이력과 결제 이력을 분리해 추적 가능성을 높인다.
- 대기열, 좌석, 결제의 책임을 명확히 분리한다.

현재 구현 단계 메모:

- 로드맵 3단계에서 상태 enum, 도메인 예외, 최소 도메인 모델이 코드에 반영되었다.
- 다만 아직 상태 전이 메서드와 본격적인 도메인 정책 조합은 구현하지 않았다.
- 현재 도메인 코드는 구조와 개념을 고정하는 역할에 집중한다.

## 2. 핵심 도메인 모델

### 2.1 User

역할:

- 사전 등록된 사용자
- 잔액, 대기열 토큰, 예약, 결제의 주체

주요 속성:

- `id`
- `uuid`

### 2.2 Concert

역할:

- 예약 대상이 되는 콘서트
- 콘서트별 대기열의 기준 단위

주요 속성:

- `id`
- `name`

### 2.3 ConcertSchedule

역할:

- 콘서트의 예약 가능한 날짜 단위
- 본 프로젝트에서는 날짜가 곧 회차다

주요 속성:

- `id`
- `concertId`
- `concertDate`

설명:

- 한 공연장에서는 하루 하나의 콘서트만 있다고 가정하므로 날짜 충돌 정책은 단순화한다.

### 2.4 QueueToken

역할:

- 콘서트별 대기열 관리
- 좌석 예약과 결제 API 접근 권한 제어

주요 속성:

- `id`
- `token`
- `userId`
- `concertId`
- `queuePosition`
- `status`
- `issuedAt`
- `activatedAt`
- `expiredAt`

상태:

- `WAITING`
- `ACTIVE`
- `USED`
- `EXPIRED`

현재 코드 반영:

- `QueueToken` 최소 도메인 모델과 `QueueTokenStatus` enum을 분리해 반영했다.
- `QueueTokenNotActiveException`을 별도 도메인 예외로 정의했다.

### 2.5 UserBalance

역할:

- 사용자 현재 잔액 보관
- 결제 시 차감 대상

주요 속성:

- `userId`
- `amount`
- `version`

설명:

- `version` 필드를 이용해 낙관적 락을 기본 적용한다.

현재 코드 반영:

- `UserBalance` 최소 도메인 모델과 `InsufficientBalanceException`, `OptimisticLockConflictException` 골격을 반영했다.
- 실제 `version` 기반 충돌 처리 로직은 아직 구현하지 않았다.

### 2.6 BalanceTransaction

역할:

- 잔액 변경 이력 관리
- 충전과 사용 내역 추적

주요 속성:

- `id`
- `userId`
- `type`
- `amount`
- `relatedPaymentId`
- `createdAt`

상태 또는 유형:

- `CHARGE`
- `USE`

### 2.7 SeatInventory

역할:

- 회차별 좌석의 현재 상태를 관리하는 단일 기준 엔티티
- 좌석 중복 선점 방지의 핵심 엔티티

주요 속성:

- `id`
- `scheduleId`
- `seatNumber`
- `status`
- `heldByUserId`
- `holdExpiresAt`
- `reservedByUserId`
- `version`

상태:

- `AVAILABLE`
- `HELD`
- `RESERVED`

현재 코드 반영:

- `SeatStatus` enum을 먼저 도입해 좌석 상태 개념을 코드에 고정했다.
- 실제 `SeatInventory` 상태 전이와 조건부 업데이트는 다음 단계에서 persistence와 함께 구현한다.

설명:

- 좌석 조회와 선점 가능 여부 판단은 이 엔티티를 기준으로 한다.
- 회차별 좌석 번호는 1부터 50까지 고정이다.

### 2.8 Reservation

역할:

- 좌석 예약 시도의 이력과 상태 관리
- 임시 배정과 확정 상태 추적

주요 속성:

- `id`
- `userId`
- `scheduleId`
- `seatNumber`
- `status`
- `reservedAt`
- `expiresAt`

상태:

- `TEMPORARY`
- `CONFIRMED`
- `EXPIRED`

설명:

- 현재 좌석 상태는 `SeatInventory`가 관리하고, 예약 이력은 `Reservation`이 관리한다.

현재 코드 반영:

- `Reservation` 최소 도메인 모델과 `ReservationStatus` enum을 도입했다.
- `ReservationExpiredException`, `UserAlreadyHasHeldSeatException`을 별도 예외로 정의했다.

### 2.9 Payment

역할:

- 결제 결과 기록
- 결제 성공 시 예약 및 좌석 확정과 연결

주요 속성:

- `id`
- `userId`
- `reservationId`
- `amount`
- `status`
- `paidAt`

상태:

- `SUCCESS`
- `FAILED`

현재 코드 반영:

- `Payment` 최소 도메인 모델과 `PaymentStatus` enum을 추가했다.

## 3. 도메인 관계

핵심 관계는 아래와 같다.

- `User` 1:N `QueueToken`
- `User` 1:1 `UserBalance`
- `User` 1:N `BalanceTransaction`
- `User` 1:N `Reservation`
- `User` 1:N `Payment`
- `Concert` 1:N `ConcertSchedule`
- `Concert` 1:N `QueueToken`
- `ConcertSchedule` 1:N `SeatInventory`
- `ConcertSchedule` 1:N `Reservation`
- `Reservation` 1:1 `Payment`

## 4. 핵심 제약 조건

### 4.1 대기열 관련 제약

- 대기열은 콘서트 단위로 분리한다.
- 좌석 예약과 결제는 `ACTIVE` 상태 토큰만 허용한다.
- 콘서트별 `ACTIVE` 토큰 수는 최대 슬롯 수를 넘을 수 없다.
- `WAITING` 토큰 승격은 가장 앞선 대기순번부터 수행한다.
- 결제 완료 후 토큰은 `USED` 또는 `EXPIRED`로 전이되어야 한다.

### 4.2 좌석 관련 제약

- 회차별 좌석 번호는 1부터 50까지다.
- 같은 회차의 같은 좌석은 동시에 한 사용자만 임시 배정 또는 확정할 수 있다.
- 임시 배정은 약 5분 유효하다.
- 한 사용자는 동시에 하나의 임시 배정만 가질 수 있다.

### 4.3 결제 관련 제약

- 결제 금액은 고정 좌석 가격이다.
- 결제는 본인이 임시 배정한 좌석에 대해서만 가능하다.
- 결제 시점에 예약과 좌석 상태가 여전히 유효해야 한다.

## 5. 상태 흐름

### 5.1 대기열 토큰 흐름

1. 토큰 발급 시 `WAITING`
2. 콘서트별 활성 슬롯이 비면 가장 앞선 `WAITING` 토큰이 `ACTIVE`
3. 결제 성공 시 `USED`
4. 유효시간 경과 또는 정책상 종료 시 `EXPIRED`

추가 규칙:

- 콘서트별 `ACTIVE` 슬롯 최대 수는 설정값으로 관리한다.
- `ACTIVE` 슬롯이 비는 조건은 `USED`, `EXPIRED`, 운영 정책에 의한 강제 종료다.
- 승격은 대기번호 조회, 토큰 검증 직전 보정 로직, 스케줄러 중 하나에서 수행할 수 있다.
- 어떤 경로로 승격을 시도하더라도 최종 승격 성공은 트랜잭션 안에서 다시 판정한다.

주의:

- 현재 단계에서는 위 상태 흐름을 설명하는 enum과 예외만 반영했다.
- 실제 승격 로직과 상태 전이 메서드는 아직 구현하지 않았다.

### 5.2 좌석 흐름

1. 초기 상태 `AVAILABLE`
2. 예약 요청 성공 시 `HELD`
3. 결제 성공 시 `RESERVED`
4. 결제 없이 만료되면 `AVAILABLE`

### 5.3 예약 흐름

1. 좌석 임시 배정 시 `TEMPORARY`
2. 결제 성공 시 `CONFIRMED`
3. 결제 미완료로 만료 시 `EXPIRED`

## 6. 주요 처리 시나리오

### 6.1 좌석 예약 요청

1. 필요 시 토큰 승격 보정 로직을 먼저 수행한다.
2. 사용자의 콘서트별 토큰이 `ACTIVE`인지 확인한다.
3. 사용자가 이미 다른 임시 배정을 보유 중인지 확인한다.
4. 대상 좌석이 `AVAILABLE`이거나 기존 홀드가 만료된 상태인지 확인한다.
5. 좌석을 `HELD`로 전환하고 만료 시각을 기록한다.
6. 예약 이력을 `TEMPORARY` 상태로 생성한다.

### 6.2 결제 요청

1. 필요 시 토큰 승격 보정 로직을 먼저 수행한다.
2. 사용자의 콘서트별 토큰이 `ACTIVE`인지 확인한다.
3. 예약이 본인 소유이며 아직 만료되지 않았는지 확인한다.
4. 좌석이 본인에게 임시 배정된 상태인지 확인한다.
5. 잔액이 충분한지 확인하고 차감한다.
6. 결제 내역을 생성한다.
7. 좌석을 `RESERVED`로 변경한다.
8. 예약을 `CONFIRMED`로 변경한다.
9. 토큰을 `USED` 또는 `EXPIRED`로 변경한다.

## 7. 구현 우선순위

### 7.1 1순위

- `SeatInventory`, `Reservation`, `UserBalance` 중심의 핵심 정합성 모델부터 구현한다.

### 7.2 2순위

- 좌석 임시 배정과 결제 확정 트랜잭션을 구현한다.

### 7.3 3순위

- 대기열 토큰과 콘서트별 대기번호 조회를 구현한다.

### 7.4 4순위

- 조회 API와 만료 스케줄러를 구현한다.

### 7.5 5순위

- 단위 테스트와 동시성 테스트를 보강한다.
