package aqua.blatt3.common.msgtypes;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SnapshotToken implements Serializable {

    private int value = 0;

    public void add(int value) {
        this.value += value;
    }

    public int getValue() {
        return value;
    }
}
