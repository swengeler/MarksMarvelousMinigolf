package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.utils.ShotData;

public class Node {
	
	private Node parent;
	
	private Node north;
	private float nDist;
	private Node south;
	private float sDist;
	private Node east;
	private float eDist;
	private Node west;
	private float wDist;
	
	private Vector3f position;
	
	private float d = Integer.MAX_VALUE;
	private float height;
	
	private boolean walkable = false;
	private boolean visited = false;
	
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
	
	//Using length squared to optimize calculations
	public float getSqDistance(Node target){
		Vector3f distVec = Vector3f.sub(this.getPosition(), target.getPosition(), null);
		return distVec.lengthSquared();
	}
	
	public float getDistance(Node target){
		Vector3f distVec = Vector3f.sub(this.getPosition(), target.getPosition(), null);
		return distVec.lengthSquared();
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

	public void setNorth(Node north, float nDist) {
		this.north = north;
		this.nDist = nDist;
	}

	public Node getSouth() {
		return south;
	}

	public void setSouth(Node south, float sDist) {
		this.south = south;
		this.sDist = sDist;
	}

	public Node getEast() {
		return east;
	}

	public void setEast(Node east, float eDist) {
		this.east = east;
		this.eDist = eDist;
	}

	public Node getWest() {
		return west;
	}

	public void setWest(Node west, float wDist) {
		this.west = west;
		this.wDist = wDist;
	}

	public float getnDist() {
		return nDist;
	}

	public float getsDist() {
		return sDist;
	}

	public float geteDist() {
		return eDist;
	}

	public float getwDist() {
		return wDist;
	}
	
	

	public void setnDist(float nDist) {
		this.nDist = nDist;
	}

	public void setsDist(float sDist) {
		this.sDist = sDist;
	}

	public void seteDist(float eDist) {
		this.eDist = eDist;
	}

	public void setwDist(float wDist) {
		this.wDist = wDist;
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
	
	
	
}
