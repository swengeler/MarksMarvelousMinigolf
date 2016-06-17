package bot2_0;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.obstacles.Entity;
import entities.playable.Ball;
import entities.playable.RealBall;
import entities.playable.VirtualBall;
import physics.engine.PhysicsEngine;
import physics.utils.ShotData;
import terrains.Terrain;
import terrains.World;

public class HMPathing extends Algorithm {

	private static final float MAX_SLOPE = 1.5f; // That is the maximum height difference between two adjacent cell for them to be connected
	private static final float MAX_SHOT_POWER = 5;
	private static final float DELTA_ANGLE = 5; // In degrees
	private static final int MIDPOINT_ITERATIONS = 10;
	
	private Node[][] grid;
	private Ball b;
	private World w;
	
	public HMPathing(Ball b, World w) {
		this.b = b;
		this.w = w;
		createGraph();
	}
	
	public void shootBall() {
		
		int xBallGrid = (int)(b.getPosition().x/CELL_SIZE);
		int zBallGrid = (int)(b.getPosition().z/CELL_SIZE);
		Node ballNode = grid[xBallGrid][zBallGrid];
		float ballD = ballNode.getD();
		
		AIShot bestShot = null;
		ArrayList<AIShot> shots = new ArrayList<AIShot>();
		int shotsTaken = 0;
		for(int i = 0; i < 360; i += DELTA_ANGLE){
			shotsTaken++;
			System.out.println("Shots taken: " + shotsTaken);
			Vector3f vec = generateShot(i, MAX_SHOT_POWER);
			AIShot shot = PhysicsEngine.getInstance().aiTestShot((RealBall)b, vec, grid);
			if(bestShot == null || (shot.getClosestNode().getD() < bestShot.getClosestNode().getD())){
				bestShot = shot;
			}
		}
		
		float p = MAX_SHOT_POWER;
		float q = 0;
		Vector3f shotDir = new Vector3f(bestShot.getShot().x, 0, bestShot.getShot().z);
		shotDir.normalise();
		float xBestShot = shotDir.x;
		float zBestShot = shotDir.z;
		AIShot pShot = bestShot;
		AIShot qShot = PhysicsEngine.getInstance().aiTestShot((RealBall)b, new Vector3f(), grid);
		int counter = 0;
		while(!bestShot.getClosestNode().equals(pShot.getNodes().get(pShot.getNodes().size() - 1)) || counter > MIDPOINT_ITERATIONS){
			
			float newPow = (p + q)/2;
			Vector3f vec = new Vector3f(xBestShot*  newPow, 0, zBestShot * newPow);
			AIShot newShot = PhysicsEngine.getInstance().aiTestShot((RealBall) b, vec, grid);
			
			if(qShot.getClosestNode().equals(bestShot.getClosestNode())){
				pShot = newShot;
				p = newPow;
			} else {
				qShot = newShot;
				q = newPow;
			}
			counter++;
			
		}
		
		b.setVelocity(pShot.getShot());
		b.setMoving(true);
		System.out.println("Ball shooted by the bot with velocity: " + pShot.getShot());
		
	}
	
	private static Vector3f generateShot(float angDeg, float power){
		float angle = (float) Math.toRadians(angDeg);
		float xVec = (float) Math.cos(angle);
		float zVec = (float) Math.sin(angle);
		Vector3f result = new Vector3f(xVec, 0, zVec);
		result.normalise();
		result.scale(power);
		return result;
	}
	
