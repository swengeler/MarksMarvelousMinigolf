 package bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import physics.PhysicsEngine;
import entities.playable.Ball;
import entities.camera.Camera;
import guis.GuiTexture;
import models.TexturedModel;
import objConverter.ModelData;
import renderEngine.utils.Loader;
import terrains.World;
import water.WaterTile;

public class bot {
	private Map<String,TexturedModel> tModels = new HashMap<String,TexturedModel>();
	private Map<String,ModelData> mData = new HashMap<String,ModelData>();
	private World world;
	private ArrayList<WaterTile> waterTiles;
	private ArrayList<GuiTexture> guis;
	private Camera camera;
	private Loader loader;
	private float bothit=100;

	float a;
	float b;
	private boolean first=true;
	
	public bot(World world){
		this.world=world;
	}
	
	public Vector3f ab(Ball ball){
		//float angle=(world.getEnd().z-world.getStart().z)/(world.getEnd().x-world.getStart().x);
		float angle=(world.getEnd().z-ball.getPosition().z)/(world.getEnd().x-ball.getPosition().z);
		Vector3f abc= Vector3f.sub( world.getEnd(),ball.getPosition(), null);
		abc.normalise();
		float distance= (float) Math.sqrt(Math.pow((world.getEnd().z-ball.getPosition().z),2)+Math.pow((world.getEnd().x-ball.getPosition().x),2));
		float u= (float) Math.sqrt(Math.abs((2*distance*PhysicsEngine.COEFF_FRICTION*PhysicsEngine.GRAVITY.y)));
		abc.scale((float) (u*1.4));
		//a= (float)(u* Math.sin(Math.toRadians(abc)));
	//	b=(float) (u* Math.cos(Math.toRadians()));
	//System.out.print("a= " + a);
	//System.out.print("b= " + b);
		return new Vector3f(abc.x,0f, abc.z);
		
	}
	public float geta(){
		return a;
	}
	public float getb(){
		return b;
	}
}
