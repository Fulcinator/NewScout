package plugin;

import java.util.ArrayList;
import java.util.HashMap;

public class MobileSession extends Session{
	
	
	public MobileSession(String home, ArrayList<String> initialState, String tester_id, boolean simpleVersion) {
		super(home, tester_id);
		super.setSimpleVersion(simpleVersion);
		super.setRoot(firstNode(initialState));
		super.setCurrent(super.getRoot());
		if(!simpleVersion) {
			//TODO: adattare le pagine visitate per la versione mobile
			widgetNewlyDiscovered = new HashMap<>();
			totNewWidget = 0;
			setBugCount(0);
			reloadMap();
			pageKnown = GamificationUtils.loadStats("pages.txt");
			pageDiscovered = new ArrayList<>();
			if(!pageKnown.contains(home)) {
				pageDiscovered.add(home);
				super.getCurrent().getPage().setIsNewPage(true);
			}
			if(tester_id.length() > 0)
				super.getCurrent().getPage().setHighscore(GamificationUtils.getHighScorePage(home));
		}
	}
	
	public Node newNode(ArrayList<String> state) {
		for(Node n: super.getCurrent().getChildren()) {
			Page p = n.getPage();
			if(p instanceof MobilePage) {
				if(((MobilePage)p).compareState(state)) {
					super.setCurrent(n);
					return n;
				}
			}
		}
		MobilePage p = new MobilePage(state, "" + (state.hashCode()));
		Node n = new Node(p, super.getCurrent());
		super.getCurrent().addChild(n);
		super.setCurrent(n);
		totNodes++;
		String pagename = p.getId();
		if(!super.isSimpleVersion()) {
			reloadMap();
			if(!pageKnown.contains(pagename)) {
				if(!pageDiscovered.contains(pagename)) {
					pageDiscovered.add(pagename);
					super.getCurrent().getPage().setIsNewPage(true);
				}
			}
			if(super.getTester_id().length() > 0)
				super.getCurrent().getPage().setHighscore(GamificationUtils.getHighScorePage(pagename));
		}
		return n;
	}
	
	
	private Node firstNode(ArrayList<String> state) {
		MobilePage p = new MobilePage(state, "" + (state.hashCode()));
		Node n = new Node(p, null);
		totNodes++;
		return n;
	}
	
	/*
	 * Go Home setta il timing delle due pagine: ferma quella corrente e fa ripartire quella della home
	 * per ora non è il caso di esternderlo, poi vedremo perché servirà chiamare l'unwind o qualcosa del genere 
	 */
	/*public void goHome() {
		stopPageTiming();
		current = root;
		if(!simpleVersion)
			reloadMap();
		startPageTiming();
	}*/
	
	
	@Override
	public void newInteraction(String interaction) {
		if(super.isSimpleVersion())
			super.newInteraction(interaction);
		else {
			super.getCurrent().getPage().recordInteraction(interaction);
			/* La chiave è l'identificatore che è esattamente in mezzo alla sottostringa che definisce il tipo del widget
			 * e il timestamp a cui è avvenuto, serve per i widget scoperti in questa sessione
			 * TODO: GAMIFICATION - ADATTARE MOLTO PROBABILMENTE
			 * LA PARTE SOTTO SERVE PER I WIDGET NUOVI
			 */
			if(super.getTester_id().length() > 0) {
				String key = interaction.split("TIME")[0].split("IDENTIFIER")[1].trim();
				//String toInsert = "IDENTIFIER " + key + " FOUND_BY " + this.tester_id;
				if(!widgetAlreadyKnown.containsKey(key)) {
					widgetNewlyDiscovered.put(key, super.getTester_id());
				}
			}
		}
		
		
			
	}
		
	public void shrink(int n) {
		for(int i = 0; i < n; i++) {
			if(super.getCurrent().getFather() != null) {
				super.setCurrent(super.getCurrent().getFather());
			}
		}
	}
}
