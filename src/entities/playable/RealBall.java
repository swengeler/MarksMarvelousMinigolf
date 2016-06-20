package entities.playable;

import java.util.ArrayList;

import entities.obstacles.Entity;
import entities.camera.Camera;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import programStates.GameState;
import renderEngine.utils.DisplayManager;
import physics.collisions.PhysicalFace;
import physics.engine.PhysicsEngine;

public class RealBall extends Entity implements Ball {

	private static final float FACTOR = 0.5f;
	private static final float RUN_SPEED = 2;
	private static final float TURN_SPEED = 100;
	private static final float JUMP_POWER = 40;

	public static final float REAL_RADIUS = 0.04267f;
	public static final float REAL_MASS = 0.04593f;
	public static final float RADIUS = 1f;

	private static final float MAX_CHARGING_TIME = 3;
	private static final float POWER_SCALE = 100;

	private Vector3f currentVel;
	private Vector3f currentAcc;
    private Vector3f lastPositionMovementCheck;
	private Vector3f lastPositionActual;
	private boolean gameover=false;
	private float currentTurnSpeed;
	private float lastTimeElapsed;

	private int score;

	private boolean moving;

	private float initspeed = 0;
	private boolean charging;
	private boolean played;

	private Vector3f spin;

	public RealBall(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale, "ball");
        this.currentVel = new Vector3f();
        this.currentAcc = new Vector3f();
        this.lastPositionMovementCheck = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		this.lastPositionActual = new Vector3f(position.x, position.y, position.z);
		this.moving = false;
		this.spin = new Vector3f();
	}

	public void updateAndMove() {
		super.increaseRotation(0, currentTurnSpeed * getTimeElapsed(), 0);
		lastPositionMovementCheck.set(position);
		lastPositionActual.set(position);
		lastTimeElapsed = getTimeElapsed();

		// based on the newly updated velocity, move the ball
		Vector3f delta = new Vector3f(currentVel.x, currentVel.y, currentVel.z);
		delta.scale(getTimeElapsed());
		super.increasePosition(delta);
        super.increaseRotation(delta.z * 360, 0, delta.x * 360);

		System.out.printf("Ball's position after moving: (%f|%f|%f)\n", getPosition().x, getPosition().y, getPosition().z);
		System.out.printf("Ball's velocity after moving (with gravity applied): (%f|%f|%f)\n", currentVel.x, currentVel.y, currentVel.z);
		if (getVelocity().length() < Ball.MIN_VEL && Math.abs(getPosition().y - GameState.getInstance().getWorld().getHeightOfTerrain(getPosition().x, getPosition().z)) < 1) {
			setMoving(false);
		}
	}

	public void move() {
		Vector3f delta = new Vector3f(currentVel.x, currentVel.y, currentVel.z);
		delta.scale(getTimeElapsed());
		super.increasePosition(delta);
	}

    public void applyAccel(Vector3f accel) {
		//System.out.printf("Acceleration applied: (%f|%f|%f)\n", accel.x, accel.y, accel.z);
		currentAcc.set(accel.x, accel.y, accel.z);
		currentAcc.scale(getTimeElapsed());
		Vector3f.add(currentVel, currentAcc, currentVel);
		//System.out.printf("... and velocity after: (%f|%f|%f)\n", currentVel.x, currentVel.y, currentVel.z);
    }

	public void resetLastPos() {
		lastPositionMovementCheck.set(-Float.MIN_VALUE, -Float.MIN_VALUE, -Float.MIN_VALUE);
	}

	public void setMoving(boolean moving) {
		//System.out.println("Moving set to " + moving);
		this.moving = moving;
		if (moving)
			resetLastPos();
	}

	public boolean isMoving() {
		return moving;
	}

	private void jump() {
		setMoving(true);
		this.currentVel.y = JUMP_POWER;
		//System.out.println("Moving set to " + moving + " (velocity now: (" + currentVel.x + "|" + currentVel.y + "|" + currentVel.z + ")");
	}

	public void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_C)){
			//System.out.println("Here swapping from the ball at position " + this.getPosition().x );
			GameState.getInstance().swap();
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			if (!played){
				charging = true;
				initspeed += DisplayManager.getFrameTimeSeconds();
				if (initspeed > MAX_CHARGING_TIME)
					initspeed = MAX_CHARGING_TIME;
			}
		} else if (!Keyboard.isKeyDown(Keyboard.KEY_F) && charging) {
			charging = false;
			setMoving(true);

			System.out.println(initspeed + "hell is here");
			this.currentVel.x = (float) (initspeed * Math.sin(Math.toRadians(super.getRotY()+ Camera.getInstance().getAngleAroundBall()))) * POWER_SCALE;
			this.currentVel.z = (float) (initspeed * Math.cos(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall()))) * POWER_SCALE;

			initspeed=0;
			played = true;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.currentVel.x += (float) (RUN_SPEED * Math.sin(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR;
			this.currentVel.z += (float) (RUN_SPEED * Math.cos(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR;
			//System.out.println("Change in x-velocity: " + (RUN_SPEED * Math.sin(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR);
			//System.out.println("Change in z-velocity: " + (RUN_SPEED * Math.cos(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR);
			setMoving(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			this.currentVel.x -= (float) (RUN_SPEED * Math.sin(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR;
			this.currentVel.z -= (float) (RUN_SPEED * Math.cos(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR;
			//System.out.println("Change in x-velocity: " + (-RUN_SPEED * Math.sin(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR);
			//System.out.println("Change in z-velocity: " + (-RUN_SPEED * Math.cos(Math.toRadians(super.getRotY()+Camera.getInstance().getAngleAroundBall())))/FACTOR);
			setMoving(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			this.currentVel.z += 40;
			//System.out.println("x-speed increased by pressing up-arrow");
			setMoving(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			this.currentVel.z -= 40;
			//System.out.println("x-speed decreased by pressing down-arrow");
			setMoving(true);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			this.currentTurnSpeed = -TURN_SPEED;
			setMoving(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			this.currentTurnSpeed = TURN_SPEED;
			setMoving(true);
		} else
			this.currentTurnSpeed = 0;

		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			jump();
			this.setRotation(new Vector3f(-90,0,0));
			//this.setVelocity(10, 0, 0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_P)){
			this.setVelocity(50, this.getVelocity().y, this.getVelocity().z);
		}
	}

	public boolean collidesWith(ArrayList<PhysicalFace> faces) {
		for (PhysicalFace f : faces) {
			if (f.collidesWithFace(this))
				return true;
		}
		return false;
	}

	public Vector3f getVelocity() {
		return currentVel;
	}

	public void setVelocity(Vector3f v) {
        //System.out.printf("Velocity of ball %s set to: (%f|%f|%f)\n", this.toString(), v.x, v.y, v.z);
		currentVel.set(v);
	}

	public void scaleVelocity(float s) {
		currentVel.scale(s);
	}

	public void increaseVelocity(float x, float y, float z) {
		currentVel.x += x;
		currentVel.y += y;
		currentVel.z += z;
	}

	public void increaseVelocity(Vector3f v) {
		currentVel.x += v.x;
		currentVel.y += v.y;
		currentVel.z += v.z;
	}

	public void setVelocity(float x, float y, float z) {
		//System.out.printf("Velocity of ball %s set to: (%f|%f|%f)\n", this.toString(), x, y, z);
		currentVel.set(x, y, z);
	}

	public boolean movedLastStep() {
		System.out.printf("Difference in positions: (%f|%f|%f)\n", super.getPosition().x - lastPositionMovementCheck.x, super.getPosition().y - lastPositionMovementCheck.y, super.getPosition().z - lastPositionMovementCheck.z);
		boolean moved = (Math.pow(super.getPosition().x - lastPositionMovementCheck.x, 2) +
						Math.pow(super.getPosition().y - lastPositionMovementCheck.y, 2) +
						Math.pow(super.getPosition().z - lastPositionMovementCheck.z, 2) >
						Math.pow(PhysicsEngine.MIN_MOV_REQ, 2));
		//System.out.println("Therefore moved is " + moved);
		return moved;
	}

	public float getTimeElapsed() {
		float answer;
		if ((answer = DisplayManager.getFrameTimeSeconds()) < 0.03)
			return answer;
		return 0.03f;
	}

	public float getLastTimeElapsed() {
		return lastTimeElapsed;
	}

	public Vector3f getLastPosition() {
		return lastPositionActual;
	}

	public String toString() {
		return "Ball at (" + getPosition().x + "|" + getPosition().y + "|" + getPosition().z + ") with velocity (" + currentVel.x + "|" + currentVel.y + "|" + currentVel.z + ")" ;
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

	public void addScore(){
		score++;
	}

	public int getScore(){
		return score;
	}

	public boolean isPlayed(){
		return played;
	}

	public void setPlayed(boolean p){
		played = p;
	}

	public void setRotation(Vector3f v){
		this.spin = v;
		System.out.println("fuck you " + spin.x + " " + spin.y + " " + spin.z);
	}

	public Vector3f getRotation(){
		return spin;
	}

	public float getRadius(){
		return RADIUS;
	}

}
