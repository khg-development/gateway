package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteXForwardedRemoteAddrPredication;
import tr.com.khg.services.gateway.model.response.XForwardedRemoteAddrPredicationResponse;

@Component
public class XForwardedRemoteAddrPredicationMapper
    implements BaseMapper<
        RouteXForwardedRemoteAddrPredication, XForwardedRemoteAddrPredicationResponse> {
  @Override
  public RouteXForwardedRemoteAddrPredication toEntity(
      XForwardedRemoteAddrPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public XForwardedRemoteAddrPredicationResponse toDto(
      RouteXForwardedRemoteAddrPredication entity) {
    if (entity == null) {
      return null;
    }
    return XForwardedRemoteAddrPredicationResponse.builder().source(entity.getSource()).build();
  }
}
