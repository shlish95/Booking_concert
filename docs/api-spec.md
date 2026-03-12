# 콘서트 예약 서비스 API 명세서

## 1. 문서 목적

이 문서는 콘서트 예약 서비스의 HTTP API 초안을 정리한다.

기준 정책은 아래와 같다.

- 토큰 검증은 좌석 예약과 결제 API에서만 수행한다.
- 대기열은 콘서트별로 관리한다.
- 날짜는 회차와 동일하다.
- 좌석 가격은 고정이다.

## 2. 공통 규칙

### 2.1 Base URL

```text
/api/v1
```

### 2.2 공통 헤더

- `Content-Type: application/json`

### 2.3 인증 및 토큰 규칙

- 일반 사용자 인증은 범위 외로 가정한다.
- 대기열 토큰은 좌석 예약과 결제 시에만 전달한다.
- 전달 방식은 헤더를 기본안으로 사용한다.

예시:

```text
X-Queue-Token: {token}
```

### 2.4 공통 응답 필드 예시

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

에러 응답 예시:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SEAT_ALREADY_HELD",
    "message": "이미 선점된 좌석입니다."
  }
}
```

## 3. API 목록

### 3.1 콘서트별 대기열 토큰 발급

메서드:

```text
POST /api/v1/concerts/{concertId}/queue-tokens
```

설명:

- 사용자를 콘서트별 대기열에 등록하고 토큰을 발급한다.

요청 본문:

```json
{
  "userId": 1
}
```

응답 예시:

```json
{
  "success": true,
  "data": {
    "token": "qt_123456789",
    "concertId": 10,
    "userId": 1,
    "queuePosition": 125,
    "status": "WAITING",
    "issuedAt": "2026-03-12T10:00:00"
  },
  "error": null
}
```

주요 검증:

- 사용자 존재 여부
- 콘서트 존재 여부

### 3.2 대기번호 조회

메서드:

```text
GET /api/v1/concerts/{concertId}/queue-tokens/{token}
```

설명:

- 특정 콘서트에 대한 대기열 상태를 조회한다.

응답 예시:

```json
{
  "success": true,
  "data": {
    "token": "qt_123456789",
    "concertId": 10,
    "userId": 1,
    "queuePosition": 125,
    "status": "WAITING",
    "activatedAt": null,
    "expiredAt": null
  },
  "error": null
}
```

### 3.3 예약 가능 날짜 조회

메서드:

```text
GET /api/v1/concerts/{concertId}/schedules
```

설명:

- 콘서트의 예약 가능한 날짜 목록을 조회한다.

응답 예시:

```json
{
  "success": true,
  "data": [
    {
      "scheduleId": 100,
      "concertDate": "2026-04-01"
    },
    {
      "scheduleId": 101,
      "concertDate": "2026-04-02"
    }
  ],
  "error": null
}
```

### 3.4 예약 가능 좌석 조회

메서드:

```text
GET /api/v1/schedules/{scheduleId}/seats/available
```

설명:

- 특정 회차에서 예약 가능한 좌석을 조회한다.

응답 예시:

```json
{
  "success": true,
  "data": {
    "scheduleId": 100,
    "availableSeats": [1, 2, 3, 7, 8, 9]
  },
  "error": null
}
```

주요 규칙:

- `AVAILABLE` 좌석을 조회한다.
- `HELD` 상태라도 이미 만료된 좌석은 예약 가능으로 간주할 수 있다.
- 실제 선점 가능 여부는 예약 API 트랜잭션에서 최종 판정한다.

### 3.5 좌석 예약 요청

메서드:

```text
POST /api/v1/reservations
```

헤더:

```text
X-Queue-Token: qt_123456789
```

설명:

- 좌석을 약 5분간 임시 배정한다.

요청 본문:

```json
{
  "userId": 1,
  "scheduleId": 100,
  "seatNumber": 12
}
```

응답 예시:

```json
{
  "success": true,
  "data": {
    "reservationId": 5001,
    "userId": 1,
    "scheduleId": 100,
    "seatNumber": 12,
    "status": "TEMPORARY",
    "expiresAt": "2026-03-12T10:05:00"
  },
  "error": null
}
```

주요 검증:

- 토큰 존재 여부
- 토큰이 해당 콘서트의 `ACTIVE` 상태인지
- 사용자가 이미 다른 임시 배정을 가지고 있지 않은지
- 좌석이 현재 선점 가능한 상태인지

주요 에러 코드 예시:

- `QUEUE_TOKEN_INVALID`
- `QUEUE_TOKEN_NOT_ACTIVE`
- `USER_ALREADY_HAS_HELD_SEAT`
- `SEAT_ALREADY_HELD`
- `SEAT_ALREADY_RESERVED`

### 3.6 잔액 충전

메서드:

```text
POST /api/v1/users/{userId}/balance/charge
```

요청 본문:

```json
{
  "amount": 100000
}
```

응답 예시:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "balance": 150000
  },
  "error": null
}
```

