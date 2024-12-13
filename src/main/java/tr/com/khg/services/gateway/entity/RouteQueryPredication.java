package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_query_predications")
public class RouteQueryPredication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_name", nullable = false)
    private String param;

    @Column(name = "param_value_regexp")
    private String regexp;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}
