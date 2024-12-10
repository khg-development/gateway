package tr.com.khg.services.gateway.model;

import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.FilterType;

@Data
public class HeaderConfiguration {
  private String key;
  private String value;
  private FilterType type;
}
