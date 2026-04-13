# 콘서트 예약 서비스 학습 로드맵

## 1. 문서 목적

이 문서는 콘서트 예약 서비스 프로젝트를 단계적으로 구현하기 위한 학습 로드맵을 정리한다.

핵심 원칙은 아래와 같다.

- 단계별 목표를 분리한다.
- 현재 단계의 범위를 넘는 구현은 하지 않는다.
- 설계 의도와 코드 구조를 함께 학습한다.
- 각 단계 종료 시 배운 점과 다음 단계 개선 포인트를 남긴다.

## 2. 전체 단계 개요

### 2.1 1단계 설계 문서화

목표:

- 요구사항, 도메인 모델, ERD, 동시성 전략, 테스트 전략을 문서로 확정한다.

산출물:

- `README.md`
- `docs/architecture.md`
- `docs/requirements.md`
- `docs/domain-design.md`
- `docs/concurrency.md`
- `docs/erd.md`
- `docs/jpa-entity-design.md`
- `docs/transaction-sequence.md`
- `docs/api-spec.md`
- `docs/testing-strategy.md`
- `docs/roadmap.md`

완료 기준:

- 아키텍처, 도메인, 대기열, 만료, 동시성, 테스트 전략이 문서로 정리되어 있다.

### 2.2 2단계 API 계약과 Mock API 문서화

목표:

- 실제 비즈니스 구현 전에 API 계약을 고정하고, 문서와 Mock 응답 구조를 먼저 정리한다.

범위:

- Controller 시그니처 정의
- Request와 Response DTO 정의
- Swagger 또는 OpenAPI 문서화
- Mock 응답 수준의 API 계약 검증

제외:

- 실제 대기열 처리
- 실제 좌석 선점
- 실제 잔액 차감
- 실제 결제 확정
- 실제 스케줄러 처리

완료 기준:

- 주요 API 계약이 문서와 코드에 일관되게 드러난다.
- Swagger에서 요청과 응답 구조를 확인할 수 있다.

### 2.3 3단계 Application과 Domain 골격 구현

목표:

- `presentation`, `application`, `domain`, `infrastructure` 계층 구조를 실제 코드에 반영한다.

범위:

- 유스케이스 인터페이스와 서비스 골격
- 도메인 상태 enum
- 도메인 예외
- 포트 인터페이스 초안

완료 기준:

- 계층 책임이 코드 구조에 드러난다.
- 핵심 로직이 앞으로 들어갈 위치가 명확하다.

현재 반영 상태:

- `presentation -> application(usecase/facade) -> domain` 구조가 코드로 드러난다.
- `application/port/out`이 추가되어 infrastructure 의존이 역전되었다.
- `infrastructure.mock` adapter가 현재 포트 구현체 역할을 담당한다.
- domain에는 상태 enum, 도메인 예외, 최소 도메인 모델이 반영되었다.
- 컨트롤러는 더 이상 `MockApiService`를 직접 호출하지 않고 `usecase` 인터페이스를 의존한다.
- `compileJava` 기준으로 골격 연결을 검증했다.

### 2.4 4단계 DB 기반 핵심 정합성 구현

목표:

- DB 기반 정합성 제어로 예약과 결제의 핵심 흐름을 구현한다.

범위:

- `SeatInventory` 중심 현재 좌석 상태 관리
- 조건부 업데이트 기반 좌석 선점
- `Reservation`, `Payment`, `UserBalance` 핵심 흐름 구현
- 낙관적 락 기반 잔액 차감

완료 기준:

- 같은 좌석 중복 선점이 방지된다.
- 결제 트랜잭션이 원자적으로 동작한다.

3단계에서 4단계로 이어지는 작업:

- `infrastructure.persistence` 추가
- MySQL 연동과 JPA Entity/Repository 구현
- facade 내부 실제 유스케이스 흐름 채우기
- 조건부 업데이트와 낙관적 락 반영
- facade 기준 테스트와 persistence 통합 테스트 분리
- 인덱스 설계 및 성능 분석 포인트를 영속성 계층 기준으로 정리

### 2.5 5단계 대기열과 만료 처리 구현

목표:

- 콘서트별 대기열과 만료 정리 흐름을 실제로 반영한다.

범위:

- `QueueToken` 발급과 조회
- `WAITING -> ACTIVE` 승격 보정 로직
- `ACTIVE` 슬롯 관리
- 만료 스케줄러
- 요청 시점 재검증

완료 기준:

- 대기열 승격 정책이 코드와 문서에 일치한다.
- 만료 처리와 실제 요청 간 충돌에도 정합성이 유지된다.

### 2.6 6단계 테스트 강화

목표:

- 단위 테스트, 통합 테스트, 동시성 테스트를 강화한다.

범위:

- 도메인 규칙 테스트
- 유스케이스 테스트
- 조건부 업데이트 테스트
- 낙관적 락 충돌 테스트
- 만료 경계 테스트

완료 기준:

- 핵심 제약과 실패 시나리오가 테스트로 재현 가능하다.

### 2.7 7단계 확장 학습

목표:

- 단일 DB 기반 제어 이후 확장 전략을 학습한다.

후보 주제:

- Redis 기반 분산 락
- Outbox 패턴
- Kafka 기반 비동기 처리
- 최종 일관성 전략
- 사용자 단일 임시 배정 보조 테이블 전략

완료 기준:

