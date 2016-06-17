/*
package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.playable.Ball;
import physics.engine.PhysicsEngine;
import physics.utils.ShotData;
import terrains.World;

public class ThetaStar extends Algorithm {

	private static final int INTER = 5;
	
	private MinHeap open;
	private ArrayList<Node> closed = new ArrayList<Node>();
	private Node endNode;
	
	@Override
	public ArrayList<ShotData> getPath(Ball b, World w) {
		Node startNode = createGraph(b,w);
		endNode = lookForEnd();
		open = new MinHeap(1000,endNode);
		startNode.setG(0);
		startNode.setParent(startNode);
		open.insert(startNode);
		while(!open.isEmpty()){
			Node node = open.delete();
			setVertex(node);
			if(node.equals(endNode))
				break;
			closed.add(node);
			for(Node neighbour : node.getNeighbours()){
				if(!closed.contains(neighbour)){
					if(!open.contains(neighbour)){
						neighbour.setG(Integer.MAX_VALUE);
						neighbour.setParent(null);
					}
					updateVertex(node,neighbour);
				}
			}
		}
		
		
		ArrayList<ShotData> shots = new ArrayList<ShotData>();
		Node currentNode = endNode;
		while(currentNode.hasParent()){
			Node parent = currentNode.getParent();
			int pos = parent.getNeighbours().indexOf(currentNode);
			Edge edge = parent.getEdge(pos);
			shots.add(0, edge.getData());
		}
		
		return shots;
	}

	private void updateVertex(Node node, Node neighbour) {
		float oldG = neighbour.getGCost();
		computeCost(node,neighbour);
		if(neighbour.getGCost() < oldG){
			if(open.contains(neighbour)){
				open.delete(neighbour);
			}
			open.insert(neighbour);
		}
	}

	private void computeCost(Node node, Node neighbour) {
		float newG = (node.getParent().getGCost() + node.getParent().getSqDistance(neighbour));
		if(newG < neighbour.getGCost()){
			neighbour.setParent(node.getParent());
			neighbour.setG(newG);
		}
	}

	private void setVertex(Node n) {
		if(!isReachable(n.getParent(),n)){
			Node minNode = n.getNeighbours().get(0);
			for(Node node : n.getNeighbours()){
				if(closed.contains(node) && (node.getThetaValue(n) < minNode.getThetaValue(n)))
					minNode = node;
			}
			n.setParent(minNode);
			n.setG(minNode.getGCost() + n.getSqDistance(minNode));
		}
	}

	private boolean isReachable(Node parent, Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	private Node lookForEnd() {
		return null;
	}

	@Override
	public Node createGraph(Ball b, World w) {
		PhysicsEngine engine = PhysicsEngine.getInstance();
		Node startNode = new Node(b.getPosition());
		for(int i=0; i*INTER<360; i++){
			//Vector2f unit2 = createUnitVector(i*INTER);
			//Vector3f unit = new Vector3f(unit2.x,0,unit2.y);
			
		}

		return null;
	}
	
	private Vector2f createUnitVector(float degrees){
		float rad = (float) Math.toRadians(degrees);
		float x = (float) Math.cos(rad);
		float y = (float) Math.sin(rad);
		return new Vector2f(x,y);
	}
	
	


}
*/
