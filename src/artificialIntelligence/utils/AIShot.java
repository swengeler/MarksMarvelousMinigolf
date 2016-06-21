package artificialIntelligence.utils;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.playable.Ball;

public class AIShot{
	
	private Vector3f shot;
	private ArrayList<Node> nodes;
	private Node closestNode;
	private ArrayList<Vector3f> collidingBallPositions;
	private Vector3f closestBallPosition;
	private Vector3f lastCollidingPosition;
	
	public AIShot(Vector3f shot){
		this.shot = shot;
		nodes = new ArrayList<Node>();
		collidingBallPositions = new ArrayList<Vector3f>();
	}
	
	public void addNode(Node node, Ball b){
		if(nodes.size() == 0 || !nodes.get(nodes.size() - 1).equals(node)){
			nodes.add(node);
			//System.out.println("Added node with distance " + node.getD());
			if(closestNode == null){
				closestNode = node;
				closestBallPosition = new Vector3f(b.getPosition().x, b.getPosition().y, b.getPosition().z);
				lastCollidingPosition = collidingBallPositions.get(collidingBallPositions.size() - 1);
			} else if(node.getD() < closestNode.getD()){
				closestNode = node;
				closestBallPosition = new Vector3f(b.getPosition().x, b.getPosition().y, b.getPosition().z);
				lastCollidingPosition = collidingBallPositions.get(collidingBallPositions.size() - 1);
			}
		}
	}
	
	
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public Node getClosestNode() {
		return closestNode;
	}

	public Vector3f getShot() {
		return shot;
	}
	
	public Vector3f getClosestBallPosition(){
		return this.closestBallPosition;
	}
	
	public void addCollidingBallPosition(Vector3f pos){
		Vector3f position = new Vector3f(pos.x, pos.y, pos.z);
		this.collidingBallPositions.add(position);
	}
	
	public ArrayList<Vector3f> reduceBallPositions(){
		ArrayList<Vector3f> newList = new ArrayList<Vector3f>();
		for(Vector3f vec : collidingBallPositions){
			newList.add(vec);
			if(vec.equals(lastCollidingPosition))
				break;
		}
		newList.add(closestBallPosition);
		return newList;
	}
	
}
