package terrains;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.collisions.PhysicalFace;
import entities.obstacles.Entity;
import entities.lights.Light;
import toolbox.LinearAlgebra;
import entities.playable.Ball;
import entities.camera.Camera;

public class World implements Serializable {

	private List<Terrain> terrains = new ArrayList<>();
	private List<Entity> entities = new ArrayList<>();
	private List<Entity> normalEntities = new ArrayList<>();
	private List<Light> lights = new ArrayList<>();
	private Camera camera;
	private Vector2f Start = new Vector2f(Terrain.getSize()/2f, Terrain.getSize()/2f);
	private Vector2f End = new Vector2f(20, Terrain.getSize() - 10);
	private boolean hasStart = false, hasEnd = false;
	
	
	public World(Camera camera){
		this.camera = camera;
	}
	
	public void start(){
		camera.move();
	}
	
	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	public Camera getCamera(){
		return camera;
	}
	
	public void add(Terrain terrain){
		terrains.add(terrain);
	}
	
	public void add(Entity entity){
		entities.add(entity);
	}

	public void clearEntities() {
		entities.clear();
	}

	public void removeLastEntity() {
		if (entities.size() > 0)
			entities.remove(entities.size() - 1);
	}
	
	public void removeTerrain() {
		terrains.clear();
	}
	
	public void add(Light light){
		lights.add(light);
	}
	
	public void addEntities(List<Entity> entity){
		entities.addAll(entity);
	}
	
	public void setEntities(List<Entity> entity){
		entities = entity;
	}
	
	public void addLights(List<Light> light){
		lights.addAll(light);
	}
	
	public List<Terrain> getTerrains(){
		return terrains;
	}
	
	public List<Entity> getEntities(){
		return entities;
	}
	
	public void addNormE(List<Entity> normEnt){
		getNormalEntities().addAll(normEnt);
	}
	
	public void addNE(Entity e){
		normalEntities.add(e);
	}
	
	public List<Light> getLights(){
		return lights;
	}
	
	public boolean obstaclesIntersectedBySegment(Vector3f p1, Vector3f p2) {
	    for (Entity e : entities) {
	        if (e.isCollidable() && e.isIntersectedBySegment(p1, p2)) {
	            return true;
	        }
	    }
	    return false;
	}

	public Vector3f getLastIntersectionPointSegment(Vector3f p1, Vector3f p2) {
		long before = System.currentTimeMillis();
		ArrayList<Vector3f> curList;
		Vector3f closest = null;
		double curDistanceSq, minDistanceSq = Double.MAX_VALUE;
		for (Entity e : entities) {
			if (e.isCollidable()) {
				curList = e.getIntersectionPointsSegment(p1, p2);
				for (Vector3f v : curList) {
					// this is the distance because the segment goes from p1 to p2 (p1 = curBallPosition, p2 = nextBallPosition)
					curDistanceSq = Math.pow(p1.x - v.x, 2) + Math.pow(p1.y - v.y, 2) + Math.pow(p1.z - v.z, 2);
					if (curDistanceSq < minDistanceSq) {
						closest = v;
						minDistanceSq = curDistanceSq;
					}
				}
			}
		}
		System.out.println("Getting intersection point in world took " + (System.currentTimeMillis() - before) + " ms");
		return closest;
	}
	
	public ArrayList<Entity> getCollidingEntities(Ball b) {
		ArrayList<Entity> obstaclesHit = new ArrayList<>();
		for (Entity e : entities) {
			if (e.isCollidable() && e.inBounds(b))
				//System.out.println("VirtualBall with position " + b.getPosition() + " for test may collide with " + e);
			if (e.isCollidable() && e.inBounds(b) && e.collides(b)) {
				//System.out.println("VirtualBall with position " + b.getPosition() + " for test collides with " + e);
				obstaclesHit.add(e);
			}
		}
		return obstaclesHit;
	}
	
	public ArrayList<PhysicalFace> getCollidingFacesEntities(Ball b) {
		ArrayList<PhysicalFace> collidingFaces = new ArrayList<>();
		for (Entity e : entities) {
			if (e.isCollidable() && e.inBounds(b)) {
				collidingFaces.addAll(e.getCollidingFaces(b));
				System.out.println("Ball may collide with " + e + ".");
			}
		}
		return collidingFaces;
	}
	
