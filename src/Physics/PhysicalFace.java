package Physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector3f;

import entities.Ball;
import toolbox.Douple;
import toolbox.Maths;

public class PhysicalFace {

	private Vector3f normal, point1, point2, point3, dist;
	private BoundingBox bbox;

	public PhysicalFace(Vector3f normal, Vector3f point1, Vector3f point2, Vector3f point3) {
		this.normal = new Vector3f(normal.x, normal.y, normal.z);
		this.normal.normalise();
		/*this.point1 = new Vector3f(point1.x, point1.y, point1.z);
		this.point2 = new Vector3f(point2.x, point2.y, point2.z);
		this.point3 = new Vector3f(point3.x, point3.y, point3.z);*/
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
		dist = new Vector3f();
		prepareBounds();
	}

	public boolean collidesWithFace(Ball b) {
		Vector3f closest = Maths.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		//System.out.println("Distance stuff/ballcenter: " + dist.length());
		if (dist.length() <= Ball.RADIUS)
			return true;
		return false;
	}
	
	public boolean collidesWithPlane(Ball b) {
		dist.set((point1.x - b.getPosition().x), (point1.y - b.getPosition().y), (point1.z - b.getPosition().z));
		double distanceSq = Math.pow(Vector3f.dot(normal, dist), 2)/normal.lengthSquared();
		if (distanceSq <= Ball.RADIUS * Ball.RADIUS)
			return true;
		return false;
	}
	
	public float distanceToFace(Ball b) {
		Vector3f closest = Maths.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.length();
	}
	
	public float distanceToFaceSq(Ball b) {
		Vector3f closest = Maths.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.lengthSquared();
	}
	
	public float distanceMidToFaceSq(Ball b) {
		Vector3f closest = Maths.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.lengthSquared();
	}

    public Vector3f getClosestPoint(Ball b) {
        return Maths.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
    }

	public Vector3f getNormal() {
		return normal;
	}

	public Vector3f getP1() {
		return point1;
	}

	public Vector3f getP2() {
		return point2;
	}

	public Vector3f getP3() {
		return point3;
	}

	private void prepareBounds() {
		float minX = Math.min(point1.x, Math.min(point2.x, point3.x));
		float minY = Math.min(point1.y, Math.min(point2.y, point3.y));
		float minZ = Math.min(point1.z, Math.min(point2.z, point3.z));
		float maxX = Math.max(point1.x, Math.max(point2.x, point3.x));
		float maxY = Math.max(point1.y, Math.max(point2.y, point3.y));
		float maxZ = Math.max(point1.z, Math.max(point2.z, point3.z));
		bbox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public Vector3f getCommonEdge(PhysicalFace f) {
        Vector3f c1, c2, edge = new Vector3f();

        if (this.point1 == f.getP1() || this.point1 == f.getP2() || this.point1 == f.getP3()) {
            c1 = this.point1;
        } else if (this.point2 == f.getP1() || this.point2 == f.getP2() || this.point2 == f.getP3()) {
            c1 = this.point2;
        } else if (this.point3 == f.getP1() || this.point3 == f.getP2() || this.point3 == f.getP3()) {
            c1 = this.point3;
        } else {
            return null;
        }

        if (this.point1 == f.getP1() || this.point1 == f.getP2() || this.point1 == f.getP3()) {
            c2 = this.point1;
        } else if (this.point2 == f.getP1() || this.point2 == f.getP2() || this.point2 == f.getP3()) {
            c2 = this.point2;
        } else if (this.point3 == f.getP1() || this.point3 == f.getP2() || this.point3 == f.getP3()) {
            c2 = this.point3;
        } else {
            return null;
        }

        Vector3f.sub(c1, c2, edge);
        return edge;
    }

    public Vector3f getCommonVertex(ArrayList<PhysicalFace> faces) {
        return null;
    }
	
	public String toString() {
		return this.getClass().getName() + " with normal (" + normal.x + "|" + normal.y + "|" + normal.z + ") with " + bbox;
	}

}
