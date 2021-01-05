package plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

public class RecapGUI {

	public RecapGUI(Map<String,String> stats) {
		JFrame frame = new JFrame("Session Recap");
		JPanel panel = new JPanel();
		Color colorA = new Color(255, 179, 102);
		Color colorC = new Color(153, 255, 153);
		Color colorD = new Color(153, 153, 255);
		
		//Preparazione testo
		String testoLabel = "Tester: " + stats.get("Tester");
		String testoStats = "";
		String testoResults = "";
		String score = "";
		String grade = "";
		String bonus = "";
		Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry<String, String> entry = iterator.next();
	        if(!entry.getKey().equals("Tester") && !entry.getKey().equals("Score") && !entry.getKey().equals("Grade") && !entry.getKey().equals("Bonus Score")) {
	        	testoStats += entry.getKey() + "\r\n\r\n";
		        testoResults += entry.getValue() + "\r\n\r\n";
	        }
	        if(entry.getKey().equals("Score"))
	        	score += entry.getValue();
	        if(entry.getKey().equals("Grade"))
	        	grade += entry.getValue();
	        if(entry.getKey().equals("Bonus Score"))
	        	bonus += entry.getValue();
	    }
	    String tot = String.valueOf(Integer.parseInt(score) + Integer.parseInt(bonus));
		
