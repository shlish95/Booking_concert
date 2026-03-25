package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_uuid", columnNames = "uuid")
        }
)
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 64)
    private String uuid;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(String uuid, Long balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getBalance() {
        return balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
