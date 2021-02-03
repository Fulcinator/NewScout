package plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StatsComputer {
	private static StatsComputer sc = null;
	private Map<String, Stats> stats;
	
	private StatsComputer() {
		stats = new HashMap<String, Stats>();
		GamificationUtils.parseStats(GamificationUtils.loadStats("db.txt"), stats);
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
		ArrayList<Integer> score = computeScore(s);
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
			st.setTotScore(st.getTotScore() + score.get(0) + score.get(1));
			
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
			st.setTotScore(st.getTotScore() + score.get(0) + score.get(1));
		}
		
		//Creazione stats sessione appena conclusa
		Stats toReturn = new Stats(s.getTesterId());
		double sum = 0.0;
		
		toReturn.setMinutes(s.getTiming().getMinutes());
		toReturn.setSeconds(s.getTiming().getSeconds());
		toReturn.setHLWidgets(s.getTotHLWidgets());
		for(Double d : s.getCoverage())
			sum += d;
		toReturn.setGlobalAvgCoverage(s.getCoverage().size() != 0 ? sum/(s.getCoverage().size()) : 0.0);
		toReturn.setIssues(s.getNIssue());
		toReturn.setGlobalEEP(s.getEasterEggPercentage());
		toReturn.setNewWidgets(s.getTotalNewWidgets());
		toReturn.setNewPages(s.getPageDiscovered().size());
		if(s.getTesterId().length() > 0) {
			toReturn.setScore(score.get(0));
			toReturn.setBonus(score.get(1));
			toReturn.setGrade(computeGrade(toReturn.getScore() + toReturn.getBonus()));
		}
		
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
		if(s instanceof MobileSession)
			GamificationUtils.savePages(discovered,"AndroidPages.txt");
		else 
			GamificationUtils.savePages(discovered,"pages.txt");
	}
	
	public ArrayList<Integer> computeScore(Session s) {
		ArrayList<Integer> totscore = new ArrayList<>();
		Map <String,Double> values = GamificationUtils.parseCoeff(GamificationUtils.loadStats("ParamsConf"));
		double a = values.get("a");
		double c = values.get("c");
		double d = values.get("d");
		double k = values.get("k");
		double h = values.get("h");
		double x = values.get("x");
		double y = values.get("y");
		double z = values.get("z");
		
		double basescore = (a*computeCovComp(s) + computeExComp(s,k,h) + c*computeEfComp(s));
		double bonusScorePerc = (d*computeTimeComp(s) + computeProbComp(s,x,y,z));
		int bonusscore = (int) (bonusScorePerc * basescore/100.0);
		totscore.add(0, (int) basescore);
		totscore.add(1, bonusscore);
		
		return totscore;
	}
	
	public double computeCovComp(Session s) {
		double num = 0.0;
		
		for(Double d : s.getCoverage())
			num += d;
		
		return num / (s.getCoverage().size());
	}
	
	public double computeExComp(Session s, double k, double h) {
		double B1 = 0.0;
		int f = s.getTotalPageVisited();
		if(f != 0)
			B1 = k * (s.getPageDiscovered().size() * 100 / f);
		
		int g = s.getTotHLWidgets();
		double B2 = 0.0;
		if(g != 0)
			B2 = h * (s.getTotalNewWidgets() * 100 / g);
		
		return B1 + B2;
		
	}
	
	public double computeEfComp(Session s) {
		if(s.getNSessionInteraction() == 0)
			return 0.0;
		return s.getTotHLWidgets() * 100.0 / s.getNSessionInteraction();
	}
	
	public double computeTimeComp(Session s) {
		double t = s.getTiming().getMinutes() + (s.getTiming().getSeconds()/60);
		double toReturn = 0.0;
		double coeff = 6.0;
		double s_int = s.getSecondsPerInteraction();
		if(s_int<1 || s_int>30) 
			return 0.0;
		if(s_int >= 1 && s_int <= 5)
			toReturn = 1.5 * t * coeff;
		if(s_int > 5 && s_int <= 15)
			toReturn = t * coeff;
		if(s_int > 15 && s_int <= 30)
			toReturn = 0.5 * t * coeff;
		
		if(toReturn >= 100)
			return 100.0;
		else
			return toReturn;

	}
	
	public double computeProbComp(Session s, double x1, double y1, double z1) {
		int x = s.getNIssue();
		int y = s.getNEasterEggs();
		int z = s.getBugCount();
		int alfa = 1;
		int beta = 1;
		int gamma = 3;
		double tot = 0.0;
		
		if(alfa*x >= x1*100)
			tot += x1;
		else
			tot += alfa*x;
		if(beta*y >= y1*100)
			tot += y1;
		else
			tot += beta*y;
		if(gamma*z >= z1*100)
			tot += z1;
		else
			tot += gamma*z;
		
		return tot;
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
