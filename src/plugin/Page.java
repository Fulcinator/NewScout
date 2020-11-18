package plugin;

import java.util.ArrayList;

public class Page {

	private ArrayList<String> interactions;
	private int totalWidget;
	private int activeWidget;
	String id;
	
	public String getId() {
		return id;
	}

	public Page(int totalWidget) {
		this.totalWidget = totalWidget;
		activeWidget = 0;
		interactions = new ArrayList<>();
	}
	
	public int getTotalWidget() {
		return totalWidget;
	}

	public ArrayList<String> getInteractions() {
		return interactions;
	}

	public int getActiveWidget() {
		return activeWidget;
	}
	
	public void printCurrentStats() {
		System.out.println("Current page coverage is " + ((double) activeWidget/totalWidget *100 ) + "%");
	}
	
	public void printStatsInteractions() {
		System.out.println("There have been " + interactions.size() + " over the " + activeWidget + " active Widget");
	}
}
