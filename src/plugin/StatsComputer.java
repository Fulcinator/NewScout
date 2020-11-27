package plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StatsComputer {
	private static StatsComputer sc = null;
	private Map<String, Stats> stats;
	
	private StatsComputer() {
		Map<String, Stats> stats = new HashMap<String, Stats>();
	}
	
	public StatsComputer getInstance() {
		if(sc == null)
			sc = new StatsComputer();
		return sc;
	}
	
	public Stats getStats(String tester_id) {
		return stats.get(tester_id);
	}
	
	public void computeStats(Session s) {
		if(!stats.containsKey(s.getTesterId())) {
			Stats st = new Stats();
			st.addTime(s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.addHLWidgets(s.getTotNodes());
			stats.put(s.getTesterId(), st);
		}
		else {
			Stats st = getStats(s.getTesterId());
			st.addTime(s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.addHLWidgets(s.getTotNodes());
		}
	}
}
