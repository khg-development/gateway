package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_fallback_headers_filter")
public class RouteFallbackHeadersFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "execution_exception_type_header_name")
  private String executionExceptionTypeHeaderName;

  @Column(name = "execution_exception_message_header_name")
  private String executionExceptionMessageHeaderName;

  @Column(name = "root_cause_exception_type_header_name")
  private String rootCauseExceptionTypeHeaderName;

  @Column(name = "root_cause_exception_message_header_name")
  private String rootCauseExceptionMessageHeaderName;
}
