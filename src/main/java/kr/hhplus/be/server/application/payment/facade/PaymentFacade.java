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
        Payment payment = paymentPort.pay(command.queueToken(), command.userId(), command.reservationId());

        return new PaymentResult(
                payment.paymentId(),
                payment.reservationId(),
                payment.userId(),
                payment.amount(),
                payment.status().name(),
                payment.paidAt()
        );
    }
}
