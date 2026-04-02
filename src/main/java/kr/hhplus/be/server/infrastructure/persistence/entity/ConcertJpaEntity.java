package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "concerts")
public class ConcertJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "seat_price", nullable = false)
    private Long seatPrice;

    protected ConcertJpaEntity() {
    }

    public ConcertJpaEntity(String name, Long seatPrice) {
        this.name = name;
        this.seatPrice = seatPrice;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSeatPrice() {
        return seatPrice;
    }
}
