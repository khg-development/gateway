package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_circuit_breaker_filter")
public class RouteCircuitBreakerFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "name")
  private String name;

  @Column(name = "fallback_uri")
  private String fallbackUri;

  @Column(name = "status_codes")
  private String statusCodes;
}
