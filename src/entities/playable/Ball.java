package entities.playable;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import physics.collisions.PhysicalFace;

public interface Ball {
	
	float REAL_RADIUS = 0.04267f;
	float REAL_MASS = 0.04593f;
	float RADIUS = 1f;

	void updateAndMove();
	void move();
	float getTimeElapsed();
	Vector3f getPosition();
	Vector3f getVelocity();
	void setPosition(Vector3f p);
	void increasePosition(float x, float y, float z);
	void increasePosition(Vector3f v);
	void setVelocity(float x, float y, float z);
	void setVelocity(Vector3f v);
	void increaseVelocity(float x, float y, float z);
	void increaseVelocity(Vector3f v);
	void scaleVelocity(float s);
	void addAccel(Vector3f accel);
	void removeAccel(Vector3f accel);
	void setMoving(boolean moving);
	boolean isMoving();
	boolean movedLastStep();
	boolean collidesWith(ArrayList<PhysicalFace> faces);
	void checkInputs();
	float getRotY();
	Vector3f getRotation();
	float getRadius();
	void setRotation(Vector3f vector3f);
	
}
