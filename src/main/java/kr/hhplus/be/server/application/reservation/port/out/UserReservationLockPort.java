package kr.hhplus.be.server.application.reservation.port.out;

import java.util.function.Supplier;

public interface UserReservationLockPort {

    <T> T executeWithUserLock(Long userId, Supplier<T> action);
}
