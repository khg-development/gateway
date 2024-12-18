package tr.com.khg.services.gateway.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.DedupeStrategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupeResponseHeaderResponse {
  private String name;
  private DedupeStrategy strategy;
}
