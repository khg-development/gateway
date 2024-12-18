package tr.com.khg.services.gateway.model.request;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerConfigurationRequest {
  private String name;
  private Integer failureRateThreshold = 50;
  private Integer slowCallRateThreshold = 100;
  private Long slowCallDurationThreshold = 60000L;
  private Integer permittedNumberOfCallsInHalfOpenState = 10;
  private Integer slidingWindowSize = 100;
  private Integer minimumNumberOfCalls = 100;
  private Long waitDurationInOpenState = 60000L;
  private Boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
  private SlidingWindowType slidingWindowType = SlidingWindowType.COUNT_BASED;
  private List<String> ignoreExceptions = new ArrayList<>();
  private List<String> recordExceptions = new ArrayList<>();
  private Long maxWaitDurationInHalfOpenState = 0L;
}
