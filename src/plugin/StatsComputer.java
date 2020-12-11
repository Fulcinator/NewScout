package plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StatsComputer {
	private static StatsComputer sc = null;
	private Map<String, Stats> stats;
	
	private StatsComputer() {
		stats = new HashMap<String, Stats>();
		GamificationUtils.parseStats(GamificationUtils.loadStats(), stats);
	}
	
	public static StatsComputer getInstance() {
		if(sc == null)
			sc = new StatsComputer();
		return sc;
	}
	
	public Stats getStats(String tester_id) {
		return stats.get(tester_id);
	}
	
	public Map<String, Stats> getStatsMap() {
		return stats;
	}
	
	public void computeStats(Session s) {				
		if(!stats.containsKey(s.getTesterId())) {
			Stats st = new Stats(s.getTesterId());
			computeTime(st, s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.setHLWidgets(st.getTotHLWidgets() + s.getTotHLWidgets());
			addAvgCoverage(s.getCoverage(), st);
			st.setGlobalAvgCoverage(computeAvgCoverage(st));
			stats.put(s.getTesterId(), st);
		}
		else {
			Stats st = getStats(s.getTesterId());
			computeTime(st, s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.setHLWidgets(st.getTotHLWidgets() + s.getTotHLWidgets());
			addAvgCoverage(s.getCoverage(), st);
			st.setGlobalAvgCoverage(computeAvgCoverage(st));
		}
		
		//DEBUG
		for(Stats e : stats.values())
			System.out.println(e.prepareStats());
	}
	
	public void computeTime(Stats st, int min, int sec) {
		int m = st.getMinutes() + min;
		int s = st.getSeconds() + sec;
		m += s / 60;
		s = s % 60;
		st.setMinutes(m);
		st.setSeconds(s);
	}
	
	public void addAvgCoverage(ArrayList<Double> cov, Stats st) {
		double sum = 0.0;
		for(double e : cov)
			sum += e;
		if(cov.size() != 0) { 
			// se � vuoto non lo aggiunge nemmeno, non dovrebbe servire ma meglio evitare di avere dei NaN
			double newAvg = sum/(cov.size());
			st.addAvgCoverage(newAvg); 
		}
	}
	
	public double computeAvgCoverage(Stats st) {
		double sum = 0.0;
		for(double e : st.getAvgCoverage())
			sum += e;
		if(st.getAvgCoverage().size() != 0) {
			//come prima, non dovrebbe essere necessario ma meglio evitare casi particolari non noti
			return (sum / (st.getAvgCoverage().size()));
		}
		else
			return 0.0;
	}
}
