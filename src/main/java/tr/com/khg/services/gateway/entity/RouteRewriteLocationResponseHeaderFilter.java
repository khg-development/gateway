package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.com.khg.services.gateway.entity.enums.StripVersionMode;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_rewrite_location_response_header_filter")
public class RouteRewriteLocationResponseHeaderFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "strip_version_mode")
    @Enumerated(EnumType.STRING)
    private StripVersionMode stripVersionMode = StripVersionMode.AS_IN_REQUEST;

    @Column(name = "location_header_name")
    private String locationHeaderName = "Location";

    @Column(name = "host_value")
    private String hostValue;

    @Column(name = "protocols_regex")
    private String protocolsRegex = "https?|ftps?";
}
