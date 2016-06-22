package physics.collisions;

import entities.playable.Ball;
import org.lwjgl.util.vector.Matrix4f;

import java.util.ArrayList;

public class RotatingCollisionData extends CollisionData {

    private Matrix4f tfMatrix, invTFMatrix;

    public RotatingCollisionData(Matrix4f invTFMatrix) {
        super();
        this.invTFMatrix = invTFMatrix;
    }

    @Override
    public ArrayList<Face> getCollidingFaces(Ball b) {
        collisionList.clear();
        for (Face f : faces) {
            ((RotatingFace) f).updateFace(tfMatrix, invTFMatrix);
            if (f.collidesWithFace(b))
                collisionList.add(f);
        }
        return collisionList;
    }

    @Override
    public boolean collides(Ball b) {
        for (Face f : faces) {
            ((RotatingFace) f).updateFace(tfMatrix, invTFMatrix);
            if (f.collidesWithFace(b))
                return true;
        }
        return false;
    }

    public void updateTFMatrix(Matrix4f tfMatrix) {
        this.tfMatrix = tfMatrix;
    }

}
