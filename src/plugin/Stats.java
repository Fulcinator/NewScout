package plugin;

public class Stats {
	private int minutes;
	private int seconds;
	private int totHighlightedWidgets;
	
	public Stats() {
		
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
	
	public void addTime(int min, int sec) {
		minutes += min;
		seconds += sec;
	}
	
	public void addHLWidgets(int w) {
		totHighlightedWidgets += w;
	}
}
