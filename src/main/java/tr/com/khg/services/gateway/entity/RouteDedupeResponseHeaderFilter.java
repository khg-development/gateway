package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.com.khg.services.gateway.entity.enums.DedupeStrategy;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_dedupe_response_header_filter")
public class RouteDedupeResponseHeaderFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "strategy")
  private DedupeStrategy strategy = DedupeStrategy.RETAIN_FIRST;
}
