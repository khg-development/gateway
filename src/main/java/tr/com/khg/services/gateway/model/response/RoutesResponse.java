package tr.com.khg.services.gateway.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.route.RouteDefinition;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutesResponse {
  private List<RouteDefinition> routes;
}
