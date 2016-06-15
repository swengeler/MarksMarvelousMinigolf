package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import physics.utils.ShotData;

public class Node {
	
	private ArrayList<Node> neighbours = new ArrayList<Node>();
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	private Node parent;
	
	private Vector3f position;
	
	private float d = Integer.MAX_VALUE;
	private float g = Integer.MAX_VALUE;
	private float h = -1;
	
	private boolean walkable = false;
	
	public Node(Vector3f position){
		this.position = position;
	}
	
	public void connect(Node n, ShotData data){
		Vector3f dist = Vector3f.sub(position, n.getPosition(), null);
		Edge weight = new Edge(data, dist.lengthSquared());
		
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
	
	public float getGCost(){
		return g;
	}
	
	public void setG(float newG){
		g = newG;
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
	
	//Using length squared to optimize calculations
	public float getSqDistance(Node target){
		Vector3f distVec = Vector3f.sub(this.getPosition(), target.getPosition(), null);
		return distVec.lengthSquared();
	}
	
	public float getHValue(Node endNode){
		if(h == -1)
			h =  getSqDistance(endNode);
		return h;
	}
	
	public float getFValue(Node endNode){
		return g + getHValue(endNode);
	}
	
	public float getThetaValue(Node node){
		return getGCost() + getSqDistance(node);
	}
	
	public float getD(){
		return d;
	}
	
	public void setD(float newD){
		d = newD;
	}
	
	public boolean isWalkable(){
		return walkable;
	}
	
	public void setWalkable(boolean w){
		walkable = w;
	}
	
}
