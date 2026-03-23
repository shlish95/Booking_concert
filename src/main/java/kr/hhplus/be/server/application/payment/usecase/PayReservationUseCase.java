package kr.hhplus.be.server.application.payment.usecase;

import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.payment.dto.PaymentResult;

public interface PayReservationUseCase {

    PaymentResult pay(PayReservationCommand command);
}