- 현재 구조를 유지하면서 확장 가능한 방향을 설명할 수 있다.

### 2.8 STEP09 동시성 문제 분석과 해결 방식 확정

목표:

- 좌석 선점과 유저 잔액 차감의 동시성 문제를 분석하고 DB 기반 해결 방식을 확정한다.

주요 산출물:

- `docs/steps/step09-concurrency.md`

주요 내용:

- 좌석 선점은 `Pessimistic Lock`을 우선 적용 대상으로 정리했다.
- 유저 잔액 차감은 `@Version` 기반 `Optimistic Lock`을 우선 적용 대상으로 정리했다.
- STEP10에서 구현해야 할 테스트 방향과 트랜잭션 고려사항을 함께 정리했다.

관련 브랜치:

- `feature/finalize-concurrency`

최종 반영 브랜치:

- `feature/finalize-concurrency`

### 2.9 STEP10 동시성 구현과 통합 테스트 검증

목표:

- STEP09에서 확정한 동시성 해결 방안을 실제 코드와 통합 테스트로 검증한다.

주요 산출물:

- `docs/steps/step10-finalize.md`
- `ReservationFacade` 좌석 선점 orchestration 보강
- `SeatInventoryJpaRepository` 비관적 락 조회 메서드
- `ReservationFacadeConcurrencyTest`
- `PaymentFacade` 잔액 차감 orchestration 보강
- `PaymentFacadeConcurrencyTest`

주요 내용:

- 좌석 선점은 `scheduleId + seatNumber` 기준 `PESSIMISTIC_WRITE`로 구현했다.
- `ReservationFacade`가 좌석 선점 검증 순서와 저장 순서를 직접 orchestration 하도록 반영했다.
- 동일 좌석 동시 요청 성공 1건, HELD/RESERVED/만료 경계 시나리오를 테스트로 보강했다.
- 유저 잔액 차감은 `UserJpaEntity.version` 기반 `Optimistic Lock`으로 구현했다.
- 같은 사용자에 대한 동시 결제 요청에서 1건만 성공하고 최종 잔액이 3,000원이 되는 시나리오를 테스트로 검증했다.

관련 브랜치:

- `feature/finalize-concurrency`

최종 반영 브랜치:

- `feature/finalize-concurrency`

## 3. 현재 기준 핵심 정책

- 아키텍처는 도메인 중심의 가벼운 클린 아키텍처를 따른다.
- 계층은 `presentation`, `application`, `domain`, `infrastructure`로 분리한다.
- 현재 좌석 상태의 단일 기준은 `SeatInventory`다.
- JPA Entity는 FK ID 중심으로 설계하고 외래키 제약은 두지 않는다.
- 참조 무결성 검증은 애플리케이션 서비스와 도메인 규칙이 담당한다.
- 대기열은 콘서트별로 관리한다.
- 좌석 선점은 조건부 업데이트를 우선 적용한다.
- 잔액 차감은 낙관적 락을 기본안으로 사용한다.
- 만료 처리는 스케줄러와 요청 시점 재검증을 병행한다.
- `한 사용자는 동시에 하나의 임시 배정만 가능` 규칙은 동시성 제약으로 다룬다.

## 4. 현재 단계 운영 원칙

- 현재 단계 목표를 벗어난 구현은 하지 않는다.
- 설계 단계에서는 문서 품질과 정책 일관성을 우선한다.
- Mock 단계에서는 실제 비즈니스 구현 대신 API 계약과 응답 구조를 우선한다.
- 구현 단계에서는 테스트 가능 구조와 트랜잭션 경계를 먼저 반영한다.

현재 단계 해석:

- 3단계는 실제 기능 완성 단계가 아니라 골격 구현 단계다.
- facade, usecase, port.out, infrastructure.mock을 통해 책임과 의존성 방향을 먼저 고정한다.
- 상태 전이, 트랜잭션, 락, 조건부 업데이트, 만료 재검증, 대기열 승격은 다음 단계의 구현 대상이다.

## 5. 단계 종료 시 기록할 항목

- 이번 단계에서 배운 점
- 현재 단계의 한계
- 다음 단계에서 보완할 내용
- 설계와 구현 간 차이점

3단계 종료 메모:

- 배운 점: mock 구현체도 infrastructure 바깥 계층으로 배치해야 의존성 방향이 유지된다.
- 한계: 실제 persistence와 트랜잭션이 없어 정합성 보장은 아직 문서 수준이다.
- 다음 단계 보완: `infrastructure.persistence`, MySQL 연동, 통합 테스트, 인덱스 설계 문서화

## 6. 문서 연계

- `README.md`: 프로젝트 개요와 설계 의도
- `docs/architecture.md`: 계층 구조와 책임
- `docs/requirements.md`: 기능과 정책
- `docs/domain-design.md`: 핵심 모델과 상태 흐름
- `docs/concurrency.md`: 동시성 위험과 락 전략
- `docs/erd.md`: 테이블과 참조 ID 구조
- `docs/jpa-entity-design.md`: JPA 엔티티 설계 방향
- `docs/transaction-sequence.md`: 유스케이스 트랜잭션 흐름
- `docs/api-spec.md`: API 명세
- `docs/testing-strategy.md`: 테스트 전략
- `docs/steps/step09-concurrency.md`: 동시성 문제 분석과 해결 방식 선정
- `docs/steps/step10-finalize.md`: 동시성 구현 및 통합 테스트 검증
