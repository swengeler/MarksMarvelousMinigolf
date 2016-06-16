package bot2_0;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.obstacles.Entity;
import entities.playable.Ball;
import entities.playable.VirtualBall;
import physics.utils.ShotData;
import terrains.Terrain;
import terrains.World;

public class HMPathing extends Algorithm {

	private static final float CELL_SIZE = 1f;
	
	private Node[][] grid;
	
	private boolean courseHasEnd;
	
	@Override
	public ArrayList<ShotData> getPath(Ball b, World w) {
		
		return null;
	}

	@Override
	public Node createGraph(Ball b, World w) {
		float xBall = b.getPosition().x;
		float zBall = b.getPosition().z;
		int arraySize = (int) Math.ceil(Terrain.getSize()/CELL_SIZE);
		grid = new Node[arraySize][arraySize];
		for(int x=0; x<arraySize; x++)
			for(int z=0; z<arraySize; z++){
				float xCell = (x + 1/2) * CELL_SIZE;
				float zCell = (z + 1/2) * CELL_SIZE;
				float yCell = w.getHeightOfTerrain(xCell, zCell);
				Node node = new Node(new Vector3f(xCell,yCell,zCell));
				grid[x][z] = node;
				if(x > 0){
					Node west = grid[x-1][z];
					float dist = node.getDistance(west);
					node.setWest(west, dist);
					west.setEast(node, dist);
				}
				if(z > 0){
					Node north = grid[x][z-1];
					float dist = node.getDistance(north);
					node.setNorth(north, dist);
					north.setSouth(node, dist);
				}
			}
		int xBallGrid = (int)(xBall / CELL_SIZE);
		int zBallGrid = (int)(zBall / CELL_SIZE);
		Node ballNode = grid[xBallGrid][zBallGrid];
		ballNode.setWalkable(true);
		ballNode.setVisited(true);
		
		testWalkable(ballNode,w);
		
		return ballNode;
	}
	
	private void testWalkable(Node node, World world) {
		if(node.getNorth() != null && !node.getNorth().isVisited()){
			Ball testBall = new VirtualBall(node.getNorth().getPosition());
			ArrayList<Entity> es = world.getCollidingEntities(testBall);
			if(es.size() > 0){
				
			} else {
				node.getNorth().setWalkable(true);
			}
			if(node.getNorth().isWalkable()){
				testWalkable(node.getNorth(), world);
			}
		}
		
	}

	public void labelDistance(Node node){
		
		if(node.getNorth() != null && node.getNorth().isWalkable()){
			float newD = node.getD() + node.getnDist();
			if(node.getNorth().getD() > newD){
				node.getNorth().setD(newD);
				labelDistance(node.getNorth());
			}
		}
		
		
		if(node.getSouth() != null && node.getSouth().isWalkable()){
			float newD = node.getD() + node.getsDist();
			if(node.getSouth().getD() > newD){
				node.getSouth().setD(newD);
				labelDistance(node.getSouth());
			}
		}
		
		if(node.getWest() != null && node.getWest().isWalkable()){
			float newD = node.getD() + node.getwDist();
			if(node.getWest().getD()> newD){
				node.getWest().setD(newD);
				labelDistance(node.getWest());
			}
		}

		if(node.getEast() != null && node.getEast().isWalkable()){
			float newD = node.getD() + node.geteDist();
			if(node.getEast().getD()> newD){
				node.getEast().setD(newD);
				labelDistance(node.getEast());
			}
		}
	}
	
}
