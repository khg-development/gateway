package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_redirect_to_filter")
public class RouteRedirectToFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "status")
  private Integer status;

  @Column(name = "url")
  private String url;

  @Column(name = "include_request_params")
  private boolean includeRequestParams = false;
}
