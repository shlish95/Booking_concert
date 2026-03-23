# 콘서트 예약 서비스 아키텍처 설계

## 1. 문서 목적

이 문서는 프로젝트의 아키텍처 패턴, 계층 구조, 책임 분리, 의존성 원칙, 트랜잭션과 비동기 확장 지점을 정리한다.

## 2. 아키텍처 스타일

프로젝트는 도메인 중심의 가벼운 클린 아키텍처를 채택한다.

핵심 원칙:

- 비즈니스 규칙은 domain과 application 계층에 위치한다.
- 외부 기술은 infrastructure 계층으로 격리한다.
- presentation은 입출력 변환에 집중한다.
- 의존성은 안쪽 계층으로만 향한다.

현재 구현 단계 메모:

- 현재는 로드맵 3단계로서 실제 DB 정합성 구현 전의 골격 단계다.
- 목적은 기능 완성보다 계층 구조와 책임을 코드에 드러내는 데 있다.
- 실제 persistence 대신 `infrastructure.mock`이 포트를 구현해 구조를 먼저 고정했다.

## 3. 계층 구조

```text
presentation -> application -> domain
                     ^
                     |
              infrastructure
```

설명:

- `presentation`은 `application` 유스케이스를 호출한다.
- `application`은 `domain` 규칙을 사용한다.
- `infrastructure`는 `application` 또는 `domain`에서 정의한 포트 구현체를 제공한다.
- `domain`은 어떤 외부 기술에도 의존하지 않는다.

현재 코드 기준 흐름:

- Controller는 `usecase` 인터페이스를 주입받는다.
- `facade`는 `usecase`를 구현하고 `port.out`을 통해 바깥 계층과 통신한다.
- `infrastructure.mock` adapter는 `port.out` 구현체로 동작한다.
- 기존 `application/mock/MockApiService` 직접 호출 구조는 제거되었다.

## 4. 계층별 책임

### 4.1 presentation

주요 책임:

- Controller
- Request DTO 검증
- Header와 PathVariable 파싱
- Response DTO 변환
- 공통 예외를 HTTP 응답으로 변환

하지 말아야 할 것:

- 좌석 선점 여부 판단
- 결제 가능 여부 판단
- 토큰 상태 전이 처리

### 4.2 application

주요 책임:

- 유스케이스 오케스트레이션
- 트랜잭션 경계 설정
- 여러 도메인 객체 및 포트 조합
- 외부 포트 호출
- 이벤트 발행
- 예외 매핑

현재 코드 구조:

- `usecase`: presentation이 의존하는 애플리케이션 계약
- `dto`: request 성격의 command/query와 response 성격의 result
- `facade`: 현재는 얇은 오케스트레이션 계층이며 이후 트랜잭션 경계와 검증 순서가 들어갈 위치
- `port.out`: infrastructure 세부 구현을 추상화하는 출력 포트

대표 유스케이스 예시:

- `IssueQueueTokenUseCase`
- `GetQueuePositionUseCase`
- `GetAvailableSchedulesUseCase`
- `GetAvailableSeatsUseCase`
- `ReserveSeatUseCase`
- `ChargeBalanceUseCase`
- `GetBalanceUseCase`
- `PayReservationUseCase`

### 4.3 domain

주요 책임:

- 엔티티 상태 규칙
- 좌석 홀드 가능 여부 판단
- 예약 만료 판단
- 결제 가능 여부 판단
- 대기열 토큰 사용 가능 여부 판단

구성 예시:

- entity
- value object
- enum
- domain service
- domain exception
- repository port

현재 코드 반영 범위:

- 상태 enum
- 도메인 예외
- 최소 도메인 모델

아직 본격 구현하지 않은 것:

- 상태 전이 메서드
- 정책 객체
- 복합 도메인 규칙 조합

### 4.4 infrastructure

주요 책임:

- JPA 엔티티와 매핑
- Repository 구현체
- 조건부 업데이트 쿼리
- 스케줄러 구현
- 이벤트 발행 구현
- 로깅 설정

현재 단계에서는 아래만 반영되어 있다.

- `infrastructure.mock`: 포트를 구현하는 mock adapter

의도:

