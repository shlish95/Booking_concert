package kr.hhplus.be.server.application.payment.facade;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.balance.port.out.BalancePort;
import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.payment.dto.PaymentResult;
import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.application.payment.usecase.PayReservationUseCase;
import kr.hhplus.be.server.application.ranking.port.out.SoldOutRankingPort;
import kr.hhplus.be.server.domain.balance.InsufficientBalanceException;
import kr.hhplus.be.server.domain.balance.OptimisticLockConflictException;
import kr.hhplus.be.server.domain.balance.UserBalance;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PaymentFacade implements PayReservationUseCase {

    private final BalancePort balancePort;
    private final PaymentPort paymentPort;
    private final SoldOutRankingPort soldOutRankingPort;

    public PaymentFacade(
            BalancePort balancePort,
            PaymentPort paymentPort,
            SoldOutRankingPort soldOutRankingPort
    ) {
        this.balancePort = balancePort;
        this.paymentPort = paymentPort;
        this.soldOutRankingPort = soldOutRankingPort;
    }

    @Override
    @Transactional
    public PaymentResult pay(PayReservationCommand command) {
        validateRequest(command);
        PaymentPort.PaymentContext paymentContext = getPaymentContext(command);
        Long amount = paymentContext.amount();
        UserBalance currentBalance = getCurrentBalance(command);
        validateSufficientBalance(currentBalance, amount);
        Payment payment = deductBalanceAndSavePayment(command, paymentContext);
        recordSoldOutRankingIfNeeded(paymentContext, payment.paidAt());

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

    private PaymentPort.PaymentContext getPaymentContext(PayReservationCommand command) {
        return paymentPort.getPaymentContext(command.userId(), command.reservationId());
    }

    private UserBalance getCurrentBalance(PayReservationCommand command) {
        return balancePort.get(command.userId());
    }

    private void validateSufficientBalance(UserBalance currentBalance, Long amount) {
        if (currentBalance.amount() < amount) {
            throw new InsufficientBalanceException();
        }
    }

    private Payment deductBalanceAndSavePayment(
            PayReservationCommand command,
            PaymentPort.PaymentContext paymentContext
    ) {
        try {
            balancePort.use(command.userId(), paymentContext.amount());
            return paymentPort.saveSuccess(
                    command.userId(),
                    command.reservationId(),
                    paymentContext.amount(),
                    LocalDateTime.now()
            );
        } catch (OptimisticLockException | OptimisticLockConflictException e) {
            throw new OptimisticLockConflictException();
        }
    }

    private void recordSoldOutRankingIfNeeded(PaymentPort.PaymentContext paymentContext, LocalDateTime paidAt) {
        if (!paymentPort.isScheduleSoldOut(paymentContext.scheduleId())) {
            return;
        }

        long soldOutDurationSeconds = Math.max(
                0,
                Duration.between(paymentContext.salesOpenedAt(), paidAt).getSeconds()
        );
        soldOutRankingPort.recordSoldOutIfAbsent(paymentContext.scheduleId(), soldOutDurationSeconds);
    }
}
