package plugin;

import java.util.ArrayList;

public class Stats {
	private int minutes;
	private int seconds;
	private int totHighlightedWidgets;
	private ArrayList<Double> avgCoverages;
	
	public Stats() {
		minutes = 0;
		seconds = 0;
		totHighlightedWidgets = 0;
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
	
	public void addAvgCoverage(double avg) {
		avgCoverages.add(avg);
	}
}
