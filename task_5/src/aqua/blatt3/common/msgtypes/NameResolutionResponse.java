package aqua.blatt3.common.msgtypes;

import java.io.Serializable;
import java.net.InetSocketAddress;

@SuppressWarnings("serial")
public class NameResolutionResponse implements Serializable {

    private final String requestId;
    private final InetSocketAddress sourceAddress;
    private final InetSocketAddress targetAddress;

    public NameResolutionResponse(String requestId, InetSocketAddress sourceAddress, InetSocketAddress targetAddress) {
        this.requestId = requestId;
        this.sourceAddress = sourceAddress;
        this.targetAddress = targetAddress;
    }

    public String getRequestId() {
        return requestId;
    }

    public InetSocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public InetSocketAddress getTargetAddress() {
        return targetAddress;
    }
}
