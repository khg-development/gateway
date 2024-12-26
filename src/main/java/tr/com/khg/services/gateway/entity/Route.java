package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.cloud.gateway.route.RouteDefinition;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "routes")
public class Route implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "route_id", unique = true, nullable = false)
  private String routeId;

  @Column(name = "route_definition", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private RouteDefinition routeDefinition;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "match_trailing_slash", nullable = false)
  private boolean matchTrailingSlash = true;

  @Column(name = "body_log_enabled", nullable = false)
  private boolean bodyLogEnabled = true;

  @Column(name = "preserve_host_header")
  private boolean preserveHostHeader = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false)
  private HttpMethods method;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "api_proxy_id", nullable = false)
  private ApiProxy apiProxy;

  @Column(name = "activation_time")
  private ZonedDateTime activationTime;

  @Column(name = "expiration_time")
  private ZonedDateTime expirationTime;

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteCookiePredication> routeCookiePredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteHeaderPredication> routeHeaderPredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteHostPredication> routeHostPredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteQueryPredication> routeQueryPredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRemoteAddrPredication> routeRemoteAddrPredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteWeightPredication> routeWeightPredications = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteXForwardedRemoteAddrPredication> routeXForwardedRemoteAddrPredications =
      new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteAddRequestHeaderFilter> routeAddRequestHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteAddRequestHeaderIfNotPresentFilter> routeAddRequestHeaderIfNotPresentFilters =
      new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteAddRequestParameterFilter> routeAddRequestParameterFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteAddResponseHeaderFilter> routeAddResponseHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteCircuitBreakerFilter> routeCircuitBreakerFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteDedupeResponseHeaderFilter> routeDedupeResponseHeaderFilters =
      new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteFallbackHeadersFilter> routeFallbackHeadersFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteLocalResponseCacheFilter> routeLocalResponseCacheFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteMapRequestHeaderFilter> routeMapRequestHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RoutePrefixPathFilter> routePrefixPathFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRedirectToFilter> routeRedirectToFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRemoveJsonAttributesResponseBodyFilter> routeRemoveJsonAttributesResponseBodyFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRemoveRequestHeaderFilter> routeRemoveRequestHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRemoveRequestParameterFilter> routeRemoveRequestParameterFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRemoveResponseHeaderFilter> routeRemoveResponseHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRequestHeaderSizeFilter> routeRequestHeaderSizeFilters = new ArrayList<>();

  @OneToOne(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private RouteRequestRateLimiterFilter routeRequestRateLimiterFilter;

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRewriteLocationResponseHeaderFilter> routeRewriteLocationResponseHeaderFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRewritePathFilter> routeRewritePathFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRewriteRequestParameterFilter> routeRewriteRequestParameterFilters = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteRewriteResponseHeaderFilter> routeRewriteResponseHeaderFilters = new ArrayList<>();

  public void clearPredications() {
    routeCookiePredications.clear();
    routeHeaderPredications.clear();
    routeHostPredications.clear();
    routeQueryPredications.clear();
    routeRemoteAddrPredications.clear();
    routeWeightPredications.clear();
    routeXForwardedRemoteAddrPredications.clear();
  }

  public void clearFilters() {
    routeAddRequestHeaderFilters.clear();
    routeAddRequestHeaderIfNotPresentFilters.clear();
    routeAddRequestParameterFilters.clear();
    routeAddResponseHeaderFilters.clear();
    routeCircuitBreakerFilters.clear();
    routeDedupeResponseHeaderFilters.clear();
    routeFallbackHeadersFilters.clear();
    routeLocalResponseCacheFilters.clear();
    routeMapRequestHeaderFilters.clear();
    routePrefixPathFilters.clear();
    routeRedirectToFilters.clear();
    routeRemoveJsonAttributesResponseBodyFilters.clear();
    routeRemoveRequestHeaderFilters.clear();
    routeRemoveRequestParameterFilters.clear();
    routeRemoveResponseHeaderFilters.clear();
    routeRequestHeaderSizeFilters.clear();
    routeRequestRateLimiterFilter = null;
    routeRewriteLocationResponseHeaderFilters.clear();
    routeRewritePathFilters.clear();
    routeRewriteRequestParameterFilters.clear();
    routeRewriteResponseHeaderFilters.clear();
  }
}
