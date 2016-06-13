package terrains;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import Physics.PhysicalFace;
import entities.Entity;
import entities.Light;
import toolbox.Maths;
import entities.Ball;
import entities.Camera;

public class World implements Serializable{
	private List<Terrain> terrains = new ArrayList<Terrain>();
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> normalEntities = new ArrayList<Entity>();
	private List<Light> lights = new ArrayList<Light>();
	private Camera camera;
	private Vector2f Start = new Vector2f(Terrain.getSize()/2f, Terrain.getSize()/2f);
	private Vector2f End = new Vector2f(Terrain.getSize()/4f, Terrain.getSize()/4f);
	
	
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
	
	public ArrayList<Entity> getCollidingEntities(Ball b) {
		ArrayList<Entity> obstaclesHit = new ArrayList<Entity>();
		for (Entity e : entities) {
			if (e.collides(b)) {
				obstaclesHit.add(e);
			}
		}
		return obstaclesHit;
	}
	
	public ArrayList<PhysicalFace> getCollidingFacesEntities(Ball b) {
		ArrayList<PhysicalFace> collidingFaces = new ArrayList<PhysicalFace>();
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
	
	public ArrayList<PhysicalFace> getCollidingFacesTerrains(Ball b) {
		System.out.println("getCollidingFaces in World is called (there are " + terrains.size() + " terrains)");
		ArrayList<PhysicalFace> collidingFaces = new ArrayList<PhysicalFace>();
		for (Terrain t : terrains) {
			if (t.ballInTerrain(b))
				collidingFaces.addAll(t.getCollidingFaces(b));
		}
		return collidingFaces;
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
			answer = Maths.barryCentric(new Vector3f(0, terrain.getHeights()[gridX][gridZ], 0), new Vector3f(1,
					terrain.getHeights()[gridX + 1][gridZ], 0), new Vector3f(0,
							terrain.getHeights()[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		} else {
			answer = Maths.barryCentric(new Vector3f(1, terrain.getHeights()[gridX + 1][gridZ], 0), new Vector3f(1,
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
	}
	
	public void setEnd(Vector2f position) {
		System.out.println("New hole at x: " + position.x + " z: " + position.y);
		this.End =  position;
	}
	
	public Vector3f getStart() {
		return new Vector3f(Start.x, getHeightOfTerrain(Start.x, Start.y), Start.y);
	}
	
	public Vector3f getEnd() {
		return new Vector3f(End.x, getHeightOfTerrain(End.x, End.y), End.y);
	}
	
}
