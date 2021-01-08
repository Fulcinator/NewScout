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
	/* nuove pagine*/
	private ArrayList<String> pageKnown;
	private ArrayList<String> pageDiscovered;
	/* Bug trovati col parse della pagina */
	private int bugCount;
	private boolean simpleVersion;
	
	
	public ArrayList<String> getPageKnown() {
		if(simpleVersion)
			return new ArrayList<>();
		return pageKnown;
	}

	public ArrayList<String> getPageDiscovered() {
		if(simpleVersion)
			return new ArrayList<>();
		return pageDiscovered;
	}

	public int getTotalNewWidgets() {
		return totNewWidget;
	}
	
	public void updateNewTotalWidget() {
		totNewWidget += widgetNewlyDiscovered.size();
	}
	
	public HashMap<String, String> getWidgetNewlyDiscovered(){
		if(simpleVersion)
			return new HashMap<>();
		return widgetNewlyDiscovered;
	}
	
	public void reloadMap() {
		widgetAlreadyKnown = GamificationUtils.getNewInteractionInPage(current.getPage().getId(), tester_id);
		updateNewTotalWidget();
		widgetNewlyDiscovered.clear();
	}
	
	public Session(String home, String tester_id, boolean simpleVersion) {
		this.simpleVersion = simpleVersion;
		timing = new Timing();
		totNodes = 0;
		this.tester_id = tester_id;
		root = firstNode(home);
		current = root;
		if(!simpleVersion) {
			widgetNewlyDiscovered = new HashMap<>();
			totNewWidget = 0;
			setBugCount(0);
			reloadMap();
			pageKnown = GamificationUtils.loadStats("pages.txt");
			pageDiscovered = new ArrayList<>();
			if(!pageKnown.contains(home)) {
				pageDiscovered.add(home);
				current.getPage().setIsNewPage(true);
			}
			if(tester_id.length() > 0)
				current.getPage().setHighscore(GamificationUtils.getHighScorePage(home));
		}
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
		if(!simpleVersion) {
			reloadMap();
			if(!pageKnown.contains(pagename)) {
				if(!pageDiscovered.contains(pagename)) {
					pageDiscovered.add(pagename);
					current.getPage().setIsNewPage(true);
				}
			}
			if(tester_id.length() > 0)
				current.getPage().setHighscore(GamificationUtils.getHighScorePage(pagename));
		}
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
		reloadMap();
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
		if(!simpleVersion)
			if(this.tester_id.length() > 0) {
				String key = interaction.split("TIME")[0].split("IDENTIFIER")[1].trim();
				//String toInsert = "IDENTIFIER " + key + " FOUND_BY " + this.tester_id;
				if(!widgetAlreadyKnown.containsKey(key)) {
					widgetNewlyDiscovered.put(key, tester_id);
				}
			}
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
			return (timing.getSeconds() + timing.getMinutes()*60)/(double) n;
		}
	}
	
	public int getNSessionInteraction() {
		return root.getNInteraction();
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
	
	public int getNEasterEggs() {
		return root.getNEasterEgg();
	}
	
	public int getNIssue() {
		return root.getNIssue();
	}
	
	public Set<Page> getVisitedPages() {
		return root.getPageVisited();
	}

	public int getBugCount() {
		return bugCount;
	}

	public void setBugCount(int bugCount) {
		this.bugCount = bugCount;
	}
}
