package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

public class AIShot{
	
	private Vector3f shot;
	private ArrayList<Node> nodes;
	private Node closestNode;
	private float angDeg;
	
	public AIShot(Vector3f shot){
		this.shot = shot;
		nodes = new ArrayList<Node>();
	}
	
	public void addNode(Node node){
		if(nodes.size() == 0 || !nodes.get(nodes.size() - 1).equals(node)){
			nodes.add(node);
			//System.out.println("Added node with distance " + node.getD());
			if(closestNode == null)
				closestNode = node;
			else if(node.getD() < closestNode.getD())
				closestNode = node;
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

	public float getAngle() {
		return angDeg;
	}

	public void setAngle(float angDeg) {
		this.angDeg = angDeg;
	}
	
	
	
}
