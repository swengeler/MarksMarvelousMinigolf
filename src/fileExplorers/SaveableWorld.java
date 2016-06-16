package fileExplorers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import entities.lights.Light;
import entities.obstacles.Entity;
import terrains.Terrain;
import terrains.World;

public class SaveableWorld implements Serializable{
	private List<Terrain> terrains = new ArrayList<Terrain>();
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> normalEntities = new ArrayList<Entity>();
	private List<Light> lights = new ArrayList<Light>();
	private Vector2f Start;
	private Vector2f End;
	private boolean hasStart, hasEnd;

	public SaveableWorld(World world) {
		this.terrains = world.getTerrains();
		this.entities = world.getEntities();
		this.normalEntities = world.getNormalEntities();
		this.lights = world.getLights();
		this.Start = new Vector2f(world.getStart().getX(),world.getStart().getZ());
		this.End = new Vector2f(world.getEnd().getX(),world.getEnd().getZ());
		this.hasStart = world.hasStart();
		this.hasEnd = world.hasEnd();
	}
	
	public List<Terrain> getTerrains() {
		return terrains;
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public List<Entity> getNormEntities() {
		return normalEntities;
	}
	
	public List<Light> getLights() {
		return lights;
	}
	
	public Vector2f getStart() {
		return Start;
	}
	
	public Vector2f getEnd() {
		return End;
	}
	
	public boolean hasStart() {
		return hasStart;
	}
	
	public boolean hasEnd() {
		return hasEnd;
	}
}
