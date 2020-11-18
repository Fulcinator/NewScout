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
		//TODO: settare l'id
		this.totalWidgets = totalWidgets;
		activeWidgets = 0;
		interactions = new ArrayList<>();
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
	
	public void addActiveWidget() {
		activeWidgets++;
	}
	
	public void printCurrentStats() {
		System.out.println("Current page coverage is " + ((double) activeWidgets/totalWidgets *100 ) + "%");
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + activeWidgets + " active Widget");
	}
}
