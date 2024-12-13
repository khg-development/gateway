package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_remote_addr_predications")
public class RouteRemoteAddrPredication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source", nullable = false)
    private String source;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}
