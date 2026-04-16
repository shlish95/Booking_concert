package kr.hhplus.be.server.infrastructure.mock.reservation;

import kr.hhplus.be.server.application.reservation.port.out.UserReservationLockPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Profile("mock")
public class MockUserReservationLockAdapter implements UserReservationLockPort {

    @Override
    public <T> T executeWithUserLock(Long userId, Supplier<T> action) {
        return action.get();
    }
}
