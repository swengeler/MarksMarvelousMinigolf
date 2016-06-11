package physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.obstacles.Entity;

public class ShotData {
	
	private Vector3f startVelocity;
	private Vector3f endPosition;
	private ArrayList<Entity> obstaclesHit;

	public ShotData(Vector3f startVelocity, Vector3f endPosition, ArrayList<Entity> obstaclesHit) {
		this.startVelocity = new Vector3f(startVelocity.x, startVelocity.y, startVelocity.z);
		this.endPosition = new Vector3f(endPosition.x, endPosition.y, endPosition.z);
		this.obstaclesHit = (ArrayList<Entity>) obstaclesHit.clone();
	}
	
	public Vector3f getStartVelocity() {
		return startVelocity;
	}

	public Vector3f getEndPosition() {
		return endPosition;
	}

	public ArrayList<Entity> getObstaclesHit() {
		return obstaclesHit;
	}
	
}
