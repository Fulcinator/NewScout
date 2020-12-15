package plugin;

import java.util.ArrayList;
import java.util.Set;

public class Session {
	private String tester_id;
	//Visto che è facoltativo, io farei pure un costruttore che non lo riceve
	//e gliene generiamo uno noi univoco
	private Timing timing;
	private Node root;
	private Node current;
	int totNodes;
	//TODO stats
	
	public Session(String home, String tester_id) {
		timing = new Timing();
		totNodes = 0;
		this.tester_id = tester_id;
		root = firstNode(home);
		current = root;
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
		root.getPage().loadPage();
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
		current.getPage().closePage();
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
	
	public void printPageSet() {
		Set<Page> set =root.getPageVisited();
		for(Page p: set) {
			System.out.println("Pagina: " + p.getId());
		}
	}
}
