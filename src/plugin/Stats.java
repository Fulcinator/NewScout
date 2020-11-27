package plugin;

public class Stats {
	private int minutes;
	private int seconds;
	private int totHighlightedWidgets;
	private double averageCoverage;
	
	public Stats() {
		minutes = 0;
		seconds = 0;
		totHighlightedWidgets = 0;
		averageCoverage = 0;
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
	
	public double getAverageCoverage() {
		return averageCoverage;
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
	
	public void setAvgCoverage(double cov) {
		averageCoverage = cov;
	}
}
