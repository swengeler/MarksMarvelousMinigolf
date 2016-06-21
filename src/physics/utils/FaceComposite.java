package physics.utils;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.playable.Ball;
import physics.collisions.Face;
import physics.engine.PhysicsEngine;
import toolbox.LinearAlgebra;

public class FaceComposite {

    private ArrayList<Face> faces;

    public FaceComposite(ArrayList<Face> fcs) {
        // the first face from the array list defines the normal vector that all others must have
    	faces = new ArrayList<Face>();
        faces.add(fcs.get(0));
        for (int i = 1; i < fcs.size(); i++) {
        	
        }
    }

    public FaceComposite(Face f) {
        // the first face from the array list defines the normal vector that all others must have
    	faces = new ArrayList<Face>();
        faces.add(f);
    }

    public boolean canAddFace(Face f) {
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

    public void add(Face f) {
        faces.add(f);
    }

    public void remove(Face f) {
        faces.remove(f);
    }

    public Face get(int i) {
        return faces.get(i);
    }

    public boolean pointInComposite(Vector3f p) {
        for (Face f : faces) {
            if (LinearAlgebra.checkPointInFace(p, f))
                return true;
        }
        return false;
    }

    public boolean collidesWith(Ball b) {
        for (Face f : faces) {
            if (f.collidesWithFace(b))
                return true;
        }
        return false;
    }

    public Vector3f getNormal() {
        return faces.get(0).getNormal();
    }

    public Face getPlane() {
        return faces.get(0);
    }

}
