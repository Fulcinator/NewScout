package plugin;

import java.util.ArrayList;

public class Stats {
	private String tester_id;
	private int minutes;
	private int seconds;
	private int totHighlightedWidgets;
	private ArrayList<Double> avgCoverages;
	private double globalAvgCoverage;
	private int issues;
	private ArrayList<Double> avgEEPercentages;
	private double globalEEPercentage;
	
	public Stats(String tester_id) {
		this.tester_id = tester_id;
		minutes = 0;
		seconds = 0;
		issues = 0;
		totHighlightedWidgets = 0;
		globalAvgCoverage = 0.0;
		globalEEPercentage = 0.0;
		avgCoverages = new ArrayList<Double>();
		avgEEPercentages = new ArrayList<Double>();
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public int getSeconds() {
		return seconds;
	}
	
	public int getTotHLWidgets() {
		return totHighlightedWidgets;
	}
	
	public ArrayList<Double> getAvgCoverage() {
		return avgCoverages;
	}
	
	public int getIssues() {
		return issues;
	}
	
	public ArrayList<Double> getEEPercentages() {
		return avgEEPercentages;
	}
	
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	
	public void setHLWidgets(int tot) {
		totHighlightedWidgets = tot;
	}
	
	public void setGlobalAvgCoverage(double avg) {
		globalAvgCoverage = avg;
	}
	
	public void addAvgCoverage(double avg) {
		avgCoverages.add(avg);
	}
	
	public void setGlobalEEP(double avg) {
		globalEEPercentage = avg;
	}
	
	public void addAvgEEP(double eep) {
		avgEEPercentages.add(eep);
	}
	
	public void setIssues(int i) {
		this.issues = i;
	}
	
	public String prepareStats() {
		if(tester_id.equals(""))
			return "";
		String toReturn = "STATS " + ": " + tester_id + System.lineSeparator();
		toReturn += "MIN " + ": " + minutes + System.lineSeparator();
		toReturn += "SEC " + ": " + seconds + System.lineSeparator();
		toReturn += "HLW " + ": " + totHighlightedWidgets + System.lineSeparator();
		toReturn += "AVGC " + ": " + globalAvgCoverage + System.lineSeparator();
		toReturn += "VAL " + ": "; 
		for(double d : avgCoverages)
			toReturn += d + "; ";
		toReturn += System.lineSeparator();
		toReturn += "ISS " + ": " + issues + System.lineSeparator();
		toReturn += "EEP " + ": " + globalEEPercentage + System.lineSeparator();
		toReturn += "VAL2 " + ": "; 
		for(double d : avgEEPercentages)
			toReturn += d + "; ";
		toReturn += System.lineSeparator();
		toReturn += "ENDUSER" + System.lineSeparator();
		return toReturn;
	}
}
