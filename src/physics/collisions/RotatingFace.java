package physics.collisions;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RotatingFace extends PhysicalFace {

    protected Vector3f orNormal, orP1, orP2, orP3;
    protected Vector4f tfVector;

    public RotatingFace(Vector3f normal, Vector3f point1, Vector3f point2, Vector3f point3) {
        super(normal, point1, point2, point3);
        //System.out.printf("Face initialised in super with (%f|%f|%f) - (%f|%f|%f) - (%f|%f|%f)\n", this.point1.x, this.point1.y, this.point1.z, this.point2.x, this.point2.y, this.point2.z, this.point3.x, this.point3.y, this.point3.z);
        orNormal = new Vector3f(normal.x, normal.y, normal.z);
        orNormal.normalise();
        orP1 = new Vector3f(point1.x, point1.y, point1.z);
        orP2 = new Vector3f(point2.x, point2.y, point2.z);
        orP3 = new Vector3f(point3.x, point3.y, point3.z);
        tfVector = new Vector4f(0, 0, 0, 1f);
        //System.out.printf("\nFace initialised with (%f|%f|%f) - (%f|%f|%f) - (%f|%f|%f)\n", orP1.x, orP1.y, orP1.z, orP2.x, orP2.y, orP2.z, orP3.x, orP3.y, orP3.z);
    }

    protected void updateFace(Matrix4f tfMatrix, Matrix4f invTFMatrix) {
        // transforming the current points of the face to their actual position in the movable model
        tfVector.set(orNormal.x, orNormal.y, orNormal.z, 1f);
        Matrix4f.transform(invTFMatrix, tfVector, tfVector);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        normal.set(tfVector.x, tfVector.y, tfVector.z);
        normal.normalise();

        tfVector.set(orP1.x, orP1.y, orP1.z, 1f);
        Matrix4f.transform(invTFMatrix, tfVector, tfVector);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        point1.set(tfVector.x, tfVector.y, tfVector.z);

        tfVector.set(orP2.x, orP2.y, orP2.z, 1f);
        Matrix4f.transform(invTFMatrix, tfVector, tfVector);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        point2.set(tfVector.x, tfVector.y, tfVector.z);

        tfVector.set(orP3.x, orP3.y, orP3.z, 1f);
        Matrix4f.transform(invTFMatrix, tfVector, tfVector);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        point3.set(tfVector.x, tfVector.y, tfVector.z);

        //System.out.printf("Face updated to (%f|%f|%f) - (%f|%f|%f) - (%f|%f|%f)\n", this.point1.x, this.point1.y, this.point1.z, this.point2.x, this.point2.y, this.point2.z, this.point3.x, this.point3.y, this.point3.z);
    }

}
