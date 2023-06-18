package aqua.blatt3.common.msgtypes;

import java.io.Serializable;
import java.net.InetSocketAddress;

@SuppressWarnings("serial")
public class LocationUpdate implements Serializable {

    private final String id;
    private final InetSocketAddress address;

    public LocationUpdate(String id, InetSocketAddress address) {
        this.id = id;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
