package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_retry_filter")
public class RouteRetryFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "retries")
    private Integer retries;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "route_retry_filter_statuses",
        joinColumns = @JoinColumn(name = "retry_filter_id"))
    @Column(name = "status")
//    @Enumerated(EnumType.STRING)
    private List<String> statuses = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "route_retry_filter_methods",
        joinColumns = @JoinColumn(name = "retry_filter_id"))
    @Column(name = "method")
//    @Enumerated(EnumType.STRING)
    private List<String> methods = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "route_retry_filter_series",
        joinColumns = @JoinColumn(name = "retry_filter_id"))
    @Column(name = "series")
//    @Enumerated(EnumType.STRING)
    private List<String> series = new ArrayList<>();

    @Column(name = "first_backoff")
    private Long firstBackoff;

    @Column(name = "max_backoff")
    private Long maxBackoff;

    @Column(name = "factor")
    private Integer factor;

    @Column(name = "based_on_previous_value")
    private Boolean basedOnPreviousValue;
}
