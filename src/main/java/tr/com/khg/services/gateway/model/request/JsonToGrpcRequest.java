package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonToGrpcRequest {
  private String protoDescriptor;
  private String protoFile;
  private String serviceName;
  private String methodName;
}
