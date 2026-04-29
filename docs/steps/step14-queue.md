# STEP14 - Asynchronous Design
## Redis 기반 콘서트별 대기열 1차 구현

## 1. 문서 목적

이 문서는 STEP14에서 적용한 Redis 기반 대기열의 1차 설계와 구현 범위를 정리한다.

## 2. 1차 적용 범위

이번 단계에서는 아래만 다룬다.

- 토큰 발급
- 상위 N명 `WAITING -> ACTIVE` 전환
- `ACTIVE` 토큰 검증

이번 단계에서 제외한다.

- 사용 종료 후 후속 승격
- 복잡한 스케줄러/배치 처리
- Lua 기반 고급 원자화
- STEP14 이후의 대기열 운영 고도화

## 3. Redis 자료구조

- waiting: `Sorted Set`
- active: `Set`
- token metadata: `Hash`

키 구조:

- `queue:{concertId}:waiting`
- `queue:{concertId}:active`
- `queue:token:{token}`

설계:

- waiting ZSET member = `token`
- waiting ZSET score = `issuedAtEpochMillis`
- active SET member = `token`

metadata 필드:

- `token`
- `userId`
- `concertId`
- `status`
- `issuedAt`
- `activatedAt`
- `expiredAt`

## 4. 흐름

### 4.1 토큰 발급

1. metadata를 `WAITING` 상태로 저장
2. waiting ZSET에 삽입
3. 현재 순번을 계산해 반환

### 4.2 상위 N명 활성화

1. 현재 active 수 확인
2. 남은 슬롯만큼 waiting 상위 토큰 조회
3. waiting에서 제거
4. active SET에 추가
5. metadata 상태를 `ACTIVE`로 변경

### 4.3 토큰 검증

1. metadata 조회
2. `concertId` 일치 확인
3. active SET membership 확인
4. active면 `ACTIVE`, waiting에 남아 있으면 `WAITING`으로 판단

## 5. 현재 구현 기준

- facade/application이 발급/활성화/검증 흐름을 orchestration 한다.
- Redis 기술 세부사항은 infrastructure adapter에 둔다.
- 기존 DB 기반 queue persistence adapter는 보조 구현으로 남기고, 실제 주입은 Redis adapter가 우선한다.
