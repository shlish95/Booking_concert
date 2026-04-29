# STEP13 - Ranking Design
## 빠른 매진 랭킹 1차 구현

## 1. 문서 목적

이 문서는 STEP13에서 적용한 Redis 기반 빠른 매진 랭킹의 1차 구현 범위를 정리한다.

## 2. 랭킹 정의

1차 구현은 판매 시작 이후 완판까지 걸린 시간을 기준으로 한 전체 랭킹 하나만 다룬다.

- 랭킹 대상: 완판된 회차만 포함
- 랭킹 기준: `soldOutDurationSeconds`
- 빠를수록 상위 랭크

## 3. Redis 설계

- 자료구조: Redis Sorted Set
- 키: `ranking:concert:soldout:speed:all`
- member: `schedule:{scheduleId}`
- score: `soldOutDurationSeconds`

## 4. 적재 시점

적재는 결제 성공 이후 수행한다.

1. 결제 성공
2. 좌석 상태를 `RESERVED`로 반영
3. 예약 상태를 `CONFIRMED`로 반영
4. 해당 회차의 잔여 좌석 수를 확인
5. 잔여 좌석이 0이면 랭킹 적재

중복 적재는 Redis `ZADD NX` 성격의 `addIfAbsent`로 방지한다.

## 5. 조회 전략

- 조회 범위: 전체 랭킹 `all`
- 조회 방식: score 오름차순 Top N
- 반환 필드: `rank`, `scheduleId`, `soldOutDurationSeconds`

## 6. 제외 범위

이번 단계에서는 아래를 제외한다.

- 일간/주간/월간 랭킹
- 콘서트 이름 등 상세 메타데이터 결합
- 고급 동률 처리
- 배치성 재계산
- STEP14 대기열 기능 연계
