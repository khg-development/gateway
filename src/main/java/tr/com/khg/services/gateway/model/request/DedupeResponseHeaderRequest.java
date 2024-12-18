package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.DedupeStrategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupeResponseHeaderRequest {
  private String name;
  private DedupeStrategy strategy = DedupeStrategy.RETAIN_FIRST;
}