		//Panel esterno
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(400,600));
		panel.setMaximumSize(new Dimension (400,600));
		panel.setMinimumSize(new Dimension (400,600));
		panel.setBorder(BorderFactory.createBevelBorder(1));
		panel.setBackground(new Color(204, 239, 255));
		
		//Label Tester
		JLabel label = new JLabel(testoLabel, JLabel.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		label.setFont(new Font("Arial", Font.BOLD, 16));
		panel.add(label, BorderLayout.PAGE_START);
		
		//Panel interno che racchiude tutte le statistiche
		JPanel textpanel = new JPanel();
		textpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		textpanel.setBackground(new Color(204, 239, 255));
		textpanel.setLayout(new GridLayout(0,2,3,0));
		textpanel.setMinimumSize(new Dimension (400,280));
		
		//Panel Nomi Statistiche
		JTextPane statsarea = new JTextPane();
		statsarea.setText(testoStats);
		statsarea.setBackground(new Color(204, 239, 255));
		statsarea.setFont(new Font("Arial", Font.PLAIN, 12));
		statsarea.setEditable(false);
		textpanel.add(statsarea);
		
		//Panel Risultati
		JTextPane resultsarea = new JTextPane();
		resultsarea.setText(testoResults);
		resultsarea.setBackground(new Color(204, 239, 255));
		resultsarea.setFont(new Font("Arial", Font.PLAIN, 12));
		StyledDocument doc = resultsarea.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		resultsarea.setEditable(false);
		textpanel.add(resultsarea);
		
		//Panel esterno score e grade
		JPanel sgpanel = new JPanel();
		sgpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		sgpanel.setBackground(new Color(204, 239, 255));
		sgpanel.setLayout(new GridLayout(3,0,3,0));
		sgpanel.setMaximumSize(new Dimension(400,120));
		
		//Panel score
		JPanel scorepanel = new JPanel();
		scorepanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		scorepanel.setBackground(new Color(204, 239, 255));
		scorepanel.setLayout(new GridLayout(0,2,3,0));
		JTextPane scorelabel = new JTextPane();
		scorelabel.setLayout(new GridLayout(2,0,3,0));
		JTextPane baseandbonus = new JTextPane();
		baseandbonus.setText("Base Score\r\nBonus Score\r\n");
		baseandbonus.setBackground(new Color(204, 239, 255));
		baseandbonus.setFont(new Font("Arial", Font.PLAIN, 12));
		baseandbonus.setEditable(false);
		scorelabel.add(baseandbonus);
		JTextPane totpanel = new JTextPane();
		totpanel.setText("Total Score");
		totpanel.setBackground(new Color(204, 239, 255));
		totpanel.setFont(new Font("Arial", Font.BOLD, 16));
		totpanel.setEditable(false);
		scorelabel.add(totpanel);
		scorelabel.setBackground(new Color(204, 239, 255));
		scorelabel.setEditable(false);
		scorepanel.add(scorelabel);
		JTextPane scorearea = new JTextPane();
		scorearea.setLayout(new GridLayout(2,0,3,0));
		JTextPane bbvalues = new JTextPane();
		bbvalues.setText(score + "\r\n" + bonus + "\r\n");
		bbvalues.setBackground(new Color(204, 239, 255));
		bbvalues.setFont(new Font("Arial", Font.PLAIN, 12));
		StyledDocument dbbv = bbvalues.getStyledDocument();
		SimpleAttributeSet cbbv = new SimpleAttributeSet();
		StyleConstants.setAlignment(cbbv, StyleConstants.ALIGN_RIGHT);
		dbbv.setParagraphAttributes(0, dbbv.getLength(), cbbv, false);
		bbvalues.setEditable(false);
		scorearea.add(bbvalues);
		JTextPane totvalue = new JTextPane();
		totvalue.setText(tot);
		totvalue.setBackground(new Color(204, 239, 255));
		totvalue.setFont(new Font("Arial", Font.BOLD, 16));
		StyledDocument dtot = totvalue.getStyledDocument();
		SimpleAttributeSet ctot = new SimpleAttributeSet();
		StyleConstants.setAlignment(ctot, StyleConstants.ALIGN_RIGHT);
		dtot.setParagraphAttributes(0, dtot.getLength(), ctot, false);
		totvalue.setEditable(false);
		scorearea.add(totvalue);
		scorearea.setBackground(new Color(204, 239, 255));
		StyledDocument d = scorearea.getStyledDocument();
		SimpleAttributeSet c = new SimpleAttributeSet();
		StyleConstants.setAlignment(c, StyleConstants.ALIGN_RIGHT);
		d.setParagraphAttributes(0, d.getLength(), c, false);
		scorearea.setEditable(false);
		scorepanel.add(scorearea);
		sgpanel.add(scorepanel);
		
		//Panel grade
		JPanel gradepanel = new JPanel();
		gradepanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		gradepanel.setBackground(new Color(204, 239, 255));
		gradepanel.setLayout(new GridLayout(0,2,3,0));
		JTextPane gradelabel = new JTextPane();
		gradelabel.setText("Grade");
		gradelabel.setBackground(new Color(204, 239, 255));
		gradelabel.setFont(new Font("Arial", Font.BOLD, 16));
		gradelabel.setEditable(false);
		gradepanel.add(gradelabel);
		JTextPane gradearea = new JTextPane();
		gradearea.setText(grade);
		gradearea.setBackground(new Color(204, 239, 255));
		//InputStream fontStream = Main.class.getResourceAsStream("/fonts/HarukazeSolid-6YeZM.ttf");
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts//HarukazeSolid-6YeZM.ttf"));
			ge.registerFont(customFont);
			customFont = customFont.deriveFont(Font.PLAIN, 65);
			gradearea.setFont(customFont);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		switch(grade) {
			case "S":
				gradearea.setForeground(Color.RED);
				break;
			case "A":
				gradearea.setForeground(colorA);
				break;
			case "B":
				gradearea.setForeground(Color.YELLOW);
				break;
			case "C":
				gradearea.setForeground(colorC);
				break;
			case "D":
				gradearea.setForeground(colorD);
				break;
			default:
				gradearea.setForeground(new Color(102, 0, 102));
		}
		StyledDocument _d = gradearea.getStyledDocument();
		SimpleAttributeSet ce = new SimpleAttributeSet();
		StyleConstants.setAlignment(ce, StyleConstants.ALIGN_RIGHT);
		_d.setParagraphAttributes(0, _d.getLength(), ce, false);
		gradearea.setEditable(false);
		gradepanel.add(gradearea);
		sgpanel.add(gradepanel);
		
		//Panel bottoni
		JPanel buttons = new JPanel();
		buttons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		buttons.setBackground(new Color(204, 239, 255));
		buttons.setLayout(new GridLayout(0,2,0,3));
		JPanel buttonPanel1 = new JPanel(new GridBagLayout());
		buttonPanel1.setBackground(new Color(204, 239, 255));
		JButton button = new JButton("See Stats");
		//button.addActionListener(new ButtonListener()); // Add event handler
		GridBagConstraints gc=new GridBagConstraints();
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.gridx=0;
		gc.gridy=0; 
		buttonPanel1.add(button,gc);
		JPanel buttonPanel2 = new JPanel(new GridBagLayout());
		buttonPanel2.setBackground(new Color(204, 239, 255));
		JButton button2 = new JButton("Leaderboard");
		button2.addActionListener(new LeaderboardListener()); // Add event handler
		GridBagConstraints gc2=new GridBagConstraints();
		gc2.fill=GridBagConstraints.HORIZONTAL;
		gc2.gridx=0;
		gc2.gridy=0; 
		buttonPanel2.add(button2,gc2);
		buttons.add(buttonPanel1);
		buttons.add(buttonPanel2);
		sgpanel.add(buttons);
		
		//Aggiunta pannelli a quello più esterno
		panel.add(textpanel, BorderLayout.CENTER);
		panel.add(sgpanel, BorderLayout.PAGE_END);
		
		//Frame principale
		Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createVerticalGlue());
        box.add(panel);
        box.add(Box.createVerticalGlue());
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Session Recap");
		frame.add(box);
		frame.setSize(500,650);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.getContentPane().setBackground(new Color(221,234,248));
		
		ImageIcon image = new ImageIcon("icons//eye16.png");
		frame.setIconImage(image.getImage());
		
		frame.setVisible(true);
	}
}
