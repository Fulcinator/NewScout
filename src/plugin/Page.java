package plugin;

import java.util.ArrayList;

public class Page {

	private ArrayList<String> interactions;
	private int totalWidgets;
	private int activeWidgets;
	String id;
	
	public String getId() {
		return id;
	}

	public Page(int totalWidgets) {
		id = "";
		this.totalWidgets = totalWidgets;
		activeWidgets = 0;
		interactions = new ArrayList<>();
	}
	
	public Page(int totalWidget, String id) {
		this.id = id;
		this.totalWidgets = totalWidget;
		activeWidgets = 0;
		interactions = new ArrayList<>();
	}
	
	public int getTotalWidget() {
		return totalWidgets;
	}

	public ArrayList<String> getInteractions() {
		return interactions;
	}

	public int getActiveWidget() {
		return activeWidgets;
	}
	
	public void printCurrentStats() {
		System.out.println("Current page coverage is " + ((double) activeWidgets/totalWidgets *100 ) + "%");
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + activeWidgets + " active Widget");
	}
	
	public void recordInteraction(String interaction, int nActive) {
		activeWidgets = nActive;
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
	
	public void addActiveWidget() {
		activeWidgets++;
	}
}
