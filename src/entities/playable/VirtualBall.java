package entities.playable;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import physics.collisions.PhysicalFace;
import physics.engine.PhysicsEngine;

public class VirtualBall implements Ball {
	
	private RealBall cloneOf;
	private Vector3f position, lastPosition, velocity, acceleration;
	private boolean moving;
	private Vector3f spin;
	
	public VirtualBall(RealBall cloneOf, Vector3f initVelocity) {
		this.cloneOf = cloneOf;
		this.position = new Vector3f(cloneOf.getPosition().x, cloneOf.getPosition().y, cloneOf.getPosition().z);
		this.lastPosition  = new Vector3f(-Float.MIN_VALUE, -Float.MIN_VALUE, -Float.MIN_VALUE);
		this.velocity = new Vector3f(initVelocity.x, initVelocity.y, initVelocity.z);
		this.acceleration = new Vector3f();
		this.moving = true;
		this.spin= new Vector3f();
	}
	
	public VirtualBall(Vector3f position) {
		this.position = new Vector3f(position.x, position.y, position.z);
	}

	public void updateAndMove() {
		lastPosition.set(position.x, position.y, position.z);

		// based on the newly updated velocity, move the ball
		Vector3f delta = new Vector3f(velocity.x, velocity.y, velocity.z);
		delta.scale(getTimeElapsed());
		increasePosition(delta.x, delta.y, delta.z);
		//System.out.printf("\nVirtualBall's position after updating: (%f|%f|%f)\n", position.x, position.y, position.z);
		//System.out.printf("VirtualBall's velocity after updating: (%f|%f|%f)\n", velocity.x, velocity.y, velocity.z);
	}

	public void move() {
		Vector3f delta = new Vector3f(velocity.x, velocity.y, velocity.z);
		delta.scale(getTimeElapsed());
		increasePosition(delta.x, delta.y, delta.z);
	}
	
	public float getTimeElapsed() {
		return 0.02f;
	}

	public void applyAccel(Vector3f accel) {
		//System.out.printf("Acceleration applied: (%f|%f|%f)\n", accel.x, accel.y, accel.z);
		acceleration.set(accel.x, accel.y, accel.z);
		acceleration.scale(getTimeElapsed());
		Vector3f.add(velocity, acceleration, velocity);
		//System.out.printf("... and velocity after: (%f|%f|%f)\n", velocity.x, velocity.y, velocity.z);
	}
	
	public void setMoving(boolean moving) {
		System.out.println("Moving set to " + moving);
		this.moving = moving;
		if (moving)
			resetLastPos();
	}

	public boolean isMoving() {
		return moving;
	}
	
	public boolean collidesWith(ArrayList<PhysicalFace> faces) {
		for (PhysicalFace f : faces) {
			if (f.collidesWithFace(this))
				return true;
		}
		return false;
	}
	
	public Vector3f getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3f v) {
		velocity.set(v.x, v.y, v.z);
	}

	public void scaleVelocity(float s) {
		velocity.scale(s);
	}

	public void increaseVelocity(float x, float y, float z) {
		velocity.x += x;
		velocity.y += y;
		velocity.z += z;
	}

	public void increaseVelocity(Vector3f v) {
		velocity.x += v.x;
		velocity.y += v.y;
		velocity.z += v.z;
	}

	public void setVelocity(float x, float y, float z) {
		velocity.set(x, y, z);
	}

	public boolean movedLastStep() {
		boolean moved = (Math.pow(position.x - lastPosition.x, 2) +
						Math.pow(position.y - lastPosition.y, 2) +
						Math.pow(position.z - lastPosition.z, 2) >
						Math.pow(PhysicsEngine.MIN_MOV_REQ, 2));
		return moved;
	}
	
	public void resetLastPos() {
		lastPosition.set(-Float.MIN_VALUE, -Float.MIN_VALUE, -Float.MIN_VALUE);
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public void increasePosition(float x, float y, float z) {
		position.x += x;
		position.y += y;
		position.z += z;
	}
	
	public void increasePosition(Vector3f v) {
		position.x += v.x;
		position.y += v.y;
		position.z += v.z;
	}

	public void setPosition(Vector3f p) {
		position.x = p.x;
		position.y = p.y;
		position.z = p.z;
	}
	
	public boolean equals(Object o) {
		if (o instanceof RealBall)
			return ((RealBall) o).equals(this.cloneOf);
		return super.equals(o);
	}

	@Override
	public void checkInputs() {
		
	}

	@Override
	public float getRotY() {
		return 0;
	}
	public void setRotation(Vector3f v){
	this.lastPosition=v;
	}
	public Vector3f getRotation(){
		return spin;
	}
	public float getRadius(){
		return RADIUS;
	}
}
