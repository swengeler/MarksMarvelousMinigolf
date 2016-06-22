package physics.collisions;

import java.io.Serializable;
import java.util.ArrayList;

import entities.playable.Ball;
import org.lwjgl.util.vector.Vector3f;

public class CollisionData implements Serializable{

	private BoundingBox bbox;
	protected ArrayList<Face> faces;
	protected ArrayList<Face> collisionList;

	public CollisionData() {
		faces = new ArrayList<>();
		collisionList = new ArrayList<>();
	}

	public void addFace(Face face) {
		faces.add(face);
	}

	public void setBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		bbox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BoundingBox getBoundingBox() {
		return bbox;
	}

	public ArrayList<Face> getCollidingFaces(Ball b) {
		collisionList.clear();
		for (Face f : faces) {
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
		for (Face f : faces) {
			a.set(f.getP1());
			b.set(f.getP2());
			c.set(f.getP3());

			u = Vector3f.dot(pq, Vector3f.cross(c, b, temp)) + Vector3f.dot(m, Vector3f.sub(c, b, temp));
			v = Vector3f.dot(pq, Vector3f.cross(a, c, temp)) + Vector3f.dot(m, Vector3f.sub(a, c, temp));
			w = Vector3f.dot(pq, Vector3f.cross(b, a, temp)) + Vector3f.dot(m, Vector3f.sub(b, a, temp));

			if ((u * v) > 0 && (u * w) > 0) {
				denom = 1 / (u + v + w);
				u *= denom;
				v *= denom;
				w *= denom;

				curHeight = a.y * u + b.y * v + c.y * w;

				if (curHeight > maxHeight) {
					maxHeight = curHeight;
				}
			}
		}
		return maxHeight;
	}
	
	public boolean isIntersectedBySegment(Vector3f p1, Vector3f p2) {
	    if (bbox.isIntersectedBySegment(p1, p2)) {
	        for (Face f : faces) {
	            if (f.isIntersectedBySegment(p1, p2)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}

	public ArrayList<Vector3f> getIntersectionPointsSegment(Vector3f p1, Vector3f p2) {
		ArrayList<Vector3f> intersectionPoints = new ArrayList<>();
		Vector3f temp;
		for (Face f : faces) {
			temp = f.getIntersectionPointSegment(p1, p2);
			if (temp != null)
				intersectionPoints.add(new Vector3f(temp));
		}
		return intersectionPoints;
	}
	
	public boolean collides(Ball b) {
		for (Face f : faces) {
			if (f.collidesWithFace(b))
				return true;
		}
		return false;
	}

}
