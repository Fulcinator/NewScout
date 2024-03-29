package plugin;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.WindowConstants;

public class StatsListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
	        StatsDialog dialog = new StatsDialog(GamificationUtils.getStats());
	        dialog.setTitle("Statistics");
	        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
	        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	        dialog.setVisible(true);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

}