	public Terrain getTerrain(float x, float z){
		float gX = (float) Math.floor(x/Terrain.getSize());
		float gZ = (float) Math.floor(z/Terrain.getSize());
		for(Terrain t:terrains)
			if(gX == t.getGridX() && gZ == t.getGridZ())
				return t;
		return null;
	}
	
	public float getHeightOfTerrain(float x, float z){
		Terrain t = getTerrain(x,z);
		if(t != null)
			return getHeightTerrain(x,z,t);
		return 0;
	}
	
	public static float getHeightTerrain(float worldX, float worldZ, Terrain terrain){
		float terrainX = worldX - terrain.getX();
		float terrainZ = worldZ - terrain.getZ();
		float gridSquareSize = Terrain.getSize() / (float) (terrain.getHeights().length-1);
		int gridX = (int) Math.floor(terrainX/ gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ/ gridSquareSize);
		if(gridX >= terrain.getHeights().length -1 || gridZ >= terrain.getHeights().length-1 || gridX<0 || gridZ<0){
			return 0;
		}
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		float answer;
		if (xCoord <= (1-zCoord)) {
			answer = LinearAlgebra.barryCentric(new Vector3f(0, terrain.getHeights()[gridX][gridZ], 0), new Vector3f(1,
					terrain.getHeights()[gridX + 1][gridZ], 0), new Vector3f(0,
							terrain.getHeights()[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		} else {
			answer = LinearAlgebra.barryCentric(new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ], 0), new Vector3f(1,
					terrain.getHeights()[gridX + 1][gridZ + 1], 1), new Vector3f(0,
							terrain.getHeights()[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		return answer;
	}
	
	public Vector3f getNormalOfTerrain(float x, float z){
		Terrain t = getTerrain(x,z);
		if(t != null)
			return getNormalTerrain(x,z,t);
		return new Vector3f(0, 1, 0);
	}
	
	public static Vector3f getNormalTerrain(float worldX, float worldZ, Terrain terrain){
		float terrainX = worldX - terrain.getX();
		float terrainZ = worldZ - terrain.getZ();
		float gridSquareSize = Terrain.getSize() / (float) (terrain.getHeights().length-1);
		int gridX = (int) Math.floor(terrainX/ gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ/ gridSquareSize);
		if(gridX >= terrain.getHeights().length -1 || gridZ >= terrain.getHeights().length-1 || gridX<0 || gridZ<0){
			return new Vector3f(0, 1, 0);
		}
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		Vector3f answer = new Vector3f(0,1,0);
		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		if (xCoord <= (1-zCoord)) {
			Vector3f.sub(new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ], 0), new Vector3f(0, terrain.getHeights()[gridX][gridZ], 0), a);
			Vector3f.sub(new Vector3f(0, terrain.getHeights()[gridX][gridZ + 1], 1), new Vector3f(0, terrain.getHeights()[gridX][gridZ], 0), b);
			Vector3f.cross(b, a, answer);
		} else {
			Vector3f.sub(new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ + 1], 1), new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ], 0), a);
			Vector3f.sub(new Vector3f(0, terrain.getHeights()[gridX][gridZ + 1], 1), new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ], 0), b);
			Vector3f.cross(b, a, answer);
		}
		answer.normalise();
		return answer;
	}

	public List<Entity> getNormalEntities() {
		return normalEntities;
	}

	public void setNormalEntities(List<Entity> normalEntities) {
		this.normalEntities = normalEntities;
	}
	
	public void setStart(Vector2f position) {
		this.Start = position;
		this.hasStart = true;
	}
	
	public void setEnd(Vector2f position) {
		System.out.println("New hole at x: " + position.x + " z: " + position.y);
		this.End =  position;
		this.hasEnd = true;
	}
	
	public Vector3f getStart() {
		return new Vector3f(Start.x, getHeightOfTerrain(Start.x, Start.y), Start.y);
	}
	
	public Vector3f getEnd() {
		return new Vector3f(End.x, Ball.RADIUS, End.y);
	}
	
	public boolean hasStart() {
		return hasStart;
	}
	
	public boolean hasEnd() {
		return hasEnd;
	}
	
	public void setHasStart(boolean b) {
		this.hasStart = b;
	}
	
	public void setHasEnd(boolean b) {
		this.hasEnd = b;
	}
	
}
