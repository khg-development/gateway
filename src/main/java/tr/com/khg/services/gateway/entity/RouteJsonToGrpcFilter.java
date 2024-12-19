package tr.com.khg.services.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_json_to_grpc_filter")
public class RouteJsonToGrpcFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;

  @Column(name = "proto_descriptor")
  private String protoDescriptor;

  @Column(name = "proto_file")
  private String protoFile;

  @Column(name = "service_name")
  private String serviceName;

  @Column(name = "method_name")
  private String methodName;
}
