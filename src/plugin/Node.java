package plugin;

import java.util.ArrayList;
import java.util.List;

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
		page.printCurrentStats();
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
	
	public void printTiming() {
		System.out.println("Hai passato " + page.computeTotalTiming()/1000 + " secondi sulla pagina " + page.getId());
		for(Node child : children) {
			child.printTiming();
		}
	}
	
	public List<String> obtainPages(){
		ArrayList<String> l = new ArrayList<>();
		l.add(page.preparePage());
		for(Node child : children) {
			l.addAll(child.obtainPages());
		}
		return l;
	}
	
	public ArrayList<Double> getCoverage() {
		ArrayList<Double> array = new ArrayList<Double>();
		if(this.isLeaf()) {
			array.add(this.page.getCoverage());
		}
		else {
			array.add(this.page.getCoverage());
			for(Node n : children) {
				array.addAll(n.getCoverage());
			}
		}
		return array;
	}
	
	public int getHLWidgets() {
		if(this.isLeaf())
			return page.getHighlightedWidgets();
		else {
			int tot = page.getHighlightedWidgets();
			for(Node n : children) 
				tot += n.getHLWidgets();
			return tot;
		}
	}
}
