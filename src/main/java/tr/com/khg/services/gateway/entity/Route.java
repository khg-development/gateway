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
  private List<RouteHeaderConfiguration> routeHeaderConfigurations = new ArrayList<>();

  @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<RouteCookiePredication> routeCookiePredications = new ArrayList<>();
}