- mock 구현체도 바깥 계층에 둬야 application이 구체 구현을 모르게 할 수 있다.
- 이후 `infrastructure.persistence`를 추가해도 presentation/application 코드를 크게 바꾸지 않도록 하기 위함이다.

## 5. 패키지 구조 예시

```text
com.example.bookingconcert
  ├─ presentation
  │  ├─ queue
  │  ├─ reservation
  │  ├─ payment
  │  ├─ balance
  │  └─ common
  ├─ application
  │  ├─ queue
  │  │  ├─ usecase
  │  │  ├─ facade
  │  │  ├─ port/out
  │  │  └─ dto
  │  ├─ reservation
  │  ├─ payment
  │  ├─ balance
  │  └─ concert
  ├─ domain
  │  ├─ queue
  │  ├─ concert
  │  ├─ seat
  │  ├─ reservation
  │  ├─ payment
  │  ├─ balance
  │  └─ common
  └─ infrastructure
     ├─ mock
     ├─ persistence
     ├─ scheduler
     ├─ event
     └─ logging
```

설명:

- `usecase`는 컨트롤러가 의존하는 진입 계약이다.
- `facade`는 유스케이스 단위 오케스트레이션 계층이다.
- `port.out`은 infrastructure 의존성을 역전시키는 추상화다.
- `mock`은 현재 단계의 포트 구현체 자리이며, 이후 `persistence`가 같은 역할을 실제 DB로 대체한다.

## 6. 의존성 및 DI 전략

### 6.1 단방향 의존성

- `presentation`은 `application`에만 의존한다.
- `application`은 `domain`과 포트 인터페이스에 의존한다.
- `infrastructure`는 포트 구현체로서 `application` 또는 `domain`에서 정의한 인터페이스를 구현한다.

현재 코드상 반영 방식:

- controller 생성자 주입 대상은 `usecase` 인터페이스다.
- facade 생성자 주입 대상은 `port.out` 인터페이스다.
- adapter는 `port.out` 구현체로 등록된다.
- domain은 Spring, JPA, Web 타입을 알지 못한다.

### 6.2 생성자 주입

- 의존성 주입은 생성자 기반 DI를 기본으로 한다.
- 필드 주입은 사용하지 않는다.

이유:

- 테스트에서 Mock 주입이 쉽다.
- 필수 의존성을 컴파일 타임에 드러낼 수 있다.
- 순환 참조를 줄일 수 있다.

## 7. 핵심 모델링 원칙

### 7.1 SeatInventory 중심 모델

- 현재 좌석 상태의 진실은 `SeatInventory`다.
- `Reservation`은 이력과 사용자 소유 정보 추적에 집중한다.
- 좌석 조회와 중복 방지는 `SeatInventory` 기준으로 처리한다.

### 7.2 FK ID 중심 설계

- JPA 엔티티는 연관관계를 최소화한다.
- 필요하지 않은 객체 그래프 탐색을 피하고 FK ID 중심으로 설계한다.
- DB 외래키 제약은 두지 않는다.

이유:

- 성능과 단순성을 확보하기 쉽다.
- 대량 상태 변경이나 조건부 업데이트에 유리하다.
- 무결성 책임을 서비스와 도메인 규칙으로 집중시킬 수 있다.

### 7.3 무결성 책임 위치

- 사용자 존재, 콘서트 존재, 예약 소유권, 토큰 유효성은 application 서비스에서 검증한다.
- 상태 전이 가능 여부는 domain 규칙에서 검증한다.
- DB는 최소한의 유니크 제약과 인덱스로만 보조한다.

## 8. 대기열 설계

- 대기열은 콘서트별로 분리한다.
- 예약과 결제 시에만 토큰 검증을 요구한다.
- 토큰 상태는 `WAITING`, `ACTIVE`, `USED`, `EXPIRED`로 관리한다.
- 대기번호 조회는 읽기 전용 유스케이스로 분리한다.

## 9. 좌석 임시 배정 및 만료 처리

### 9.1 좌석 선점

- 좌석 선점은 조건부 업데이트를 우선 사용한다.
- `AVAILABLE` 이거나 만료된 `HELD` 상태만 점유 가능하다.
- 성공 건수 1건일 때만 선점 성공으로 판단한다.

### 9.2 예약 만료

