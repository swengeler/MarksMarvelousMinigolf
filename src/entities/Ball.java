package entities;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import Physics.PhysicalFace;
import terrains.World;

public interface Ball {
	
	public static final float REAL_RADIUS = 0.04267f;
	public static final float REAL_MASS = 0.04593f;
	public static final float RADIUS = 1f;

	public void updateAndMove();
	public float getTimeElapsed();
	public Vector3f getPosition();
	public Vector3f getVelocity();
	public void setPosition(Vector3f p);
	public void increasePosition(float x, float y, float z);
	public void increasePosition(Vector3f v);
	public void setVelocity(float x, float y, float z);
	public void setVelocity(Vector3f v);
	public void increaseVelocity(float x, float y, float z);
	public void increaseVelocity(Vector3f v);
	public void scaleVelocity(float s);
	public void addAccel(Vector3f accel);
	public void removeAccel(Vector3f accel);
	public void setMoving(boolean moving);
	public boolean isMoving();
	public boolean movedLastStep();
	public boolean collidesWith(ArrayList<PhysicalFace> faces);
	public void checkInputs();
	public float getRotY();
	public Vector3f getRotation();
	public float getRadius();
	public void setRotation(Vector3f vector3f);
	
}
