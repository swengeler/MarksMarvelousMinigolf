package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

public class AIShot {
	
	private Vector3f shot;
	private ArrayList<Node> nodes;
	private Node closestNode;
	
	public AIShot(Vector3f shot){
		this.shot = shot;
		nodes = new ArrayList<Node>();
	}
	
	public void addNode(Node node){
		if(nodes.size() == 0 || !nodes.get(nodes.size() - 1).equals(node)){
			nodes.add(node);
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
	
}
