package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_add_request_header_if_not_present_filters")
public class RouteAddRequestHeaderIfNotPresentFilter {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "header_name", nullable = false)
  private String name;

  @Column(name = "header_value", nullable = false)
  private String value;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;
}
