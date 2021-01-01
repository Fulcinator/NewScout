package plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class StatsComputer {
	private static StatsComputer sc = null;
	private Map<String, Stats> stats;
	
	private StatsComputer() {
		stats = new HashMap<String, Stats>();
		GamificationUtils.parseStats(GamificationUtils.loadStats("Gamification\\db.txt"), stats);
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
	
	public Stats computeStats(Session s) {
		if(!stats.containsKey(s.getTesterId())) {
			Stats st = new Stats(s.getTesterId());
			computeTime(st, s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.setHLWidgets(st.getTotHLWidgets() + s.getTotHLWidgets());
			addAvgCoverage(s.getCoverage(), st);
			st.setGlobalAvgCoverage(computeAvgCoverage(st));
			st.setIssues(st.getIssues() + s.getNIssue());
			addAvgEEP(s.getEasterEggPercentage(), st);
			st.setGlobalEEP(computeAvgEEP(st));
			st.setNewWidgets(st.getNewWidgets() + s.getTotalNewWidgets());
			computePagesDiscovered(st, s);
			
			stats.put(s.getTesterId(), st);
		}
		else {
			Stats st = getStats(s.getTesterId());
			computeTime(st, s.getTiming().getMinutes(), s.getTiming().getSeconds());
			st.setHLWidgets(st.getTotHLWidgets() + s.getTotHLWidgets());
			addAvgCoverage(s.getCoverage(), st);
			st.setGlobalAvgCoverage(computeAvgCoverage(st));
			st.setIssues(st.getIssues() + s.getNIssue());
			addAvgEEP(s.getEasterEggPercentage(), st);
			st.setGlobalEEP(computeAvgEEP(st));
			st.setNewWidgets(st.getNewWidgets() + s.getTotalNewWidgets());
			computePagesDiscovered(st, s);
		}
		
		//Creazione stats sessione appena conclusa
		Stats toReturn = new Stats(s.getTesterId());
		double sum = 0.0;
		
		toReturn.setMinutes(s.getTiming().getMinutes());
		toReturn.setSeconds(s.getTiming().getSeconds());
		toReturn.setHLWidgets(s.getTotHLWidgets());
		for(Double d : s.getCoverage())
			sum += d;
		toReturn.setGlobalAvgCoverage(sum/((double) s.getCoverage().size()));
		toReturn.setIssues(s.getNIssue());
		toReturn.setGlobalEEP(s.getEasterEggPercentage());
		toReturn.setNewWidgets(s.getTotalNewWidgets());
		toReturn.setNewPages(s.getPageDiscovered().size());
		ArrayList<Integer> score = computeScore(s);
		toReturn.setScore(score.get(0));
		toReturn.setBonus(score.get(1));
		toReturn.setGrade(computeGrade(toReturn.getScore() + toReturn.getBonus()));
		
		//DEBUG
		for(Stats e : stats.values())
			System.out.println(e.prepareStats());
		
		return toReturn;
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
			// se è vuoto non lo aggiunge nemmeno, non dovrebbe servire ma meglio evitare di avere dei NaN
			double newAvg = sum/(cov.size());
			st.addAvgCoverage(newAvg); 
		}
		else {
			st.addAvgCoverage(0.0);
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
	
	public void addAvgEEP(Double eep, Stats st) {
		st.addAvgEEP(eep);
	}
	
	public double computeAvgEEP(Stats st) {
		double sum = 0.0;
		for(double e : st.getEEPercentages())
			sum += e;
		if(st.getEEPercentages().size() != 0) {
			//come prima, non dovrebbe essere necessario ma meglio evitare casi particolari non noti
			return (sum / (st.getEEPercentages().size()));
		}
		else
			return 0.0;
	}
	
	public void computePagesDiscovered(Stats st, Session s) {
		//Caricamento db pagine
		ArrayList<String> discovered = s.getPageKnown();
		
		//Check pagine scoperte
		ArrayList<String> visited = s.getPageDiscovered();
		//riga cancellata perché non dovrebbero essercene
		//visited.removeAll(discovered);
		
		//Aggiunta a stats
		st.setNewPages(st.getNewPages() + visited.size());
		
		//Salvataggio db pagine
		discovered.addAll(visited);
		GamificationUtils.savePages(discovered);
	}
	
	public ArrayList<Integer> computeScore(Session s) {
		ArrayList<Integer> totscore = new ArrayList<>();
		//TODO parametrizzare i pesi
		double a = 0.6;
		double c = 0.1;
		double d = 0.4;
		double e = 0.1;
		
		int basescore = (int) (a*computeCovComp(s) + computeExComp(s) + c*computeEfComp(s));
		int bonusscore = (int) (d*computeTimeComp(s) + e*computeProbComp(s));
		totscore.add(0, basescore);
		totscore.add(1, bonusscore);
		
		return totscore;
	}
	
	public double computeCovComp(Session s) {
		double num = 0.0;
		
		for(Double d : s.getCoverage())
			num += d;
		
		return num / ((double) s.getTotalPageVisited());
	}
	
	public double computeExComp(Session s) {
		//TODO parametrizzare i pesi
		double k = 0.1;
		double h = 0.2;
		
		double B1 = k * (s.getPageDiscovered().size() / s.getTotalPageVisited());
		double B2 = h * (s.getTotalNewWidgets() / s.getTotHLWidgets());
		
		return B1 + B2;
		
	}
	
	public double computeEfComp(Session s) {
		return s.getNSessionInteraction() / s.getTotHLWidgets();
	}
	
	public double computeTimeComp(Session s) {
		double t = s.getTiming().getMinutes() + (s.getTiming().getSeconds()/60);
		
		double s_int = s.getSecondsPerInteraction();
		if(s_int<1 || s_int>30) 
			return 0.0;
		if(s_int > 2 && s_int <= 5)
			return 1.5 * t;
		if(s_int > 5 && s_int <= 15)
			return t;
		if(s_int > 15 && s_int <= 30)
			return 0.5 * t;
		
		return -1.0;
	}
	
	public double computeProbComp(Session s) {
		return s.getNIssue() + s.getNEasterEggs();
	}
	
	public String computeGrade(Integer score) {
		
		if(score < 50)
			return "D";
		if(score >= 50 && score < 70)
			return "C";
		if(score >= 70 && score < 80)
			return "B";
		if(score >= 80 && score < 100)
			return "A";
		if(score >= 100)
			return "S";
			
		return "Error";
	}
}