- 임시 배정은 약 5분 유효하다.
- 스케줄러는 만료된 좌석과 예약을 정리한다.
- 요청 시점에도 만료 여부를 다시 검증한다.

원칙:

- 스케줄러는 정리 역할
- 트랜잭션 내 재검증이 최종 판정 역할

## 10. 동시성 제어 전략

### 10.1 좌석

- 조건부 업데이트 기반

### 10.2 잔액

- `@Version` 기반 낙관적 락 기본 적용
- 필요 시 비관적 락 비교 가능

### 10.3 토큰 및 결제

- 결제 트랜잭션 안에서 토큰, 예약, 좌석, 잔액 상태를 재검증한다.
- 토큰 사용 완료 처리는 결제 성공과 같은 트랜잭션에 포함한다.

## 11. 트랜잭션 전략

### 11.1 예약

한 트랜잭션 안에서 처리:

- 토큰 검증
- 사용자 활성 임시 배정 여부 확인
- 좌석 조건부 업데이트
- 예약 생성

### 11.2 결제

한 트랜잭션 안에서 처리:

- 토큰 검증
- 예약 소유 및 만료 검증
- 좌석 홀드 검증
- 잔액 차감
- 결제 생성
- 사용 이력 생성
- 좌석 확정
- 예약 확정
- 토큰 종료

### 11.3 실패 시 보상

- 동기 작업은 전체 롤백을 기본 전략으로 한다.
- 비동기 후처리는 이벤트 재시도로 보상한다.
- 핵심 데이터 정합성은 보상 트랜잭션에 기대지 않고 동기 트랜잭션으로 보장한다.

## 12. 테스트 가능 구조

### 12.1 단위 테스트 용이성

- application 서비스는 포트 인터페이스에만 의존한다.
- Repository, Clock, EventPublisher, Scheduler Trigger 등을 Mock 또는 Stub으로 대체 가능하다.

현재 단계 효과:

- facade 테스트는 포트를 대체해 순수 application 테스트로 분리할 수 있다.
- 향후 persistence 구현 이후에는 같은 유스케이스를 대상으로 통합 테스트 범위를 별도로 잡을 수 있다.

### 12.2 권장 테스트 분리

- domain 테스트: 순수 규칙 검증
- application 테스트: 유스케이스 흐름 검증
- infrastructure 테스트: JPA 쿼리 및 스케줄러 검증

## 13. 예외 처리 전략

### 13.1 커스텀 예외 예시

- `QueueTokenNotFoundException`
- `QueueTokenNotActiveException`
- `SeatAlreadyHeldException`
- `SeatAlreadyReservedException`
- `UserAlreadyHasActiveHoldException`
- `ReservationExpiredException`
- `ReservationOwnershipException`
- `InsufficientBalanceException`
- `PaymentAlreadyProcessedException`
- `OptimisticLockConflictException`

### 13.2 처리 원칙

- domain 또는 application 계층에서 의미 있는 예외를 던진다.
- presentation 계층에서 공통 예외 응답 포맷으로 변환한다.
- 예외 코드는 테스트와 문서에서 함께 관리한다.

## 14. 로깅 전략

필수 로그 포인트:

- API 요청 시작과 종료
- 좌석 선점 성공과 실패
- 결제 성공과 실패
- 낙관적 락 충돌
- 스케줄러 정리 결과
- 이벤트 발행 및 소비 실패

구조화 로그 권장 필드:

- `userId`
- `concertId`
- `scheduleId`
- `seatNumber`
- `reservationId`
- `paymentId`
- `queueToken`
- `result`

## 15. 이벤트 기반 확장 지점

비동기 적용 가능 영역:

- 결제 완료 알림
- 예약 만료 알림
- 대기열 활성 알림
- 통계 적재
- 감사 로그 적재

원칙:

- 핵심 상태 변경은 동기
- 부가 후처리는 비동기

## 16. 설계 요약

이 아키텍처는 `SeatInventory` 중심의 상태 관리, application 계층 중심의 트랜잭션 제어, domain 규칙 중심의 무결성 검증을 결합한 구조다. 외래키 없는 FK ID 중심 설계를 사용하되, 서비스와 도메인 규칙에서 정합성을 보장하도록 의도했다.
