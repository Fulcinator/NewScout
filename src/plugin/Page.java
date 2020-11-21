package plugin;

import java.util.ArrayList;

public class Page {

	private ArrayList<String> interactions;
	private int totalWidgets;
	private int activeWidgets;
	String id;
	

	public Page(String id) {
		this.id = id;
		this.totalWidgets = 0;
		activeWidgets = 0;
		interactions = new ArrayList<>();
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

	public int getActiveWidgets() {
		return activeWidgets;
	}
	
	public void printCurrentStats() {
		System.out.println("Current page coverage is " + activeWidgets + " active widget over a total of " + totalWidgets + "% of page " + id);
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + activeWidgets + " active Widget of page " + id);
	}
	
	public void recordInteraction(String interaction) {
		interactions.add(interaction);
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
		 activeWidgets =  nActive;
	}
	
	public void setTotalWidgets( int nuovo) {
		totalWidgets = nuovo;
	}
}
