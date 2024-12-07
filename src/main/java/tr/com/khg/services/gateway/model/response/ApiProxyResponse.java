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
public class ApiProxyResponse {
  private Long id;
  private String name;
  private String uri;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
