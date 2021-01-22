package plugin;

import java.util.ArrayList;

import scout.AppState;

public class MobilePage extends Page {

	private ArrayList<String> state;
	

	public MobilePage(String id) {
		super(id);
		state = new ArrayList<>();
	}
	
	public MobilePage(ArrayList<String> state, String id) {
		super(id);
		this.state = state;
	}
	
	public ArrayList<String> getState(){
		return state;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this == o) return true;
		if(!(o instanceof MobilePage)) return false;
		MobilePage p = (MobilePage) o;
		return this.state.equals(p.getState());
	}
	
	@Override
	public int hashCode() {
		return this.state.hashCode();
	}
	
	public boolean compareState(ArrayList<String> other) {
		return state.equals(other);
	}
	
	@Override
	public String preparePage() {
		String toReturn = "PAGE " + state.toString() + " TIME " + computeTotalTiming() +System.lineSeparator();
		for(String s: super.getInteractions()) {
			toReturn += s + System.lineSeparator();
		}
		return toReturn;
	}

	/*

	public Page(String id) {
		this.id = id;
		this.totalWidgets = 0;
		highlightedWidgets = 0;
		interactions = new ArrayList<>();
		time = new ArrayList<>();
		hasEasterEgg = false;
		easterEggStartPoint = null;
		sonWithEasterEgg = null;
		isNew = false;
		setHighscore(new HashMap<String, Double>());
	}*/
	
}
