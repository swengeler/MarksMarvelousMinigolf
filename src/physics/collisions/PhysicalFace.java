package physics.collisions;


import org.lwjgl.util.vector.Vector3f;

import entities.playable.Ball;
import toolbox.LinearAlgebra;

public class PhysicalFace {

	protected Vector3f normal, point1, point2, point3, dist;

	public PhysicalFace(Vector3f normal, Vector3f point1, Vector3f point2, Vector3f point3) {
		this.normal = new Vector3f(normal.x, normal.y, normal.z);
		this.normal.normalise();
		this.point1 = new Vector3f(point1.x, point1.y, point1.z);
		this.point2 = new Vector3f(point2.x, point2.y, point2.z);
		this.point3 = new Vector3f(point3.x, point3.y, point3.z);
		dist = new Vector3f();
	}

	public boolean collidesWithFace(Ball b) {
		Vector3f closest = LinearAlgebra.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		//System.out.println("Distance between ball and " + this + ": " + dist.length());
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
		Vector3f closest = LinearAlgebra.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.length();
	}
	
	public float distanceToFaceSq(Ball b) {
		Vector3f closest = LinearAlgebra.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.lengthSquared();
	}
	
	public float distanceMidToFaceSq(Ball b) {
		Vector3f closest = LinearAlgebra.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
		Vector3f.sub(b.getPosition(), closest, dist);
		return dist.lengthSquared();
	}

    public Vector3f getClosestPoint(Ball b) {
        return LinearAlgebra.closestPtPointTriangle(b.getPosition(), point1, point2, point3);
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

	public Vector3f getCommonEdge(PhysicalFace f) { // all of this equality stuff needs to be changed
        Vector3f c1, c2, edge = new Vector3f();

        if (LinearAlgebra.pointsAreEqual(point1, f.getP1()) || LinearAlgebra.pointsAreEqual(point1, f.getP2()) || LinearAlgebra.pointsAreEqual(point1, f.getP3())) {
            c1 = this.point1;
        } else if (LinearAlgebra.pointsAreEqual(point2, f.getP1()) || LinearAlgebra.pointsAreEqual(point2, f.getP2()) || LinearAlgebra.pointsAreEqual(point2, f.getP3())) {
            c1 = this.point2;
        } else if (LinearAlgebra.pointsAreEqual(point3, f.getP1()) || LinearAlgebra.pointsAreEqual(point3, f.getP2()) || LinearAlgebra.pointsAreEqual(point3, f.getP3())) {
            c1 = this.point3;
        } else {
            return null;
        }

        if (c1 != point1 && (LinearAlgebra.pointsAreEqual(point1, f.getP1()) || LinearAlgebra.pointsAreEqual(point1, f.getP2()) || LinearAlgebra.pointsAreEqual(point1, f.getP3()))) {
            c2 = this.point1;
        } else if (c1 != point2 && (LinearAlgebra.pointsAreEqual(point2, f.getP1()) || LinearAlgebra.pointsAreEqual(point2, f.getP2()) || LinearAlgebra.pointsAreEqual(point2, f.getP3()))) {
            c2 = this.point2;
        } else if (c1 != point3 && (LinearAlgebra.pointsAreEqual(point3, f.getP1()) || LinearAlgebra.pointsAreEqual(point3, f.getP2()) || LinearAlgebra.pointsAreEqual(point3, f.getP3()))) {
            c2 = this.point3;
        } else {
            return null;
        }

        Vector3f.sub(c1, c2, edge);
        return edge;
    }
	
	public String toString() {
		return "PhysicalFace with normal (" + normal.x + "|" + normal.y + "|" + normal.z + ") with points (" + point1.x + "|" + point1.y + "|" + point1.z + "), (" + point2.x + "|" + point2.y + "|" + point2.z + ") and (" + point3.x + "|" + point3.y + "|" + point3.z + ")";
	}

}
