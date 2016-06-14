package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import physics.utils.ShotData;

public class Node {
	
	private ArrayList<Node> neighbours = new ArrayList<Node>();
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	private Node parent;
	
	private Vector3f position;
	
	private int d = Integer.MAX_VALUE;
	
	public Node(Vector3f position){
		this.position = position;
	}
	
	public void connect(Node n, ShotData data){
		Vector3f dist = Vector3f.sub(position, n.getPosition(), null);
		Edge weight = new Edge(data)
		
		
		this.neighbours.add(n);
		this.edges.add(weight);
		
		n.neighbours.add(this);
		n.edges.add(weight);
	}
	
	public ArrayList<Node> getNeighbours(){
		return neighbours;
	}
	
	public ArrayList<Edge> getEdges(){
		return edges;
	}
	
	public Edge getEdge(int index){
		return edges.get(index);
	}
	
	public int getFCost(Node end){
		return d;
	}
	
	public void setDistance(int newD){
		d = newD;
	}
	
	public int getDistance(){
		return d;
	}
	
	public void setParent(Node n){
		parent = n;
	}
	
	public Node getParent(){
		return parent;
	}
	
	public String toString(){
		return "Position: " + position;
	}
	
	public boolean hasParent(){
		return parent != null;
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
}
