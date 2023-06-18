package aqua.common.msgtypes;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LocationRequest implements Serializable {

    private final String id;

    public LocationRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
