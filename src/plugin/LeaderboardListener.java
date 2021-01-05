package plugin;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;

public class LeaderboardListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
	        LeaderboardDialog dialog = new LeaderboardDialog(GamificationUtils.getLeaderboard());
	        dialog.setTitle("Leaderboard");
	        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
	        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	        dialog.setVisible(true);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

}
