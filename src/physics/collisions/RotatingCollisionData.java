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
        /*System.out.println("\nTRANSFORMATION MATRIX UPDATED IN CDATA:");
        System.out.println(tfMatrix.m00 + " | " + tfMatrix.m01 + " | " + tfMatrix.m02 + " | " + tfMatrix.m03 + "\n" +
                tfMatrix.m10 + " | " + tfMatrix.m11 + " | " + tfMatrix.m12 + " | " + tfMatrix.m13 + "\n" +
                tfMatrix.m20 + " | " + tfMatrix.m21 + " | " + tfMatrix.m22 + " | " + tfMatrix.m23 + "\n" +
                tfMatrix.m30 + " | " + tfMatrix.m31 + " | " + tfMatrix.m32 + " | " + tfMatrix.m33 + "\n");*/
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
