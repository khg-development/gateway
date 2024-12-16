package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_add_request_parameter_filters")
public class RouteAddRequestParameterFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parameter_name", nullable = false)
    private String name;

    @Column(name = "parameter_value", nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}
