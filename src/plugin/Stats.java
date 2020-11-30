package plugin;

import java.util.ArrayList;

public class Stats {
	private String tester_id;
	private int minutes;
	private int seconds;
	private int totHighlightedWidgets;
	private ArrayList<Double> avgCoverages;
	private double globalAvgCoverage;
	
	public Stats(String tester_id) {
		this.tester_id = tester_id;
		minutes = 0;
		seconds = 0;
		totHighlightedWidgets = 0;
		globalAvgCoverage = 0.0;
		avgCoverages = new ArrayList<Double>();
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
	
	public String prepareStats() {
		String toReturn = "STATS " + ": " + tester_id + System.lineSeparator();
		toReturn += "MIN " + ": " + minutes + System.lineSeparator();
		toReturn += "SEC " + ": " + seconds + System.lineSeparator();
		toReturn += "HLW " + ": " + totHighlightedWidgets + System.lineSeparator();
		toReturn += "AVGC " + ": " + globalAvgCoverage + System.lineSeparator();
		for(double d : avgCoverages)
			toReturn += d + "; ";
		return toReturn;
	}
}
