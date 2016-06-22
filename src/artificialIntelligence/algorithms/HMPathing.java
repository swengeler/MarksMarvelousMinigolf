package artificialIntelligence.algorithms;

import java.util.ArrayList;

import artificialIntelligence.utils.AIShot;
import artificialIntelligence.utils.Edge;
import artificialIntelligence.utils.MinHeap;
import artificialIntelligence.utils.Node;
import artificialIntelligence.viewer.GraphFrame;
import org.lwjgl.util.vector.Vector3f;

import entities.obstacles.Entity;
import entities.playable.Ball;
import entities.playable.RealBall;
import entities.playable.VirtualBall;
import physics.engine.PhysicsEngine;
import physics.noise.Friction;
import physics.noise.Restitution;
import terrains.Terrain;
import terrains.World;

public class HMPathing extends Algorithm {

	private static final float MAX_SLOPE = 3.0f; // That is the maximum height difference between two adjacent cell for them to be connected
	public static float MAX_SHOT_POWER = 1000;
	public static float DELTA_ANGLE = 2f; // In degrees
	private static final int MIDPOINT_ITERATIONS = 100;
	private static final float DELTA_CHECK = 0.5f;
	private static final int ITER_IN_BETWEEN = 4;
	
	private Node[][] grid;
	private Ball b;
	private World w;
	private Node holeNode;
	private int xHole, zHole;
	public static float maxDistance;

	public static long sum;
	public static int count;

	
	public HMPathing(Ball b, World w) {
		this.b = b;
		this.w = w;
		long one = System.nanoTime();
		createGraph();
		System.out.println("Time to build graph: " + (System.nanoTime() - one) + " ns");
	}
	
