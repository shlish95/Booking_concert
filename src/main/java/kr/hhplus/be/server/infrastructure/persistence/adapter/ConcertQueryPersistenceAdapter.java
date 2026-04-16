package kr.hhplus.be.server.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.concert.port.out.ConcertQueryPort;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ConcertScheduleMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!mock")
public class ConcertQueryPersistenceAdapter implements ConcertQueryPort {

    private static final Duration SCHEDULE_CACHE_TTL = Duration.ofMinutes(10);
    private static final String SCHEDULE_CACHE_KEY_PREFIX = "concert:schedules:";

    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final SeatInventoryJpaRepository seatInventoryJpaRepository;
    private final ConcertScheduleMapper concertScheduleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ConcertQueryPersistenceAdapter(
            ConcertScheduleJpaRepository concertScheduleJpaRepository,
            SeatInventoryJpaRepository seatInventoryJpaRepository,
            ConcertScheduleMapper concertScheduleMapper,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
        this.seatInventoryJpaRepository = seatInventoryJpaRepository;
        this.concertScheduleMapper = concertScheduleMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConcertSchedule> getSchedules(Long concertId) {
        String cacheKey = scheduleCacheKey(concertId);
        List<ConcertSchedule> cachedSchedules = getSchedulesFromCache(cacheKey);
        if (cachedSchedules != null) {
            return cachedSchedules;
        }

        List<ConcertSchedule> schedules = concertScheduleJpaRepository.findAllByConcertIdOrderByConcertDateAsc(concertId).stream()
                .map(concertScheduleMapper::toDomain)
                .toList();
        putSchedulesToCache(cacheKey, schedules);

        return schedules;
    }

    @Override
    @Transactional(readOnly = true)
    public ConcertSchedule getSchedule(Long scheduleId) {
        return concertScheduleMapper.toDomain(
                concertScheduleJpaRepository.findById(scheduleId)
                        .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다. scheduleId=" + scheduleId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableSeatNumbers(Long scheduleId) {
        return seatInventoryJpaRepository.findAvailableSeatNumbers(scheduleId, LocalDateTime.now());
    }

    private String scheduleCacheKey(Long concertId) {
        return SCHEDULE_CACHE_KEY_PREFIX + concertId;
    }

    private List<ConcertSchedule> getSchedulesFromCache(String cacheKey) {
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached == null) {
            return null;
        }

        try {
            List<CachedSchedule> cachedSchedules = objectMapper.readValue(
                    cached,
                    new TypeReference<>() {
                    }
            );
            return cachedSchedules.stream()
                    .map(CachedSchedule::toDomain)
                    .toList();
        } catch (JsonProcessingException e) {
            stringRedisTemplate.delete(cacheKey);
            return null;
        }
    }

    private void putSchedulesToCache(String cacheKey, List<ConcertSchedule> schedules) {
        try {
            List<CachedSchedule> cachedSchedules = schedules.stream()
                    .map(CachedSchedule::from)
                    .toList();
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(cachedSchedules),
                    SCHEDULE_CACHE_TTL
            );
        } catch (JsonProcessingException e) {
            // Cache failure must not block the source-of-truth DB query result.
        }
    }

    private record CachedSchedule(
            Long scheduleId,
            Long concertId,
            String concertDate
    ) {

        private static CachedSchedule from(ConcertSchedule schedule) {
            return new CachedSchedule(
                    schedule.scheduleId(),
                    schedule.concertId(),
                    schedule.concertDate().toString()
            );
        }

        private ConcertSchedule toDomain() {
            return new ConcertSchedule(
                    scheduleId,
                    concertId,
                    java.time.LocalDate.parse(concertDate)
            );
        }
    }
}
