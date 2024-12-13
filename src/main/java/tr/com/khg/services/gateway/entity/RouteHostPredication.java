package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_host_predications")
public class RouteHostPredication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_pattern", nullable = false)
    private String pattern;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}
