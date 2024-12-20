package tr.com.khg.services.gateway.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class WebApplicationConfiguration {

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(List.of("http://localhost:5173"));
    corsConfig.setMaxAge(3600L);
    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    corsConfig.setAllowedHeaders(List.of("*"));
    corsConfig.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  //  @Bean
  //  public RouteLocator routes(RouteLocatorBuilder builder) {
  //    return builder.routes()
  //        .route("rewrite_response_upper", r -> r.host("*.rewriteresponseupper.org")
  //            .filters(f -> f.prefixPath("/httpbin")
  //                .localResponseCache(Duration.ofMinutes(30), DataSize.of(500, Unit.MEGABYTES))
  //            ).uri("uri"))
  //        .build();
  //  }

}
