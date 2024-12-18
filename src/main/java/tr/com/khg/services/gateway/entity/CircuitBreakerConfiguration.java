package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "circuit_breaker_config")
public class CircuitBreakerConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "failure_rate_threshold", nullable = false)
    private Integer failureRateThreshold;

    @Column(name = "slow_call_rate_threshold", nullable = false)
    private Integer slowCallRateThreshold;

    @Column(name = "slow_call_duration_threshold", nullable = false)
    private Long slowCallDurationThreshold;

    @Column(name = "permitted_number_of_calls_in_half_open_state", nullable = false)
    private Integer permittedNumberOfCallsInHalfOpenState;

    @Column(name = "sliding_window_size", nullable = false)
    private Integer slidingWindowSize;

    @Column(name = "minimum_number_of_calls", nullable = false)
    private Integer minimumNumberOfCalls;

    @Column(name = "wait_duration_in_open_state", nullable = false)
    private Long waitDurationInOpenState;

    @Column(name = "automatic_transition_from_open_to_half_open_enabled", nullable = false)
    private Boolean automaticTransitionFromOpenToHalfOpenEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
