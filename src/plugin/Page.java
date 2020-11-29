package plugin;

import java.util.ArrayList;

public class Page {

	private ArrayList<String> interactions;
	private ArrayList<Timing> time;
	private int totalWidgets;
	private int highlightedWidgets;
	String id;
	

	public Page(String id) {
		this.id = id;
		this.totalWidgets = 0;
		highlightedWidgets = 0;
		interactions = new ArrayList<>();
		time = new ArrayList<>();
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
		System.out.println("Current page coverage is " + highlightedWidgets + " active widget over a total of " + totalWidgets + "of page " + id);
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + highlightedWidgets + " active Widget of page " + id);
	}
	
	public void recordInteraction(String interaction) {
		interactions.add(interaction);
	}
	
	public double getCoverage() {
		return highlightedWidgets / totalWidgets;
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this == o) return true;
		if(!(o instanceof Page)) return false;
		Page p = (Page) o;
		return this.id.equals(p.getId());
	}
	
	public int hashCode() {
		return this.id.hashCode();
	}
	
	public void setActiveWidget( int nActive) {
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
}
