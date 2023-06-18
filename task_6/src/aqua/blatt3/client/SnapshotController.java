package aqua.blatt3.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SnapshotController implements ActionListener {

    private final TankModel model;

    public SnapshotController(TankModel model) {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.initiateSnapshot();
    }
}
