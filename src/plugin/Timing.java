package plugin;

public class Timing {
	private long beginMillis;
	private long endMillis;
	private double minutes;
	private double seconds;
	
	public Timing() {
		this.beginMillis = 0;
		endMillis = 0;
		minutes = 0;
		seconds = 0;
	}
	
	public void setBeginTime() {
		beginMillis = System.currentTimeMillis();
		System.out.println("Inizio sessione");
	}
	
	public void computeTime(long timeE) {
		this.endMillis = timeE;
		System.out.println(endMillis);
		long time = this.endMillis - this.beginMillis;
		System.out.println("Differenza: " + time);
		seconds = time/1000.0;
		minutes = seconds/60.0;
		seconds = seconds%60.0;
	}
	
	public double getMinutes() {
		return minutes;
	}
	
	public double getSeconds() {
		return seconds;
	}
	
	public String toString() {
		return ("The session lasted " + (int)minutes + "m " + (int)seconds + "s.");
	}
}
