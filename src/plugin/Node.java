package plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Double cov = this.page.getCoverage();
		if(cov != null) {
			array.add(cov);
		}
		if(!this.isLeaf()) {			
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
	
	public Node getFather() {
		return father;
	}
	
	public int getTotalNInteractions() {
		if(this.isLeaf())
			return page.getNInteractions();
		else {
			int tot = page.getNInteractions();
			for(Node n :children)
				tot+= n.getTotalNInteractions();
			return tot;
		}
	}
	
	/**
	 * Aggiunta per avere l'insieme delle pagine visitate nella sessione
	 * @return
	 */
	public Set<Page> getPageVisited(){
		Set<Page> set = new HashSet<Page>();
		set.add(page);
		if(!isLeaf()) {
			for(Node child: children) {
				set.addAll(child.getPageVisited());
			}
		}
		return set;
	}
	
	public int getNEasterEgg() {
		int tot = page.getHasEasterEgg() ? 1 : 0;
		if(!isLeaf()) {
			for(Node child: children) {
				tot += child.getNEasterEgg();
			}
		}
		return tot;
	}
	
	public int getPossibleEasterEgg() {
		int tot = page.getSonWithEasterEgg() != null ? 1 : 0;
		if(!isLeaf()) {
			for(Node child: children) {
				tot += child.getPossibleEasterEgg();
			}
		}
		return tot;
	}
	
	public int getNIssue() {
		long tot = page.getInteractions().stream().filter( s -> s.startsWith("ISSUE")).count();
		if(!isLeaf()) {
			for(Node child : children) {
				tot += child.getNIssue();
			}
		}
		return (int) tot;
	}
}
