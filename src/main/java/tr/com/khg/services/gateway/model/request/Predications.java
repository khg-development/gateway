package tr.com.khg.services.gateway.model.request;

import java.util.List;
import lombok.Data;

@Data
public class Predications {
  private List<CookiePredication> cookies;
  private List<HeaderPredication> headers;
  private List<HostPredication> hosts;
  private List<QueryPredication> queries;
  private List<RemoteAddrPredication> remoteAddresses;
  private List<WeightPredication> weights;
  private List<XForwardedRemoteAddrPredication> xforwardedRemoteAddresses;
}
