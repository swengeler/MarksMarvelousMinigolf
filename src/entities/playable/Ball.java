package entities.playable;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import bot2_0.AIShot;
import physics.collisions.PhysicalFace;

public interface Ball {
	
	float REAL_RADIUS = 0.04267f;
	float REAL_MASS = 0.04593f;
	float RADIUS = 1f;
	float MIN_VEL = 0.5f;

	void updateAndMove();
	void move();
	float getTimeElapsed();
	Vector3f getPosition();
	Vector3f getLastPosition();
	Vector3f getVelocity();
	void setPosition(Vector3f p);
	void increasePosition(float x, float y, float z);
	void increasePosition(Vector3f v);
	void setVelocity(float x, float y, float z);
	void setVelocity(Vector3f v);
	void increaseVelocity(float x, float y, float z);
	void increaseVelocity(Vector3f v);
	void scaleVelocity(float s);
	void applyAccel(Vector3f v);
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
