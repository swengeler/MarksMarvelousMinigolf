package entities.camera;



import entities.playable.RealBall;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import physics.engine.PhysicsEngine;
import models.TexturedModel;
import renderEngine.utils.DisplayManager;
import terrains.World;

public class Empty extends RealBall {

	private static final float RUN_SPEED = 20;
	private static final float TURN_SPEED = 100;
	
	private Vector3f currentVel = new Vector3f();
	private Vector3f currentAcc = new Vector3f();
	private float currentTurnSpeed = 0;
	private Camera camera;
	
	public Empty(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void checkInputs(World world) {
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
			this.currentVel.x = (float) (RUN_SPEED * -Math.sin(Math.toRadians(-camera.getYaw())));
			this.currentVel.z = (float) (RUN_SPEED * -Math.cos(Math.toRadians(-camera.getYaw())));
			
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
			this.currentVel.x = (float) -(RUN_SPEED * -Math.sin(Math.toRadians(-camera.getYaw())));
			this.currentVel.z = (float) -(RUN_SPEED * -Math.cos(Math.toRadians(-camera.getYaw())));
		} else {
			this.currentVel = new Vector3f();
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			this.currentTurnSpeed = -TURN_SPEED;
		else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			this.currentTurnSpeed = TURN_SPEED;
		else
			this.currentTurnSpeed = 0;
	}
}