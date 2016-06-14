package bot2_0;

import java.util.ArrayList;

import entities.playable.Ball;
import physics.utils.ShotData;
import terrains.World;

public class SimpleAlgorithm extends Algorithm {

	@Override
	public ArrayList<ShotData> getPath(Ball b, World w) {
		Node startNode = createGraph(b,w);
		
		//The actual implementation of the algorithm goes here
		Node endNode = new Node(3);
		
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

	@Override
	public Node createGraph(Ball b, World w) {
		// TODO Auto-generated method stub
		return null;
	}

}
