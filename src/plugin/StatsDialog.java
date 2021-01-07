package plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StatsDialog extends JDialog {

		public StatsDialog(Map<String,String> stats) {
			
			//Preparazione testo
			String names = "";
			String values = "";
			Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();
			while (iterator.hasNext()) {
		        Map.Entry<String, String> entry = iterator.next();
		        
		        if(entry.getKey().equals("STATS"))
		        	names += "Tester ID\r\n\r\n";
		        if(entry.getKey().equals("MIN"))
		        	names += "Total Minutes\r\n\r\n";
		        if(entry.getKey().equals("SEC"))
		        	names += "Total Seconds\r\n\r\n";
		        if(entry.getKey().equals("HLW"))
		        	names += "Total Higlighted Widgets\r\n\r\n";
		        if(entry.getKey().equals("ISS"))
		        	names += "Total Reported Issues\r\n\r\n";
		        if(entry.getKey().equals("AVGC"))
		        	names += "Average Coverage\r\n\r\n";
		        if(entry.getKey().equals("EEP"))
		        	names += "Easter Eggs Found (%)\r\n\r\n";
		        if(entry.getKey().equals("NEWW"))
		        	names += "Total Widgets Discovered\r\n\r\n";
		        if(entry.getKey().equals("NEWP"))
		        	names += "Total Pages Discovered\r\n\r\n";
		        if(entry.getKey().equals("SCO"))
		        	names += "Total Score\r\n\r\n";
		        
		        if(entry.getKey().equals("AVGC") || entry.getKey().equals("EEP")) {
		        	DecimalFormat df = new DecimalFormat("#.##");
		        	values += String.valueOf(df.format(Double.parseDouble(entry.getValue()))) + "%\r\n\r\n";
		        }else
		        	if(entry.getKey().equals("VAL") || entry.getKey().equals("VAL2"))
			        	continue;
		        	else
		        		values += entry.getValue() + "\r\n\r\n";
			}
			
			//Panel esterno
			JPanel ext = new JPanel();
			ext.setPreferredSize(new Dimension(420,420));
			ext.setMaximumSize(new Dimension (420,420));
			ext.setMinimumSize(new Dimension (420,420));
			ext.setLayout(new GridLayout(0,2,0,3));
			ext.setBackground(new Color(204, 239, 255));
			
			//Panel statistiche
			JTextPane statsnames = new JTextPane();
			statsnames.setText(names);
			statsnames.setBackground(new Color(204, 239, 255));
			statsnames.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
			statsnames.setFont(new Font("Arial", Font.PLAIN, 16));
			statsnames.setEditable(false);
			ext.add(statsnames);
			
			//Panel valori
			JTextPane v = new JTextPane();
			v.setText(values);
			v.setBackground(new Color(204, 239, 255));
			v.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
			v.setFont(new Font("Arial", Font.PLAIN, 16));
			StyledDocument doc = v.getStyledDocument();
			SimpleAttributeSet center = new SimpleAttributeSet();
			StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
			doc.setParagraphAttributes(0, doc.getLength(), center, false);
			v.setEditable(false);
			ext.add(v);
			
			this.add(ext);
			this.setSize(440,440);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
		}
}
