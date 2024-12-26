package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_set_path_filter")
public class RouteSetPathFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "template")
    private String template;
}
