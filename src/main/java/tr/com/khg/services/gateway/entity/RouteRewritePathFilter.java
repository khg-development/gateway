package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_rewrite_path_filter")
public class RouteRewritePathFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "regexp")
    private String regexp;

    @Column(name = "replacement")
    private String replacement;
}
