package Physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Ball;
import entities.RealBall;

public class CollisionData {

	private BoundingBox bbox;
	protected ArrayList<PhysicalFace> faces;
	protected ArrayList<PhysicalFace> collisionList;

	public CollisionData() {
		faces = new ArrayList<>();
		collisionList = new ArrayList<>();
	}

	public void addFace(PhysicalFace face) {
		faces.add(face);
	}

	public void setBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		bbox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BoundingBox getBoundingBox() {
		return bbox;
	}

	public ArrayList<PhysicalFace> getCollidingFaces(Ball b) {
		collisionList.clear();
		for (PhysicalFace f : faces) {
			if (f.collidesWithFace(b))
				collisionList.add(f);
		}
		return collisionList;
	}

	public boolean inBounds(Ball b) {
		return bbox.inBoundingBox(b);
	}
	
	public boolean collides(Ball b) {
		for (PhysicalFace f : faces) {
			if (f.collidesWithFace(b))
				return true;
		}
		return false;
	}

}
