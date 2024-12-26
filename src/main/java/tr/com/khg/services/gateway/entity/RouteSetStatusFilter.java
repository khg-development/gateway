package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_set_status_filter")
public class RouteSetStatusFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HttpStatus status;
}
