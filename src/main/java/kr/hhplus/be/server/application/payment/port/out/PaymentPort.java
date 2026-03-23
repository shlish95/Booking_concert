package kr.hhplus.be.server.application.payment.port.out;

import kr.hhplus.be.server.domain.payment.Payment;

public interface PaymentPort {

    Payment pay(String queueToken, Long userId, Long reservationId);
}