	public void shootBall() {
		long before = System.nanoTime();

		int xBallGrid = (int)(b.getPosition().x/CELL_SIZE);
		int zBallGrid = (int)(b.getPosition().z/CELL_SIZE);
		Node ballNode = grid[xBallGrid][zBallGrid];
		float ballD = ballNode.getD();
		AIShot bestShot = null;
		Vector3f straightVec = straightShotNonRandom(0f, w.getEnd(), b.getPosition());
		Vector3f finalPowah = null;
		if (isStraightShotPossible(b, w.getEnd())) {
			//System.out.println("Straight shot is possible");
			bestShot = PhysicsEngine.getInstance().aiTestShot((RealBall) b, straightVec, grid);
			finalPowah = bestShot.getShot();
		} else {
			//System.out.println("Straight shot is not possible");
			MinHeap<AIShot> shots = new MinHeap<>();
			int shotsTaken = 0;
			for (int i = 0; i < 360; i += DELTA_ANGLE){
				shotsTaken++;
				Vector3f vec = generateShot(i, MAX_SHOT_POWER);
				//System.out.println("Testing shot number: " + shotsTaken + " with vector " + vec);
				AIShot shot = PhysicsEngine.getInstance().aiTestShot((RealBall) b, vec, grid);
				//System.out.println("The length of the array of nodes is " + shot.getNodes().size());
				//System.out.println("The nodes traversed by this shot are " + shot.getNodes());
				//System.out.println("The closest node of this shot is at a distance " + shot.getClosestNode().getD());
				shots.insert(shot);
				if(shot.getClosestNode().getD() < 2)
					break; 
			}
			int numberOfShots = shots.size();
			bestShot = shots.pop();
			/*
			if(bestShot.getClosestNode().getD() > 3){
				ArrayList<AIShot> bestShots = new ArrayList<AIShot>();
				for(int i=0; i<numberOfShots/2; i++){
					bestShots.add(shots.pop());
				}
				for(int i=0; i<bestShots.size(); i++){
					AIShot s1 = bestShots.get(i);
					AIShot s2 = bestShots.get((i+1) % bestShots.size());
					float ang = (float) Math.toDegrees(Vector3f.angle(s1.getShot(), s2.getShot()));
					if(Math.abs(ang) <= DELTA_ANGLE + 1 ){
						
						float dAngle =  (DELTA_ANGLE / (float)ITER_IN_BETWEEN);
						
						for(int j = 1; j < ITER_IN_BETWEEN; j++){
							Vector3f direction = generateShot(s1.getAngle() + j * dAngle, MAX_SHOT_POWER);
							AIShot newShot = PhysicsEngine.getInstance().aiTestShot((RealBall) b, direction, grid);
							shots.insert(newShot);
						}
					}
					shots.insert(s1);
				}
			}
			bestShot = shots.pop();
			
			
			float p = MAX_SHOT_POWER;
			float q = 0;
			Vector3f shotDir = new Vector3f(bestShot.getShot().x, 0, bestShot.getShot().z);
			shotDir.normalise();
			float xBestShot = shotDir.x;
			float zBestShot = shotDir.z;
			AIShot pShot = bestShot;
			AIShot qShot = PhysicsEngine.getInstance().aiTestShot((RealBall)b, new Vector3f(), grid);
			int counter = 0;
			
			
			while(!bestShot.getClosestNode().equals(pShot.getNodes().get(pShot.getNodes().size() - 1)) && counter < MIDPOINT_ITERATIONS){
				counter++;
				float newPow = (p + q)/2;
				Vector3f vec = new Vector3f(xBestShot * newPow, 0, zBestShot * newPow);
				AIShot newShot = PhysicsEngine.getInstance().aiTestShot((RealBall) b, vec, grid);
				
				if(newShot.getClosestNode().equals(bestShot.getClosestNode())){
					pShot = newShot;
					p = newPow;
				} else {
					qShot = newShot;
					q = newPow;
				}
				
			}
			bestShot = pShot;*/
			finalPowah = getThePowah(bestShot);
		} 
		//System.out.println("The best shot ends up at a distance " + bestShot.getClosestNode().getD() + " from the hole");
		b.setVelocity(bestShot.getShot());
		b.setMoving(true);
		//System.out.println("Ball shot by the bot with velocity: " + bestShot.getShot());
		long duration = (System.nanoTime() - before);
		sum += duration;
		count++;
		System.out.println("Next shot " + duration + " ns");
		
	}
	
/*	private boolean isStraightShotPossible(Ball b, Vector3f end) {
		VirtualBall vb = new VirtualBall((RealBall) b, new Vector3f());
		Vector3f dirVec = Vector3f.sub(end, b.getPosition(), null);
		dirVec.y = 0;
		dirVec.normalise();
		dirVec.scale(DELTA_CHECK);
		while(Vector3f.sub(vb.getPosition(), end, null).length() > DELTA_CHECK){
			vb.increasePosition(dirVec);
			ArrayList<Entity> arr = w.getCollidingEntities(vb);
			//ArrayList<Face> arr = w.getCollidingFacesEntities(vb);
			System.out.println("The length of the array is " + arr.size());
			if(arr.size() != 0)
				return false;
		}
		return true;
	}*/
	
	private boolean isStraightShotPossible(Ball b, Vector3f end) {
		return !w.obstaclesIntersectedBySegment(b.getPosition(), end);
	}
	

