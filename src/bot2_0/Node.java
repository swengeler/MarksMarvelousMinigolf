package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.utils.ShotData;
import terrains.Terrain;

public class Node{
	
	private Node parent;
	
	private Node north;
	private Edge nEdge;
	private Node south;
	private Edge sEdge;
	private Node east;
	private Edge eEdge;
	private Node west;
	private Edge wEdge;
	
	private ArrayList<Node> neighbours = new ArrayList<Node>();
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	
	private Vector3f position;
	
	private float d = Terrain.getSize()*10f;
	private float height;
	
	private boolean walkable = false;
	private boolean visited = false;
	private boolean tested = false;
	private boolean isTesting = false;
	
	public Node(Vector3f position){
		this.position = position;
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
	
	public void setPosition(Vector3f newP){
		this.position = newP;
	}
	
	//Using length squared to optimize calculations
	public float getSqDistance(Node target){
		Vector3f distVec = Vector3f.sub(this.getPosition(), target.getPosition(), null);
		return distVec.lengthSquared();
	}
	
	public float getDistance(Node target){
		return Vector3f.sub(this.getPosition(), target.getPosition(), null).length();
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
	
	public Node getNorth() {
		return north;
	}

	public void setNorth(Node node, Edge nEdge) {
		this.north = node;
		this.nEdge = nEdge;
	}

	public Node getSouth() {
		return south;
	}

	public void setSouth(Node node, Edge sEdge) {
		this.south = node;
		this.sEdge = sEdge;
	}

	public Node getEast() {
		return east;
	}

	public void setEast(Node node, Edge eEdge) {
		this.east = node;
		this.eEdge = eEdge;
	}

	public Node getWest() {
		return west;
	}

	public void setWest(Node node, Edge wEdge) {
		this.west = node;
		this.wEdge = wEdge;
			
	}

	public float getnDist() {
		return nEdge.getLength();
	}

	public float getsDist() {
		return sEdge.getLength();
	}

	public float geteDist() {
		return eEdge.getLength();
	}

	public float getwDist() {
		return eEdge.getLength();
	}
	
	

	public void setnDist(float nDist) {
		this.nEdge.setLength(nDist);
	}

	public void setsDist(float sDist) {
		this.sEdge.setLength(sDist);
	}

	public void seteDist(float eDist) {
		this.eEdge.setLength(eDist);
	}

	public void setwDist(float wDist) {
		this.wEdge.setLength(wDist);
	}
	
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited(boolean v){
		visited = v;
	}
	
	
	public Node getNeighbourNode(int i){
		if(i == 0)
			return north;
		else if(i == 1)
			return east;
		else if(i == 2)
			return south;
		else
			return west;
	}
	
	public void setNeighbourNode(int i, Node node, Edge edge){
		if(i == 0){
			north = node;
			nEdge = edge;
		} else if(i == 1){
			east = node;
			eEdge = edge;
		} else if(i == 2){
			south = node;
			sEdge = edge;
		} else {
			west = node;
			wEdge = edge;
		}
	}
	
	public Edge getEdge(int i){
		if(i == 0)
			return nEdge;
		else if(i == 1)
			return eEdge;
		else if(i == 2)
			return sEdge;
		else
			return wEdge;
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}

	public boolean isTesting() {
		return isTesting;
	}

	public void setTesting(boolean isTesting) {
		this.isTesting = isTesting;
	}
	
	
}
