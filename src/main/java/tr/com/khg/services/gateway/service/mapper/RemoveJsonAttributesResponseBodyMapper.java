package tr.com.khg.services.gateway.service.mapper;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRemoveJsonAttributesResponseBodyFilter;
import tr.com.khg.services.gateway.model.response.RemoveJsonAttributesResponseBodyResponse;

@Component
public class RemoveJsonAttributesResponseBodyMapper
    implements BaseMapper<
        RouteRemoveJsonAttributesResponseBodyFilter, RemoveJsonAttributesResponseBodyResponse> {
  @Override
  public RouteRemoveJsonAttributesResponseBodyFilter toEntity(
      RemoveJsonAttributesResponseBodyResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RemoveJsonAttributesResponseBodyResponse toDto(
      RouteRemoveJsonAttributesResponseBodyFilter entity) {
    if (entity == null) {
      return null;
    }
    return RemoveJsonAttributesResponseBodyResponse.builder()
        .attributes(
            entity.getAttributes() != null
                ? Arrays.asList(entity.getAttributes().split(","))
                : null)
        .recursive(entity.isRecursive())
        .build();
  }
}
