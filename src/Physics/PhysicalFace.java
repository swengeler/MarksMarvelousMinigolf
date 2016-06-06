package Physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.Ball;
import toolbox.Maths;

public class PhysicalFace {

	private Vector3f normal, point1, point2, point3, dist;
	private BoundingBox bbox;

	public PhysicalFace(Vector3f normal, Vector3f point1, Vector3f point2, Vector3f point3) {
		this.normal = new Vector3f(normal.x, normal.y, normal.z);
		this.normal.normalise();
		this.point1 = new Vector3f(point1.x, point1.y, point1.z);
		this.point2 = new Vector3f(point2.x, point2.y, point2.z);
		this.point3 = new Vector3f(point3.x, point3.y, point3.z);
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

	public static PhysicalFace combineFaces(ArrayList<PhysicalFace> faces, Ball b) {
		if (faces.size() == 1) {
			System.out.println("COLLISION WITH ONE PLANE");
			return faces.get(0);
		} else if (faces.size() == 2) {
			PhysicalFace f1 = faces.get(0), f2 = faces.get(1);
			Vector3f v1 = new Vector3f(), v2 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f(), temp = new Vector3f();

			// getting the first vector out of the cross product of both normal vectors (parallel to the line of intersection of both planes)
			Vector3f.cross(f1.getNormal(), f2.getNormal(), v1);

			// getting the first point needed for the second vector in the first face/plane
			Vector3f.sub(b.getPosition(), f1.getP1(), temp);
			float signedDistance = Vector3f.dot(temp, f1.getNormal())/f1.getNormal().length();
			temp.set(f1.getNormal().x, f1.getNormal().y, f1.getNormal().z);
			temp.scale(signedDistance);
			Vector3f.add(b.getPosition(), temp, p1);

			// getting the second point needed for the second vector in the second face/plane
			Vector3f.sub(b.getPosition(), f2.getP1(), temp);
			signedDistance = Vector3f.dot(temp, f2.getNormal())/f2.getNormal().length();
			temp.set(f2.getNormal().x, f2.getNormal().y, f2.getNormal().z);
			temp.scale(signedDistance);
			Vector3f.add(b.getPosition(), temp, p2);

			// getting the second vector out of difference between the calculated points p1 and p2 and then constructing the face
			Vector3f.sub(p1, p2, v2);
			Vector3f.cross(v1, v2, temp);
			System.out.println("COLLISION WITH TWO PLANES");
			System.out.printf("Vector 1: (%f|%f|%f)\n", v1.x, v1.y, v1.z);
			System.out.printf("Vector 2: (%f|%f|%f)\n", v2.x, v2.y, v2.z);
			System.out.printf("Normal vector out of two planes: (%f|%f|%f)\n", temp.x, temp.y, temp.z);
			return new PhysicalFace(temp, p1, p1, p2);
		}
		System.out.println("COLLISION WITH THREE PLANES");
		PhysicalFace f1 = faces.get(0), f2 = faces.get(1), f3 = faces.get(2);
		Vector3f v1 = new Vector3f(), v2 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), temp = new Vector3f();

		// getting the first point needed for both vectors in the first face/plane
		Vector3f.sub(b.getPosition(), f1.getP1(), temp);
		float signedDistance = Vector3f.dot(temp, f1.getNormal())/f1.getNormal().length();
		temp.set(f1.getNormal().x, f1.getNormal().y, f1.getNormal().z);
		temp.scale(signedDistance);
		Vector3f.add(b.getPosition(), temp, p1);

		// getting the second point needed for both vectors in the second face/plane
		Vector3f.sub(b.getPosition(), f2.getP1(), temp);
		signedDistance = Vector3f.dot(temp, f2.getNormal())/f2.getNormal().length();
		temp.set(f2.getNormal().x, f2.getNormal().y, f2.getNormal().z);
		temp.scale(signedDistance);
		Vector3f.add(b.getPosition(), temp, p2);

		// getting the third point needed for both vectors in the third face/plane
		Vector3f.sub(b.getPosition(), f3.getP1(), temp);
		signedDistance = Vector3f.dot(temp, f3.getNormal())/f3.getNormal().length();
		temp.set(f3.getNormal().x, f3.getNormal().y, f3.getNormal().z);
		temp.scale(signedDistance);
		Vector3f.add(b.getPosition(), temp, p3);

		// constructing the two vectors lying in the plane, calculating the normal vector and creating the new face
		Vector3f.sub(p2, p1, v1);
		Vector3f.sub(p3, p1, v2);
		Vector3f.cross(v1, v2, temp);
		return new PhysicalFace(temp, p1, p2, p3);
	}
	
	public String toString() {
		return this.getClass().getName() + " with normal (" + normal.x + "|" + normal.y + "|" + normal.z + ") with " + bbox;
	}

}
