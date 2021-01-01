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
		Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry<String, String> entry = iterator.next();
	        if(!entry.getKey().equals("Tester") && !entry.getKey().equals("Score") && !entry.getKey().equals("Grade")) {
	        	testoStats += entry.getKey() + "\r\n\r\n";
		        testoResults += entry.getValue() + "\r\n\r\n";
	        }
	        if(entry.getKey().equals("Score"))
	        	score += entry.getValue();
	        if(entry.getKey().equals("Grade"))
	        	grade += entry.getValue();
	    }
		
		//Panel esterno
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(400,500));
		panel.setMaximumSize(new Dimension (400,500));
		panel.setMinimumSize(new Dimension (400,500));
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
		sgpanel.setLayout(new GridLayout(2,0,3,0));
		sgpanel.setMaximumSize(new Dimension(400,120));
		
		//Panel score
		
		//TODO aggiungere punteggio base e bonus
		JPanel scorepanel = new JPanel();
		scorepanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		scorepanel.setBackground(new Color(204, 239, 255));
		scorepanel.setLayout(new GridLayout(0,2,3,0));
		JTextPane scorelabel = new JTextPane();
		scorelabel.setText("Score");
		scorelabel.setBackground(new Color(204, 239, 255));
		scorelabel.setFont(new Font("Arial", Font.BOLD, 12));
		scorelabel.setEditable(false);
		scorepanel.add(scorelabel);
		JTextPane scorearea = new JTextPane();
		scorearea.setText(score);
		scorearea.setBackground(new Color(204, 239, 255));
		scorearea.setFont(new Font("Arial", Font.BOLD, 16));
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
		gradelabel.setFont(new Font("Arial", Font.BOLD, 12));
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
		
		//Aggiunta pannelli interni a quello esterno
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
		frame.setSize(500,550);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.getContentPane().setBackground(new Color(221,234,248));
		
		ImageIcon image = new ImageIcon("icons//eye16.png");
		frame.setIconImage(image.getImage());
		
		frame.setVisible(true);
	}
	
}
