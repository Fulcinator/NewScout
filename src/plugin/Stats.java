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
	private int newWidgets;
	private int newPages;
	private int score;
	private int bonus;
	private String grade;
	private int totscore;
	
	public Stats(String tester_id) {
		this.tester_id = tester_id;
		minutes = 0;
		seconds = 0;
		issues = 0;
		newWidgets = 0;
		totHighlightedWidgets = 0;
		newPages = 0;
		globalAvgCoverage = 0.0;
		globalEEPercentage = 0.0;
		score = 0;
		totscore = 0;
		bonus = 0;
		grade = "";
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
	
	public double getGlobalAvgCoverage() {
		return globalAvgCoverage;
	}
	
	public int getIssues() {
		return issues;
	}
	
	public int getNewWidgets() {
		return newWidgets;
	}
	
	public int getNewPages() {
		return newPages;
	}
	
	public ArrayList<Double> getEEPercentages() {
		return avgEEPercentages;
	}
	
	public double getGlobalEEPercentage() {
		return globalEEPercentage;
	}
	
	public String getTesterId() {
		return tester_id;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getBonus() {
		return bonus;
	}
	
	public String getGrade() {
		return grade;
	}
	
	public int getTotScore() {
		return totscore;
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
	
	public void setNewWidgets(int w) {
		newWidgets = w; 
	}
	
	public void setNewPages(int p) {
		newPages = p;
	}
	
	public void setScore(int s) {
		score = s;
	}
	
	public void setBonus(int b) {
		bonus = b;
	}
	
	public void setGrade(String g) {
		grade = g;
	}
	
	public void setTotScore(int s) {
		totscore = s;
	}
	
	public String prepareStats() {
		/**
		 * chiamato da saveStats per organizzare le cose da scrivere
		 */
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
		toReturn += "NEWW " + ": " + newWidgets + System.lineSeparator(); 
		toReturn += "NEWP " + ": " + newPages + System.lineSeparator(); 
		toReturn += "SCO " + ": " + totscore + System.lineSeparator();
		toReturn += "ENDUSER" + System.lineSeparator();
		return toReturn;
	}
}
