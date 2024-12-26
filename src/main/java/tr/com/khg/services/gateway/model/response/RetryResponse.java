package tr.com.khg.services.gateway.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryResponse {
  private Integer retries;
  private List<String> statuses;
  private List<String> methods;
  private List<String> series;
  private Long firstBackoff;
  private Long maxBackoff;
  private Integer factor;
  private Boolean basedOnPreviousValue;
}
