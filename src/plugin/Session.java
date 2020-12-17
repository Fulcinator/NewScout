package plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Session {
	private String tester_id;
	//Visto che è facoltativo, io farei pure un costruttore che non lo riceve
	//e gliene generiamo uno noi univoco
	private Timing timing;
	private Node root;
	private Node current;
	int totNodes;
	/* nuovi widget*/
	private HashMap<String, String> widgetAlreadyKnown;
	private HashMap<String, String> widgetNewlyDiscovered;
	private int totNewWidget; 
	
	public int getTotalNewWidgets() {
		return totNewWidget;
	}
	
	public void updateNewTotalWidget() {
		totNewWidget += widgetNewlyDiscovered.size();
	}
	
	public HashMap<String, String> getWidgetNewlyDiscovered(){
		return widgetNewlyDiscovered;
	}
	
	public void reloadMap() {
		widgetAlreadyKnown = GamificationUtils.getNewInteractionInPage(current.getPage().getId(), tester_id);
		updateNewTotalWidget();
		widgetNewlyDiscovered.clear();
	}
	
	public void writeDifference() {
		
	}
	
	public Session(String home, String tester_id) {
		timing = new Timing();
		totNodes = 0;
		this.tester_id = tester_id;
		root = firstNode(home);
		current = root;
		widgetNewlyDiscovered = new HashMap<>();
		totNewWidget = 0;
		reloadMap();
	}
	
	public Node newNode(String pagename) {
		for(Node n: current.getChildren()) {
			if(n.getPage().getId().equals(pagename)) {
				current = n;
				return n;
			}
		}		
		Page p = new Page(pagename);
		Node n = new Node(p, current);
		current.addChild(n);
		current = n;
		totNodes++;
		reloadMap();
		return n;
	}
	
	private Node firstNode(String pagename) {
		Page p = new Page(pagename);
		Node n = new Node(p, null);
		totNodes++;
		return n;
	}
	
	/*
	 * Go Home setta il timing delle due pagine: ferma quella corrente e fa ripartire quella della home 
	 */
	public void goHome() {
		stopPageTiming();
		current = root;
		startPageTiming();
	}
	
	public Node getRoot() {
		return root;
	}
	
	public Node getCurrent() {
		return current;
	}
	
	public Timing getTiming() {
		return timing;
	}
	
	public int getTotNodes() {
		return totNodes;
	}
	
	public String getTesterId() {
		return tester_id;
	}
	
	public void newInteraction(String interaction) {
		current.getPage().recordInteraction(interaction);
		/* La chiave è l'identificatore che è esattamente in mezzo alla sottostringa che definisce il tipo del widget
		 * e il timestamp a cui è avvenuto
		 */
		if(this.tester_id.length() > 0) {
			String key = interaction.split("TIME")[0].split("IDENTIFIER")[1].trim();
			//String toInsert = "IDENTIFIER " + key + " FOUND_BY " + this.tester_id;
			if(!widgetAlreadyKnown.containsKey(key)) {
				widgetNewlyDiscovered.put(key, tester_id);
			}
		}
	}
	
	public void computeStats() {
		//TODO
		root.printStats();
	}
	
	public void setActiveWidgetCurrentPage(int n) {
		current.getPage().setHighlightedWidget(n);
	}
	
	public void setTotalWidgetCurrentPage(int n) {
		current.getPage().setTotalWidgets(n);
	}
	
	public void printTree() {
		root.printTree(1);
	}
	
	/*
	 * Avvia il timing della sessione e della home page
	 */
	public void startSessionTiming() {
		timing.setBeginTime();
		//root.getPage().loadPage();
		startPageTiming();
	}
	
	/*
	 * Avvia il timing della pagina corrente; deve esseere chiamata dopo newNode
	 */
	public void startPageTiming() {
		current.getPage().loadPage();
	}
	
	/*
	 * Ferma il timing della pagina corrente; deve essere chiamata prima della newNode 
	 */
	public void stopPageTiming() {
		current.getPage().closePage();
	}
	
	public void stopSessionTiming() {
		timing.setEndTime();
		//current.getPage().closePage();
		stopPageTiming();
	}
	
	public void computeTimeSession() {
		timing.computeTime();
	}
	
	public String getStringTiming() {
		if(timing != null)
			return timing.toString();
		else
			return "The session has not terminated yet";
	}
	
	public ArrayList<Double> getCoverage() {
		return root.getCoverage();
	}
	
	public int getTotHLWidgets() {
		return root.getHLWidgets();
	}
	
	/**
	 * 
	 * @return il numero di secondi che ci sono voluti per ogni interazione
	 */
	public double getSecondsPerInteraction() {
		if(!timing.hasStopped())
			return 0.0;
		
		if(!timing.isReady())
			timing.computeTime();
		
		int n = root.getTotalNInteractions();
		if(n == 0) {
			//se non ho highlighted widget il tempo sarebbe infinito, il che non ha senso
			return 0.0;
		} else {
			return timing.getSeconds()/(double) n;
		}
	}
	
	public double getInteractionsPerPage() {
		
		int n = root.getTotalNInteractions();
		int k = getTotalPageVisited();
		if(n == 0) {
			//se non ho highlighted widget il tempo sarebbe infinito, il che non ha senso
			return 0.0;
		} else {
			return (double) n / k;
		}
	}
	
	/*public void printPageSet() {
		Set<Page> set =root.getPageVisited();
		for(Page p: set) {
			System.out.println("Pagina: " + p.getId());
		}
	}*/
	
	public int getTotalPageVisited() {
		return root.getPageVisited().size();
	}
	
	public double getEasterEggPercentage() {
		int num = (root.getNEasterEgg());
		int den = (root.getPossibleEasterEgg() -1);
		if(den == 0)
			return 0.0;
		return ( num * 100)/((double) den);	
	}
	
	public int getNIssue() {
		return root.getNIssue();
	}
}
