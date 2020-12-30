package plugin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
		JButton button = new JButton("Ok");
		
		//Preparazione testo
		String testoLabel = "Tester: " + stats.get("Tester");
		String testoStats = "";
		String testoResults = "";
		Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry<String, String> entry = iterator.next();
	        if(!entry.getKey().equals("Tester")) {
	        	testoStats += entry.getKey() + "\r\n\r\n";
		        testoResults += entry.getValue() + "\r\n\r\n";
	        }
	        //System.out.println(entry.getKey() + ":" + entry.getValue());
	    }
		
		//Panel esterno
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(400,400));
		panel.setMaximumSize(new Dimension (400,400));
		panel.setMinimumSize(new Dimension (400,400));
		panel.setBorder(BorderFactory.createBevelBorder(1));
		panel.setBackground(new Color(204, 239, 255));
		
		//Label Tester
		JLabel label = new JLabel(testoLabel, JLabel.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		label.setFont(new Font("Arial", Font.BOLD, 16));
		//label.setBackground(Color.RED);
		panel.add(label, BorderLayout.PAGE_START);
		
		//Panel interno che racchiude tutte le statistiche
		JPanel textpanel = new JPanel();
		textpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		textpanel.setBackground(new Color(204, 239, 255));
		textpanel.setLayout(new GridLayout(0,2,3,0));
		
		//Panel Nomi Statistiche
		JTextPane statsarea = new JTextPane();
		statsarea.setText(testoStats);
		statsarea.setBackground(new Color(204, 239, 255));
		statsarea.setFont(new Font("Arial", Font.PLAIN, 14));
		statsarea.setEditable(false);
		textpanel.add(statsarea);
		
		//Panel Risultati
		JTextPane resultsarea = new JTextPane();
		resultsarea.setText(testoResults);
		resultsarea.setBackground(new Color(204, 239, 255));
		resultsarea.setFont(new Font("Arial", Font.PLAIN, 14));
		StyledDocument doc = resultsarea.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		resultsarea.setEditable(false);
		textpanel.add(resultsarea);
		
		panel.add(textpanel, BorderLayout.CENTER);
		
		Box box = new Box(BoxLayout.Y_AXIS);
        //box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(panel);
        box.add(Box.createVerticalGlue());
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Session Recap");
		frame.add(box);
		//frame.pack();
		frame.setSize(500,450);
		frame.setResizable(false);
		frame.getContentPane().setBackground(new Color(221,234,248));
		
		ImageIcon image = new ImageIcon("icons//eye16.png");
		frame.setIconImage(image.getImage());
		
		frame.setVisible(true);
	}
	
}
