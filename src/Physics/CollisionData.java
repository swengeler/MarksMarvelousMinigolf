package Physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.Ball;
import entities.RealBall;

public class CollisionData {

	private BoundingBox bbox;
	private ArrayList<PhysicalFace> faces;
	private ArrayList<PhysicalFace> collisionList;

	public CollisionData() {
		faces = new ArrayList<PhysicalFace>();
		collisionList = new ArrayList<PhysicalFace>();
	}

	public CollisionData(float[] ver, int[] ind) {
		faces = new ArrayList<PhysicalFace>(ind.length/3);
		collisionList = new ArrayList<PhysicalFace>();

		Vector3f p1 = new Vector3f(0,0,0), p2 = new Vector3f(0,0,0), p3 = new Vector3f(0,0,0);
		Vector3f normal = new Vector3f(0,0,0), v1 = new Vector3f(0,0,0), v2 = new Vector3f(0,0,0);

		float minX = Float.MAX_VALUE;
		float minY = minX;
		float minZ = minX;
		float maxX = Float.MIN_VALUE;
		float maxY = maxX;
		float maxZ = maxX;

		PhysicalFace face;
		int[] currInd = new int[3];
		for (int i = 0; i < ind.length; i += 3) {
			currInd[0] = ind[i] * 3;
			currInd[1] = ind[i + 1] * 3;
			currInd[2] = ind[i + 2] * 3;

			// first vertex
			p1.set(ver[currInd[0]], ver[currInd[0] + 1], ver[currInd[0] + 2]);
			// second vertex
			p2.set(ver[currInd[1]], ver[currInd[1] + 1], ver[currInd[1] + 2]);
			// third vertex
			p3.set(ver[currInd[2]], ver[currInd[2] + 1], ver[currInd[2] + 3]);

			// adjusting max/min values
			minX = Math.min(minX, Math.min(p1.x, Math.min(p2.x, p3.x)));
			minY = Math.min(minY, Math.min(p1.y, Math.min(p2.y, p3.y)));
			minZ = Math.min(minZ, Math.min(p1.z, Math.min(p2.z, p3.z)));
			maxX = Math.max(maxX, Math.max(p1.x, Math.max(p2.x, p3.x)));
			maxY = Math.max(maxY, Math.max(p1.y, Math.max(p2.y, p3.y)));
			maxZ = Math.max(maxZ, Math.max(p1.z, Math.max(p2.z, p3.z)));

			// constructing a face from the three points p1, p2 and p3 and their resulting normal
			Vector3f.sub(p2, p1, v1);
			Vector3f.sub(p3, p1, v2);
			Vector3f.cross(v1, v2, normal);
			normal.normalise();
			face = new PhysicalFace(normal, p1, p2, p3);

			faces.add(face);
		}

		bbox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
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
