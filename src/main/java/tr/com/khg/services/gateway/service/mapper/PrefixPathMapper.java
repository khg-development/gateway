package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RoutePrefixPathFilter;
import tr.com.khg.services.gateway.model.response.PrefixPathResponse;

@Component
public class PrefixPathMapper implements BaseMapper<RoutePrefixPathFilter, PrefixPathResponse> {
  @Override
  public RoutePrefixPathFilter toEntity(PrefixPathResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrefixPathResponse toDto(RoutePrefixPathFilter entity) {
    if (entity == null) {
      return null;
    }
    return PrefixPathResponse.builder().prefix(entity.getPrefix()).build();
  }
}
