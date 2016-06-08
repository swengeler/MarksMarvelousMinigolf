package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import Physics.PhysicalFace;
import entities.Camera;

public class Maths {

    private static final float EQUALITY_TOLERANCE = 0.01f;

    public static boolean pointsAreEqual(Vector3f p1, Vector3f p2) {
        return  Math.abs(p1.x - p2.x) < EQUALITY_TOLERANCE &&
                Math.abs(p1.y - p2.y) < EQUALITY_TOLERANCE &&
                Math.abs(p1.z - p2.z) < EQUALITY_TOLERANCE;
    }

	public static Vector3f closestPtPointTriangle(Vector3f pOr, Vector3f aOr, Vector3f bOr, Vector3f cOr) {
		// to make sure that the actual points aren't changed by the operations below
		Vector3f p = new Vector3f(pOr.x, pOr.y, pOr.z);
		Vector3f a = new Vector3f(aOr.x, aOr.y, aOr.z);
		Vector3f b = new Vector3f(bOr.x, bOr.y, bOr.z);
		Vector3f c = new Vector3f(cOr.x, cOr.y, cOr.z);

        // check if p is in the vertex region outside a
        Vector3f ab = new Vector3f(), ac = new Vector3f(), ap = new Vector3f();
        Vector3f.sub(b, a, ab);
        Vector3f.sub(c, a, ac);
        Vector3f.sub(p, a, ap);
        float d1 = Vector3f.dot(ab, ap);
        float d2 = Vector3f.dot(ac, ap);
        if (d1 <= 0 && d2 <= 0) {
            //System.out.printf("First point returned: (%f|%f|%f)\n", aOr.x, aOr.y, aOr.z);
            return aOr;
        }

        // check if p is in the vertex region outside of b
        Vector3f bp = new Vector3f();
        Vector3f.sub(p, b, bp);
        float d3 = Vector3f.dot(ab, bp);
        float d4 = Vector3f.dot(ac, bp);
        if (d3 >= 0 && d4 <= d3) {
            //System.out.printf("Second point returned: (%f|%f|%f)\n", bOr.x, bOr.y, bOr.z);
            return bOr;
        }

        // check if p is in the vertex region outside c
        Vector3f cp = new Vector3f();
        Vector3f.sub(p, c, cp);
        float d5 = Vector3f.dot(ab, cp);
        float d6 = Vector3f.dot(ac, cp);
        if (d6 >= 0 && d5 <= d6) {
            //System.out.printf("Third point returned: (%f|%f|%f)\n", cOr.x, cOr.y, cOr.z);
            return cOr;
        }

        // check if p is in the edge region of ab
        float vc = d1 * d4 - d3 * d2;
        if (vc <= 0 && d1 >= 0 && d3 <= 0) {
            float v = d1 / (d1 - d3);
            Vector3f temp = new Vector3f();
            Vector3f.add(a, (Vector3f) ab.scale(v), temp);
            //System.out.printf("Point on edge (first - second) returned: (%f|%f|%f)\n", temp.x, temp.y, temp.z);
            return temp;
        }

        // check if p is in the edge region of ac
        float vb = d5 * d2 - d1 * d6;
        if (vb <= 0 && d2 >= 0 && d6 <= 0) {
            float w = d2 / (d2 - d6);
            Vector3f temp = new Vector3f();
            Vector3f.add(a, (Vector3f) ac.scale(w), temp);
            //System.out.printf("Point on edge (first - third) returned: (%f|%f|%f)\n", temp.x, temp.y, temp.z);
            return temp;
        }

        // check if p is in the edge region of bc
        float va = d3 * d6 - d5 * d4;
        if (va <= 0 & (d4 - d3) >= 0 && (d5 - d6) >= 0) {
            float w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            Vector3f temp1 = new Vector3f();
            Vector3f temp2 = new Vector3f();
            Vector3f.sub(c, b, temp1);
            Vector3f.add(b, (Vector3f) temp1.scale(w), temp2);
            //System.out.printf("Point on edge (second - third) returned: (%f|%f|%f)\n", temp2.x, temp2.y, temp2.z);
            return temp2;
        }

        // p must be inside the face region
        float denom = 1 / (va + vb + vc);
        float v = vb * denom;
        float w = vc * denom;
        Vector3f temp1 = new Vector3f();
        Vector3f temp2 = new Vector3f();
        Vector3f.add((Vector3f) ab.scale(v), (Vector3f) ac.scale(w), temp1);
        Vector3f.add(a, temp1, temp2);
        //System.out.printf("Point in face returned: (%f|%f|%f)\n", temp2.x, temp2.y, temp2.z);
        return temp2;
	}
	
	public static boolean checkPointInFace(Vector3f p, PhysicalFace f) {
		Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f();
		
		// translate point and triangle so that point lies at origin
		Vector3f.sub(f.getP1(), p, a);
		Vector3f.sub(f.getP2(), p, b);
		Vector3f.sub(f.getP3(), p, c);
		p.set(0, 0, 0);
		
		float ab = Vector3f.dot(a, b);
		float ac = Vector3f.dot(a, c);
		float bc = Vector3f.dot(b, c);
		float cc = Vector3f.dot(c, c);
		
		// make sure plane normals for pab and pbc point in the same direction
		if ((bc * ac) - (cc * ab) < 0)
			return false;
		
		// make sure plane normals for pab and pca point in the same direction
		float bb = Vector3f.dot(b, b);
		if ((ab * bc) - (ac * bb) < 0)
			return false;
		
		// otherwise p must lie on the triangle
		return true;
		
	}

	public static float distancePtPtSq(Vector3f p1, Vector3f p2) {
		Vector3f dist = new Vector3f();
		Vector3f.sub(p1, p2, dist);
		return dist.lengthSquared();
	}
	
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(Camera camera){
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}
	
}
