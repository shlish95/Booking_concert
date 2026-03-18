package kr.hhplus.be.server.application.mock;

import kr.hhplus.be.server.presentation.balance.dto.BalanceResponse;
import kr.hhplus.be.server.presentation.concert.dto.AvailableSeatsResponse;
import kr.hhplus.be.server.presentation.concert.dto.ScheduleSummaryResponse;
import kr.hhplus.be.server.presentation.payment.dto.PaymentResponse;
import kr.hhplus.be.server.presentation.queue.dto.QueueTokenResponse;
import kr.hhplus.be.server.presentation.reservation.dto.ReservationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MockApiService {

    public QueueTokenResponse issueQueueToken(Long concertId, Long userId) {
        return new QueueTokenResponse(
                "qt_mock_" + concertId + "_" + userId,
                concertId,
                userId,
                125L,
                "WAITING",
                LocalDateTime.of(2026, 3, 12, 10, 0, 0),
                null,
                null
        );
    }

    public QueueTokenResponse getQueueToken(Long concertId, String token) {
        return new QueueTokenResponse(
                token,
                concertId,
                1L,
                125L,
                "WAITING",
                LocalDateTime.of(2026, 3, 12, 10, 0, 0),
                null,
                null
        );
    }

    public List<ScheduleSummaryResponse> getSchedules(Long concertId) {
        return List.of(
                new ScheduleSummaryResponse(100L, concertId, LocalDate.of(2026, 4, 1)),
                new ScheduleSummaryResponse(101L, concertId, LocalDate.of(2026, 4, 2))
        );
    }

    public AvailableSeatsResponse getAvailableSeats(Long scheduleId) {
        return new AvailableSeatsResponse(scheduleId, List.of(1, 2, 3, 7, 8, 9, 12, 13));
    }

    public ReservationResponse createReservation(Long userId, Long scheduleId, Integer seatNumber) {
        return new ReservationResponse(
                5001L,
                userId,
                scheduleId,
                seatNumber,
                "TEMPORARY",
                LocalDateTime.of(2026, 3, 12, 10, 5, 0)
        );
    }

    public BalanceResponse chargeBalance(Long userId, Long amount) {
        return new BalanceResponse(userId, 50_000L + amount);
    }

    public BalanceResponse getBalance(Long userId) {
        return new BalanceResponse(userId, 150_000L);
    }

    public PaymentResponse createPayment(Long userId, Long reservationId) {
        return new PaymentResponse(
                9001L,
                reservationId,
                userId,
                50_000L,
                "SUCCESS",
                LocalDateTime.of(2026, 3, 12, 10, 2, 0)
        );
    }
}
