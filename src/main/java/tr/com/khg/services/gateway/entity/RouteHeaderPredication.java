package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_header_predications")
public class RouteHeaderPredication {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "header_name", nullable = false)
  private String name;

  @Column(name = "header_value_regexp", nullable = false)
  private String regexp;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;
}
