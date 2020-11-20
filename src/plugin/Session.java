package plugin;

public class Session {
	private String tester_id;
	//Visto che è facoltativo, io farei pure un costruttore che non lo riceve
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
		root = newNode(home);
		current = root;
	}
	
	public Node newNode(String pagename) {
		Page p = new Page(pagename);
		Node n = new Node(p, current);
		current.addChild(n);
		current = n;
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
	}
}
