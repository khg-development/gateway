package tr.com.khg.services.gateway.config;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class KeyResolversConfiguration {

  private static final String ROUTE_ATTRIBUTE =
      "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute";
  private static final String DEFAULT_HEADER_NAME = "X-Rate-Limit-Key";

  @Bean
  @Primary
  public KeyResolver principalNameKeyResolver() {
    return new PrincipalNameKeyResolver();
  }

  @Bean
  public KeyResolver ipKeyResolver() {
    return exchange ->
        Optional.ofNullable(exchange.getRequest().getRemoteAddress())
            .map(remoteAdd -> Mono.just(remoteAdd.getAddress().getHostAddress()))
            .orElse(Mono.empty());
  }

  @Bean
  public KeyResolver headerKeyResolver() {
    return exchange -> {
      Route route = exchange.getAttribute(ROUTE_ATTRIBUTE);
      String headerName =
          Optional.ofNullable(route)
              .map(r -> (String) r.getMetadata().getOrDefault("rate-limiter-header", DEFAULT_HEADER_NAME))
              .orElse(DEFAULT_HEADER_NAME);
      return Mono.justOrEmpty(
          Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(headerName)));
    };
  }
}
