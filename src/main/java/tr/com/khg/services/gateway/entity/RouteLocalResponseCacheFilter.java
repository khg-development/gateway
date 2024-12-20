package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.com.khg.services.gateway.entity.enums.NoCacheStrategy;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_local_response_cache_filter")
public class RouteLocalResponseCacheFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "size")
  private String size;

  @Column(name = "time_to_live")
  private String timeToLive;

  @Enumerated(EnumType.STRING)
  @Column(name = "no_cache_strategy")
  private NoCacheStrategy noCacheStrategy = NoCacheStrategy.SKIP_UPDATE_CACHE_ENTRY;
}
