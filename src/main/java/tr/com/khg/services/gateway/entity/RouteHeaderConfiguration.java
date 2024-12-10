package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.com.khg.services.gateway.entity.enums.FilterType;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_header_configurations")
public class RouteHeaderConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "header_key", nullable = false)
  private String key;

  @Column(name = "header_value", nullable = false)
  private String value;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private FilterType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_id")
  private Route route;
}