주요 검증:

- 충전 금액은 0보다 커야 한다.

### 3.7 잔액 조회

메서드:

```text
GET /api/v1/users/{userId}/balance
```

응답 예시:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "balance": 150000
  },
  "error": null
}
```

### 3.8 결제 요청

메서드:

```text
POST /api/v1/payments
```

헤더:

```text
X-Queue-Token: qt_123456789
```

요청 본문:

```json
{
  "userId": 1,
  "reservationId": 5001
}
```

설명:

- 본인 임시 배정 좌석을 결제하고 최종 확정한다.

응답 예시:

```json
{
  "success": true,
  "data": {
    "paymentId": 9001,
    "reservationId": 5001,
    "amount": 50000,
    "status": "SUCCESS",
    "paidAt": "2026-03-12T10:02:00"
  },
  "error": null
}
```

주요 검증:

- 토큰 존재 여부
- 토큰이 해당 콘서트의 `ACTIVE` 상태인지
- 예약이 본인 소유인지
- 예약이 아직 만료되지 않았는지
- 좌석이 본인에게 홀드된 상태인지
- 잔액이 충분한지

주요 에러 코드 예시:

- `QUEUE_TOKEN_INVALID`
- `QUEUE_TOKEN_NOT_ACTIVE`
- `RESERVATION_NOT_FOUND`
- `RESERVATION_EXPIRED`
- `SEAT_HOLD_EXPIRED`
- `INSUFFICIENT_BALANCE`
- `PAYMENT_ALREADY_COMPLETED`
- `OPTIMISTIC_LOCK_CONFLICT`

## 4. 상태 코드 초안

- `200 OK`: 조회 성공
- `201 Created`: 토큰 발급, 예약 생성, 결제 생성 성공
- `400 Bad Request`: 잘못된 요청 값
- `404 Not Found`: 사용자, 콘서트, 회차, 예약 없음
- `409 Conflict`: 좌석 선점 충돌, 낙관적 락 충돌, 중복 결제
- `422 Unprocessable Entity`: 잔액 부족, 만료된 예약 등 비즈니스 실패

## 5. DTO 초안

### 5.1 QueueTokenIssueRequest

```json
{
  "userId": 1
}
```

### 5.2 ReservationCreateRequest

```json
{
  "userId": 1,
  "scheduleId": 100,
  "seatNumber": 12
}
```

### 5.3 BalanceChargeRequest

```json
{
  "amount": 100000
}
```

### 5.4 PaymentCreateRequest

```json
{
  "userId": 1,
  "reservationId": 5001
}
```

## 6. API 구현 시 주의점

- 예약 가능 좌석 조회 결과만 믿고 예약 성공을 보장하면 안 된다.
- 예약과 결제 API에서는 반드시 토큰, 만료, 상태를 트랜잭션 안에서 재검증해야 한다.
- 결제 완료 후 동일 예약에 대한 중복 결제가 불가능해야 한다.
- 에러 코드는 테스트 케이스와 함께 고정하는 것이 좋다.