	private Vector3f straightShotNonRandom(float finalVelocity, Vector3f holePosition, Vector3f ballPosition) {
        Vector3f temp = Vector3f.sub(holePosition, ballPosition, null);
        float finalMagnitude = (float) Math.sqrt(Math.pow(finalVelocity, 2) + 2 * Friction.COEFFICIENT * 230 * temp.length());
		if (temp.lengthSquared() != 0)
        	temp.normalise();
        temp.scale(finalMagnitude);
        return temp;
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
	public Node createGraph(){
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
				Edge edge = new Edge(0);
				Edge edge1 = new Edge(0);
				if(x > 0){
					Node west = grid[x-1][z];
					node.setWest(west, edge);
					west.setEast(node, edge);
				}
				if(z > 0){
					Node north = grid[x][z-1];
					node.setNorth(north, edge1);
					north.setSouth(node, edge1);
				}
			}
		int xBallGrid = (int)(xBall / CELL_SIZE);
		int zBallGrid = (int)(zBall / CELL_SIZE);
		Node ballNode = grid[xBallGrid][zBallGrid];
		ballNode.setWalkable(true);
		ballNode.setVisited(true);
		
		testWalkable(ballNode,w);
		
		xHole = (int)(w.getEnd().x / CELL_SIZE);
		zHole = (int)(w.getEnd().z / CELL_SIZE);
		holeNode = grid[xHole][zHole];
		int newxHole = xHole;
		int newzHole = zHole;
		for(int i=0; i<5; i++)
			for(int j=0; j<5; j++){
				int x = xHole - 2 + i;
				int z = zHole - 2 + j;
				if(x>=0 && x<grid.length && z>=0 && z<grid[0].length && grid[x][z].getPosition().y < holeNode.getPosition().y){
					holeNode = grid[x][z];
					newxHole = x;
					newzHole = z;
				}
					
			}
		xHole = newxHole;
		zHole = newzHole;
		for(int i=0; i<4; i++)
			for(int j=0; j<4; j++){
				int x = xHole - 2 + i;
				int z = zHole + 2 + j;
				if(x>=0 && x<grid.length && z>=0 && z<grid[0].length){
					for(int k=0; k<2; k++)
						if(grid[x][z].getEdge(k) != null)
							grid[x][z].getEdge(k).setLength(1);
				}
			}
		holeNode.setD(0);
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid.length; j++){
				grid[i][j].setTested(false);
				grid[i][j].setTesting(false);
				grid[i][j].setVisited(false);
				for(int k = 0; k < 4; k++){
					Edge e = grid[i][j].getEdge(k);
					if(e != null)
						e.setVisited(false);
				}
			}
		}
		
		labelDistance(holeNode);
		
		//printGrid(holeNode);
		showGraphInFrame(holeNode);
		
		return ballNode;
	}
	
	private void printGrid(Node holeNode) {
		Node node;
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[0].length; j++){
				node = grid[i][j];
				if (node.equals(holeNode))
					System.out.print("H");
				else
					System.out.print(((double)((int)(node.getD()*10)))/10);
				System.out.print("\t");
			}
			System.out.println();
			System.out.println();
		}
	}

	private void showGraphInFrame(Node holeNode) {
		GraphFrame frame = new GraphFrame(holeNode, grid);
	}

	private void testWalkable(Node n, World world) {
		MinHeap<Node> open = new MinHeap<Node>();
		open.insert(n);
		int counter = 0;
		while(open.size() != 0){
			Node node = open.pop();
			for(int i=0; i<4; i++){
				Node neighbour = node.getNeighbourNode(i);
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
							neighbour.setNeighbourNode(((i + 2)%4), null, null);
							node.setNeighbourNode(i, null, null);
						} else {
							float dist = 1; //node.getDistance(neighbour);
							node.getEdge(i).setLength(dist);
							if(!neighbour.isTesting()){
								open.insert(neighbour);
								neighbour.setTesting(true);
							}
							node.getEdge(i).setVisited(true);
						}
					}
				}
			}
		counter++;
		int percentage = (int)((counter * 1.0 / (grid.length * grid.length)) * 100);
		//System.out.println("Percentage tested: " + percentage + "%");
		//System.out.println("Open set size: " + open.size());
		n.setTested(true);
		n.setTesting(false);
	}
		
	}

	public static void labelDistance(Node n){
		MinHeap<Node> open = new MinHeap<>();
		open.insert(n);
		int counter = 0;
		float curDistance;
		while(open.size() != 0){
			Node node = open.pop();
			
			for (int i = 0; i < 4; i++){
				Node neighbour = node.getNeighbourNode(i);
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
					neighbour.setVisited(true);

					if ((curDistance = neighbour.getD()) > maxDistance)
						maxDistance = curDistance;
				}
				
			}

			if ((curDistance = node.getD()) > maxDistance)
				maxDistance = curDistance;
			
			counter++;
			//System.out.println("Edges Tested: " + counter);
			//System.out.println("Open set size: " + open.size());
		}
		
	}
	
	private Vector3f getThePowah(AIShot shot){
		float total = 0;
		ArrayList<Vector3f> positions = shot.reduceBallPositions();
		for (int i = positions.size() - 1; i > 0; i--) {
			Vector3f pos1 = positions.get(i);
			Vector3f pos2 = positions.get(i-1);
			
			Vector3f straightShot = straightShotNonRandom(total, pos1, pos2);
			total = straightShot.length();
			if (i != 1) {
				total /= Restitution.COEFFICIENT;
			}
		}
		Vector3f finalPowah = shot.getShot();
		finalPowah.normalise();
		finalPowah.scale(total);
		return finalPowah;
	}
	
}
