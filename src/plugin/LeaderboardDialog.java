package plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LeaderboardDialog extends JDialog  {
	
	public LeaderboardDialog(Map<String,Integer> leaderboard) {
		String userlist = GamificationUtils.getUserList(leaderboard);
		String scorelist = GamificationUtils.getScoreList(leaderboard);
		
		//Panel esterno
		JPanel ext = new JPanel();
		ext.setPreferredSize(new Dimension(370,420));
		ext.setMaximumSize(new Dimension (370,420));
		ext.setMinimumSize(new Dimension (370,420));
		ext.setLayout(new GridLayout(0,2,0,3));
		ext.setBackground(new Color(204, 239, 255));
		
		//Panel nomi
		JTextPane users = new JTextPane();
		users.setText(userlist);
		users.setBackground(new Color(204, 239, 255));
		users.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
		users.setFont(new Font("Arial", Font.PLAIN, 16));
		users.setEditable(false);
		ext.add(users);
		
		//Panel punteggi
		JTextPane scores = new JTextPane();
		scores.setText(scorelist);
		scores.setBackground(new Color(204, 239, 255));
		scores.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
		scores.setFont(new Font("Arial", Font.PLAIN, 16));
		StyledDocument doc = scores.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		scores.setEditable(false);
		ext.add(scores);
		
		this.add(ext);
		this.setSize(390,470);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		
	}
}
