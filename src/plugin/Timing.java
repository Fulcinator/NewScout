package plugin;

public class Timing {
	private long beginMillis;
	private long endMillis;
	private long totMillis;
	
	public Timing() {
		beginMillis = 0;
		endMillis = 0;
		totMillis = 0;		
	}
	
	public void setBeginTime() {
		beginMillis = System.currentTimeMillis();
		//System.out.println("Inizio sessione");
	}
	
	public void setEndTime() {
		endMillis = System.currentTimeMillis();
		//System.out.println("Fine sessione");
	}	
	
	public long computeTime() {
		//System.out.println(endMillis);
		totMillis = endMillis - beginMillis;
		return totMillis;
		//System.out.println("Differenza: " + totMillis);
		
		/*seconds = time/1000.0;
		minutes = seconds/60.0;
		seconds = seconds%60.0;*/
	}
	
	public Boolean isReady() {
		if(totMillis == 0)
			return false;
		return true;
	}
	
	public Boolean hasStopped() {
		if(endMillis == 0)
			return false;
		return true;
	}
	
	public Boolean hasStarted() {
		if(beginMillis == 0)
			return false;
		return true;
	}
	
	public long getMillis() {
		return totMillis;
	}
	
	public int getMinutes() {
		int seconds = (int) (totMillis/1000.0); 
		return seconds/60;
	}
	
	public int getSeconds() {
		int seconds = (int) (totMillis/1000.0); 
		return seconds % 60;
	}
	
	public String toString() {
		//return ("The session lasted " + (int) getMinutes() + "m " + (int) getSeconds() + "s.");
		if(isReady())
			return ("The session lasted " + (int) getMillis() + " milliseconds");
		return super.toString();
	}
}
