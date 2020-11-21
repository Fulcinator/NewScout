package plugin;

import java.util.ArrayList;

public class Node {
	String id;
	private Node father;
	private Page page;
	private ArrayList<Node> children;
	
	public ArrayList<Node> getChildren() {
		return children;
	}

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
	
	public void printStats() {
		page.printStatsInteractions();
		for(Node child : children) {
			child.printStats();
		}
	}
	
	public void printTree(int level) {
		System.out.println(page.getId());
		for(Node child : children) {
			for(int i = 0; i < level;i++)
				System.out.print("--->");
			child.printTree(level+1);
		}
	}
}
