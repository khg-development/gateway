package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRemoteAddrPredication;
import tr.com.khg.services.gateway.model.response.RemoteAddrPredicationResponse;

@Component
public class RemoteAddrPredicationMapper
    implements BaseMapper<RouteRemoteAddrPredication, RemoteAddrPredicationResponse> {

  @Override
  public RouteRemoteAddrPredication toEntity(RemoteAddrPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RemoteAddrPredicationResponse toDto(RouteRemoteAddrPredication entity) {
    if (entity == null) {
      return null;
    }
    return RemoteAddrPredicationResponse.builder().source(entity.getSource()).build();
  }
}
