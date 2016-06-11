package physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.playable.Ball;
import physics.collisions.PhysicalFace;
import toolbox.LinearAlgebra;

public class FaceComposite {

    private ArrayList<PhysicalFace> faces;

    public FaceComposite(ArrayList<PhysicalFace> fcs) {
        // the first face from the array list defines the normal vector that all others must have
    	faces = new ArrayList<PhysicalFace>();
        faces.add(fcs.get(0));
        for (int i = 1; i < fcs.size(); i++) {
        	
        }
    }

    public FaceComposite(PhysicalFace f) {
        // the first face from the array list defines the normal vector that all others must have
    	faces = new ArrayList<PhysicalFace>();
        faces.add(f);
    }

    public boolean canAddFace(PhysicalFace f) {
        if (((Math.abs(f.getNormal().x - faces.get(0).getNormal().x) < PhysicsEngine.NORMAL_TH &&
            Math.abs(f.getNormal().y - faces.get(0).getNormal().y) < PhysicsEngine.NORMAL_TH &&
            Math.abs(f.getNormal().z - faces.get(0).getNormal().z) < PhysicsEngine.NORMAL_TH)||
    		(Math.abs(-f.getNormal().x - faces.get(0).getNormal().x) < PhysicsEngine.NORMAL_TH &&
            Math.abs(-f.getNormal().y - faces.get(0).getNormal().y) < PhysicsEngine.NORMAL_TH &&
            Math.abs(-f.getNormal().z - faces.get(0).getNormal().z) < PhysicsEngine.NORMAL_TH)) &&
            Math.abs(Vector3f.dot(faces.get(0).getNormal(), f.getP1()) - Vector3f.dot(faces.get(0).getNormal(), faces.get(0).getP1())) < 0.01) {
            return true;
        }
        System.out.printf("New face is not parallel to face composite defined by: (%f|%f|%f)\n", faces.get(0).getNormal().x, faces.get(0).getNormal().y, faces.get(0).getNormal().z);
        return false;
    }

    public void add(PhysicalFace f) {
        faces.add(f);
    }

    public void remove(PhysicalFace f) {
        faces.remove(f);
    }

    public PhysicalFace get(int i) {
        return faces.get(i);
    }

    public boolean pointInComposite(Vector3f p) {
        for (PhysicalFace f : faces) {
            if (LinearAlgebra.checkPointInFace(p, f))
                return true;
        }
        return false;
    }

    public boolean collidesWith(Ball b) {
        for (PhysicalFace f : faces) {
            if (f.collidesWithFace(b))
                return true;
        }
        return false;
    }

    public Vector3f getNormal() {
        return faces.get(0).getNormal();
    }

    public PhysicalFace getPlane() {
        return faces.get(0);
    }

}
