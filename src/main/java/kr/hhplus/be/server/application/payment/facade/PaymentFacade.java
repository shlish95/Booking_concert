package kr.hhplus.be.server.application.payment.facade;

import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.payment.dto.PaymentResult;
import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.application.payment.usecase.PayReservationUseCase;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentFacade implements PayReservationUseCase {

    private final PaymentPort paymentPort;

    public PaymentFacade(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    @Override
    public PaymentResult pay(PayReservationCommand command) {
        validateRequest(command);
        planReservationLookupAndValidation(command);
        planScheduleQueueAndSeatValidation(command);
        planConcertPriceLookup(command);
        planUserBalanceValidation(command);
        planPaymentCommitOrder(command);

        // TODO: facade가 예약/회차/토큰/좌석/콘서트/사용자 조회와 검증,
        // 잔액 차감, 결제 저장, 예약 확정, 좌석 확정, 토큰 종료 순서를 직접 조합하도록 확장한다.
        Payment payment = payReservation(command);

        return new PaymentResult(
                payment.paymentId(),
                payment.reservationId(),
                payment.userId(),
                payment.amount(),
                payment.status().name(),
                payment.paidAt()
        );
    }

    private void validateRequest(PayReservationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("결제 요청은 null일 수 없습니다.");
        }
    }

    private void planReservationLookupAndValidation(PayReservationCommand command) {
        // TODO: reservationId + userId 기준 예약 조회와 상태/만료 검증을 facade가 담당한다.
    }

    private void planScheduleQueueAndSeatValidation(PayReservationCommand command) {
        // TODO: 회차 조회, 토큰 조회/검증, 좌석 조회/상태 검증 순서를 facade에서 결정한다.
    }

    private void planConcertPriceLookup(PayReservationCommand command) {
        // TODO: 콘서트 가격 조회와 결제 금액 확정 책임을 facade에 둔다.
    }

    private void planUserBalanceValidation(PayReservationCommand command) {
        // TODO: 사용자 잔액 조회와 잔액 부족 판단을 facade가 담당한다.
    }

    private void planPaymentCommitOrder(PayReservationCommand command) {
        // TODO: 잔액 차감, 결제 저장, 예약 확정, 좌석 확정, 토큰 종료 순서를 facade에서 결정한다.
    }

    private Payment payReservation(PayReservationCommand command) {
        return paymentPort.pay(command.queueToken(), command.userId(), command.reservationId());
    }
}
