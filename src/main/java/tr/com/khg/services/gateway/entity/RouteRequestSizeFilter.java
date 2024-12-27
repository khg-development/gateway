package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_request_size_filter")
public class RouteRequestSizeFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "max_size")
    private Long maxSize;
}
