package aqua.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToggleController implements ActionListener {

    private final TankModel model;

    public ToggleController(TankModel model) {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.locateFishGlobally(e.getActionCommand());
    }
}
