package physics.collisions;

import java.io.Serializable;
import java.util.ArrayList;

import entities.playable.Ball;
import org.lwjgl.util.vector.Vector3f;
import physics.engine.PhysicsEngine;

public class CollisionData implements Serializable{

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

	public boolean inHorizontalBounds(Ball b) {
		return bbox.inHorizontalBoundingRectangle(b);
	}

	public boolean inHorizontalBounds(float x, float z) {
		return bbox.inHorizontalBoundingRectangle(x, z);
	}

	public float getHighestPointOnLine(Vector3f p, Vector3f q) {
		Vector3f pq = Vector3f.sub(q, p, null);
		Vector3f m = Vector3f.cross(pq, q, null);
		Vector3f r = new Vector3f(), a = new Vector3f(), b = new Vector3f(), c = new Vector3f(), temp = new Vector3f();
		float u, v, w, denom, curHeight, maxHeight = - Float.MAX_VALUE;
		for (PhysicalFace f : faces) {
			a.set(f.getP1());
			b.set(f.getP2());
			c.set(f.getP3());

			u = Vector3f.dot(pq, Vector3f.cross(c, b, temp)) + Vector3f.dot(m, Vector3f.sub(c, b, temp));
			v = Vector3f.dot(pq, Vector3f.cross(a, c, temp)) + Vector3f.dot(m, Vector3f.sub(a, c, temp));
			w = Vector3f.dot(pq, Vector3f.cross(b, a, temp)) + Vector3f.dot(m, Vector3f.sub(b, a, temp));
			//System.out.println("Face " + f + " processed");
			if ((u * v) > 0 && (u * w) > 0) {
				denom = 1 / (u + v + w);
				u *= denom;
				v *= denom;
				w *= denom;

				curHeight = a.y * u + b.y * v + c.y * w;
				//System.out.println("Face " + f + " has height " + curHeight);

				if (curHeight > maxHeight) {
					maxHeight = curHeight;
					//System.out.println("Is also maxHeight");
				}
			}
		}
		return maxHeight;
	}
	
	public boolean isIntersectedBySegment(Vector3f p1, Vector3f p2) {
	    if (bbox.isIntersectedBySegment(p1, p2)) {
	        for (PhysicalFace f : faces) {
	            if (f.isIntersectedBySegment(p1, p2)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}
	
	public boolean collides(Ball b) {
		for (PhysicalFace f : faces) {
			if (f.collidesWithFace(b))
				return true;
		}
		return false;
	}

}
