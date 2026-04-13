package kr.hhplus.be.server.application.payment.facade;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.balance.port.out.BalancePort;
import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.payment.dto.PaymentResult;
import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.application.payment.usecase.PayReservationUseCase;
import kr.hhplus.be.server.domain.balance.InsufficientBalanceException;
import kr.hhplus.be.server.domain.balance.OptimisticLockConflictException;
import kr.hhplus.be.server.domain.balance.UserBalance;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentFacade implements PayReservationUseCase {

    private final BalancePort balancePort;
    private final PaymentPort paymentPort;

    public PaymentFacade(BalancePort balancePort, PaymentPort paymentPort) {
        this.balancePort = balancePort;
        this.paymentPort = paymentPort;
    }

    @Override
    @Transactional
    public PaymentResult pay(PayReservationCommand command) {
        validateRequest(command);
        Long amount = getPaymentAmount(command);
        UserBalance currentBalance = getCurrentBalance(command);
        validateSufficientBalance(currentBalance, amount);
        Payment payment = deductBalanceAndSavePayment(command, amount);

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

    private Long getPaymentAmount(PayReservationCommand command) {
        return paymentPort.getPaymentAmount(command.userId(), command.reservationId());
    }

    private UserBalance getCurrentBalance(PayReservationCommand command) {
        return balancePort.get(command.userId());
    }

    private void validateSufficientBalance(UserBalance currentBalance, Long amount) {
        if (currentBalance.amount() < amount) {
            throw new InsufficientBalanceException();
        }
    }

    private Payment deductBalanceAndSavePayment(PayReservationCommand command, Long amount) {
        try {
            balancePort.use(command.userId(), amount);
            return paymentPort.saveSuccess(
                    command.userId(),
                    command.reservationId(),
                    amount,
                    LocalDateTime.now()
            );
        } catch (OptimisticLockException | OptimisticLockConflictException e) {
            throw new OptimisticLockConflictException();
        }
    }
}
