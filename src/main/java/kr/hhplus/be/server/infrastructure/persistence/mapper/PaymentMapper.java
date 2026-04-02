package kr.hhplus.be.server.infrastructure.persistence.mapper;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getReservationId(),
                entity.getUserId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getPaidAt()
        );
    }
}
