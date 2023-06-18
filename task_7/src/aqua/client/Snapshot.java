package aqua.client;

public class Snapshot {

    private int value;

    private boolean finished = false;

    public Snapshot(int value) {
        this.value = value;
    }

    public void update(int value) {
        this.value += value;
    }

    public int getValue() {
        return value;
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }
}