	@Override
	public Node createGraph() {
		float xBall = b.getPosition().x;
		float zBall = b.getPosition().z;
		int arraySize = (int) Math.ceil(Terrain.getSize()/CELL_SIZE);
		grid = new Node[arraySize][arraySize];
		for(int x=0; x<arraySize; x++)
			for(int z=0; z<arraySize; z++){
				float xCell = (x + 1/2) * CELL_SIZE;
				float zCell = (z + 1/2) * CELL_SIZE;
				float yCell = w.getHeightOfTerrain(xCell, zCell);//PhysicsEngine.getInstance().getHeightAt(xCell, zCell);
				Node node = new Node(new Vector3f(xCell,yCell,zCell));
				grid[x][z] = node;
				if(x > 0){
					Node west = grid[x-1][z];
					node.setWest(west, 0);
					west.setEast(node, 0);
				}
				if(z > 0){
					Node north = grid[x][z-1];
					node.setNorth(north, 0);
					north.setSouth(node, 0);
				}
			}
		int xBallGrid = (int)(xBall / CELL_SIZE);
		int zBallGrid = (int)(zBall / CELL_SIZE);
		Node ballNode = grid[xBallGrid][zBallGrid];
		ballNode.setWalkable(true);
		ballNode.setVisited(true);
		
		testWalkable(ballNode,w);
		
		int xHoleGrid = (int)(w.getEnd().x / CELL_SIZE);
		int zHoleGrid = (int)(w.getEnd().z / CELL_SIZE);
		Node holeNode = grid[xHoleGrid][zHoleGrid];
		holeNode.setD(0);
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid.length; j++){
				grid[i][j].setTested(false);
				grid[i][j].setTesting(false);
				for(int k = 0; k < 4; k++){
					Edge e = grid[i][j].getEdge(k);
					if(e != null)
						e.setVisited(false);
				}
			}
		}
		
		labelDistance(holeNode);
		
		return ballNode;
	}
	
	private void testWalkable(Node n, World world) {
		MinHeap<Node> open = new MinHeap<Node>();
		open.insert(n);
		int counter = 0;
		while(open.size() != 0){
			Node node = open.pop();
			for(int i=0; i<4; i++){
				Node neighbour = node.getNode(i);
				if(neighbour != null && !neighbour.isTested()){
					
					if(!neighbour.isVisited() ){
						Ball testBall = new VirtualBall(neighbour.getPosition());
						
						double stTime = System.currentTimeMillis();
						ArrayList<Entity> es = world.getCollidingEntities(testBall);
						double endTime = System.currentTimeMillis();
						//System.out.println("Milliseconds spent on checking collisions " + (endTime-stTime));
						
						if(es.size() > 0){
							
							stTime = System.currentTimeMillis();
							float newY = PhysicsEngine.getInstance().getHeightAt(neighbour.getPosition().x, neighbour.getPosition().z);
							endTime = System.currentTimeMillis();
							//System.out.println("Milliseconds spent on checking height at a point " + (endTime-stTime));
							
							neighbour.setPosition(new Vector3f(neighbour.getPosition().x, newY, neighbour.getPosition().z));
						}
						neighbour.setVisited(true);
					}
					if(!node.getEdge(i).isVisited()){
						float heightDiff = neighbour.getPosition().y - node.getPosition().y;
						if(Math.abs(heightDiff) > MAX_SLOPE){
							neighbour.setNode(((i + 2)%4), null);
							node.setNode(i, null);
						} else {
							float dist = node.getDistance(neighbour);
							node.getEdge(i).setLength(dist);
							neighbour.getEdge((i+2)%4).setLength(dist);
							if(!neighbour.isTesting()){
								open.insert(neighbour);
								neighbour.setTesting(true);
							}
						}
					}
				}
			}
		counter++;
		int percentage = (int)((counter * 1.0 / (grid.length * grid.length)) * 100);
		System.out.println("Percentage tested: " + percentage + "%");
		System.out.println("Open set size: " + open.size());
		n.setTested(true);
		n.setTesting(false);
	}
		
	}

	public static void labelDistance(Node n){
		MinHeap<Node> open = new MinHeap<Node>();
		open.insert(n);
		int counter = 0;
		while(open.size() != 0){
			Node node = open.pop();
			
			for(int i = 0; i < 4; i++){
				
				Node neighbour = node.getNode(i);
				Edge edge = node.getEdge(i);
				if(neighbour != null && !edge.isVisited()){
					float newD = node.getD() + edge.getLength();
					if(newD < neighbour.getD()){
						neighbour.setD(newD);
						if(!neighbour.isTesting()){
							open.insert(neighbour);
							neighbour.setTesting(true);
						}
					}
					edge.setVisited(true);
				}
				
			}
			
			counter++;
			//System.out.println("Edges Tested: " + counter);
			//System.out.println("Open set size: " + open.size());
		}
		
	}

	
}
