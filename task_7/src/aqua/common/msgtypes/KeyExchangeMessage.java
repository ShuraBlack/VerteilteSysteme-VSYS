package aqua.common.msgtypes;

import java.io.Serializable;
import java.security.PublicKey;

@SuppressWarnings("serial")
public class KeyExchangeMessage implements Serializable {

    private final PublicKey key;

    public KeyExchangeMessage(PublicKey key) {
        this.key = key;
    }

    public PublicKey getKey() {
        return key;
    }
}
