package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteCookiePredication;
import tr.com.khg.services.gateway.model.response.CookiePredicationResponse;

@Component
public class CookiePredicationMapper
    implements BaseMapper<RouteCookiePredication, CookiePredicationResponse> {
  @Override
  public RouteCookiePredication toEntity(CookiePredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CookiePredicationResponse toDto(RouteCookiePredication entity) {
    if (entity == null) {
      return null;
    }
    return CookiePredicationResponse.builder()
        .name(entity.getName())
        .regexp(entity.getRegexp())
        .build();
  }
}
