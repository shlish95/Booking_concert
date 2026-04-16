# STEP12 - Cache
## 콘서트 예약 서비스 조회 캐시 1차 적용

## 1. 문서 목적

이 문서는 STEP12에서 적용한 Redis 기반 조회 캐시의 첫 적용 범위와 전략을 정리한다.

## 2. 1차 적용 대상

1차 적용 대상은 콘서트별 예약 가능 회차 조회다.

- 대상 흐름: `ConcertQueryPort.getSchedules(concertId)`
- 구현 위치: `ConcertQueryPersistenceAdapter`
- 제외 범위: 예약/결제 쓰기 흐름, 회차별 예약 가능 좌석 조회

## 3. 캐시 전략

- 전략: Cache-Aside
- 캐시 키: `concert:schedules:{concertId}`
- TTL: 10분
- cache hit: Redis 값을 반환
- cache miss: DB 조회 후 Redis에 저장하고 반환

## 4. 정합성 기준

회차 정보는 변경 빈도가 낮고, 예약/결제 정합성의 최종 판단 대상이 아니다.
따라서 1차 구현에서는 명시적 evict 없이 TTL 기반 만료만 적용한다.

예약 가능 좌석 조회는 좌석 상태 변경과 직접 맞물리므로 이번 단계에서는 캐시하지 않는다.
