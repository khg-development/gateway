package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRewriteLocationResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.RewriteLocationResponseHeaderResponse;

@Component
public class RewriteLocationResponseHeaderMapper
    implements BaseMapper<
        RouteRewriteLocationResponseHeaderFilter, RewriteLocationResponseHeaderResponse> {
  @Override
  public RouteRewriteLocationResponseHeaderFilter toEntity(
      RewriteLocationResponseHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RewriteLocationResponseHeaderResponse toDto(
      RouteRewriteLocationResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return RewriteLocationResponseHeaderResponse.builder()
        .stripVersionMode(entity.getStripVersionMode())
        .locationHeaderName(entity.getLocationHeaderName())
        .hostValue(entity.getHostValue())
        .protocolsRegex(entity.getProtocolsRegex())
        .build();
  }
}
