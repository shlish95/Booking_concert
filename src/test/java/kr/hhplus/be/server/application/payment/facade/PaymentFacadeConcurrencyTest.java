package kr.hhplus.be.server.application.payment.facade;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.domain.balance.OptimisticLockConflictException;
import kr.hhplus.be.server.infrastructure.persistence.adapter.BalancePersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.PaymentPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.PaymentMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.UserBalanceMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        PaymentFacade.class,
        BalancePersistenceAdapter.class,
        PaymentPersistenceAdapter.class,
        UserBalanceMapper.class,
        PaymentMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentFacadeConcurrencyTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ConcertJpaRepository concertJpaRepository;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @BeforeEach
    void setUp() {
        paymentJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
        concertJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("같은 사용자 결제 두 건이 동시에 요청되면 한 건만 성공하고 최종 잔액은 3000원이다")
    void payConcurrently_onlyOneSucceeds() throws Exception {
        UserJpaEntity user = userJpaRepository.save(new UserJpaEntity("user-1", 10_000L));
        ConcertJpaEntity concert = concertJpaRepository.save(new ConcertJpaEntity("concert-1", 7_000L));
        ConcertScheduleJpaEntity schedule = concertScheduleJpaRepository.save(
                new ConcertScheduleJpaEntity(concert.getId(), LocalDate.of(2026, 4, 20))
        );
        ReservationJpaEntity reservation = reservationJpaRepository.save(
                new ReservationJpaEntity(
                        user.getId(),
                        schedule.getId(),
                        1,
                        kr.hhplus.be.server.domain.reservation.ReservationStatus.TEMPORARY,
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(5),
                        null
                )
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<Boolean> future1 = executorService.submit(tryPay(startLatch, new PayReservationCommand("ignored", user.getId(), reservation.getId())));
        Future<Boolean> future2 = executorService.submit(tryPay(startLatch, new PayReservationCommand("ignored", user.getId(), reservation.getId())));

        startLatch.countDown();

        int successCount = 0;
        successCount += future1.get() ? 1 : 0;
        successCount += future2.get() ? 1 : 0;

        executorService.shutdown();

        assertThat(successCount).isEqualTo(1);
        assertThat(paymentJpaRepository.count()).isEqualTo(1);
        assertThat(userJpaRepository.findById(user.getId()))
                .isPresent()
                .get()
                .extracting(UserJpaEntity::getBalance)
                .isEqualTo(3_000L);
    }

    private Callable<Boolean> tryPay(CountDownLatch startLatch, PayReservationCommand command) {
        return () -> {
            startLatch.await();
            try {
                paymentFacade.pay(command);
                return true;
            } catch (OptimisticLockConflictException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        };
    }
}
