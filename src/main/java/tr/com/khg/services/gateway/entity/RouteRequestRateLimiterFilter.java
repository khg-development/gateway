package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.com.khg.services.gateway.entity.enums.KeyResolvers;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_request_rate_limiter_filter")
public class RouteRequestRateLimiterFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "route_id", unique = true)
  private Route route;

  @Column(name = "replenish_rate", nullable = false)
  private Integer replenishRate;

  @Column(name = "burst_capacity", nullable = false)
  private Integer burstCapacity;

  @Column(name = "requested_tokens", nullable = false)
  private Integer requestedTokens = 1;

  @Column(name = "key_resolver")
  @Enumerated(EnumType.STRING)
  private KeyResolvers keyResolver = KeyResolvers.PRINCIPAL_NAME;

  @Column(name = "header_name")
  private String headerName;
}
