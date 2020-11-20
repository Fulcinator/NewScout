package plugin;

import java.util.ArrayList;

public class Node {
	String id;
	private Node father;
	private Page page;
	private ArrayList<Node> children;
	
	public Node(Page p, Node f) {
		//TODO generare id
		page = p;
		father = f;
		children = new ArrayList<Node>();
	}
	
	public Page getPage() {
		return page;
	}
	
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	public void addChild(Node n) {
		children.add(n);
	}
}
