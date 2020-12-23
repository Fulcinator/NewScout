package plugin;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Page {

	private ArrayList<String> interactions;
	private ArrayList<Timing> time;
	private int totalWidgets;
	private int highlightedWidgets;
	private String id;
	private boolean hasEasterEgg;
	private Point easterEggStartPoint;
	private String sonWithEasterEgg;
	private boolean isNew;
	private Map<String, Double> highscore;
	
	public boolean isPageNew() {
		return isNew;
	}
	
	public void setIsNewPage(boolean b) {
		isNew = b;
	}

	public String getSonWithEasterEgg() {
		return sonWithEasterEgg;
	}

	public void setSonWithEasterEgg(String sonWithEasterEgg) {
		this.sonWithEasterEgg = sonWithEasterEgg;
	}

	public void setEasterEggStartPoint(int x, int y) {
		easterEggStartPoint = new Point(x, y);
	}

	public Point getEasterEggStartPoint() {
		return easterEggStartPoint;
	}

	public Page(String id) {
		this.id = id;
		this.totalWidgets = 0;
		highlightedWidgets = 0;
		interactions = new ArrayList<>();
		time = new ArrayList<>();
		hasEasterEgg = false;
		easterEggStartPoint = null;
		sonWithEasterEgg = null;
		isNew = true;
		setHighscore(new HashMap<String, Double>());
	}
	
	public void loadPage() {
		Timing t = new Timing();
		time.add(t);
		t.setBeginTime();
	}
	
	public void closePage() {
		time.get(time.size() -1).setEndTime();
	}
	
	public String getId() {
		return id;
	}
	
	public int getTotalWidgets() {
		return totalWidgets;
	}

	public ArrayList<String> getInteractions() {
		return interactions;
	}

	public int getHighlightedWidgets() {
		return highlightedWidgets;
	}
	
	public void printCurrentStats() {
		System.out.println("Current page coverage is " + highlightedWidgets + " highlighted widget over a total of " + totalWidgets + "of page " + id);
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + highlightedWidgets + " highlighted Widget of page " + id);
	}
	
	public void recordInteraction(String interaction) {
		interactions.add(interaction);
	}
	
	public Double getCoverage() {
		if(totalWidgets == 0 || highlightedWidgets == 0)
			return null;
		else {
			int k = interactions.stream().filter( s -> s.contains("GO_HOME")).mapToInt( s -> 1).sum();
			if(interactions.size() == k)
				return null;
			return highlightedWidgets * 100.0 / totalWidgets;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this == o) return true;
		if(!(o instanceof Page)) return false;
		Page p = (Page) o;
		return this.id.equals(p.getId());
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	public void setHighlightedWidget( int nActive) {
		 highlightedWidgets =  nActive;
	}
	
	public void setTotalWidgets( int nuovo) {
		totalWidgets = nuovo;
	}
	
	public long computeTotalTiming() {
		long tot = 0;
		for(Timing t : time) {
			tot += t.computeTime();
		}
		return tot;
	}
	
	public String preparePage() {
		String toReturn = "PAGE " + id + " TIME " + computeTotalTiming() +System.lineSeparator();
		for(String s: interactions) {
			toReturn += s + System.lineSeparator();
		}
		return toReturn;
	}
	
	public void setHasEasterEgg(boolean b) {
		this.hasEasterEgg = b;
	}
	
	public boolean getHasEasterEgg() {
		return hasEasterEgg;
	}
	
	public int getNInteractions() {
		return interactions.size();
	}

	public Map<String, Double> getHighscore() {
		return highscore;
	}

	public void setHighscore(Map<String, Double> highscore) {
		this.highscore = highscore;
	}
	
	public void updateHighscore(String tester) {
		Double thisCov = this.getCoverage();
		if(highscore.size()> 0) {
			for(String key : highscore.keySet()) {
				if(highscore.get(key) < thisCov) {
					highscore.remove(key);
					highscore.put(tester, thisCov);
				}
			}
		} else {
			highscore.put(tester, thisCov);
		}
	}
}
