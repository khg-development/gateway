package tr.com.khg.services.gateway.model.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerConfigurationResponse {
  private Long id;
  private String name;
  private Integer failureRateThreshold;
  private Integer slowCallRateThreshold;
  private Long slowCallDurationThreshold;
  private Integer permittedNumberOfCallsInHalfOpenState;
  private Integer slidingWindowSize;
  private Integer minimumNumberOfCalls;
  private Long waitDurationInOpenState;
  private Boolean automaticTransitionFromOpenToHalfOpenEnabled;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
