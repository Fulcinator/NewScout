package plugin;

public class Session {
	private String tester_id;
	//Visto che � facoltativo, io farei pure un costruttore che non lo riceve
	//e gliene generiamo uno noi univoco
	//TODO timing
	private Node root;
	private Node current;
	int totNodes;
	//TODO stats
	
	public Session(String home, String tester_id, String timing) {
		totNodes = 0;
		this.tester_id = tester_id;
		//TODO timing
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
	
	public void goHome() {
		current = root;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void newInteraction(String interaction) {
		current.getPage().recordInteraction(interaction);
	}
	
	public void computeStats() {
		//TODO
		root.printStats();
	}
	
	public void setActiveWidgetCurrentPage(int n) {
		current.getPage().setActiveWidget(n);
	}
	
	public void setTotalWidgetCurrentPage(int n) {
		current.getPage().setTotalWidgets(n);
	}
	
	public void printTree() {
		root.printTree(1);
	}
}