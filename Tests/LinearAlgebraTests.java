import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import toolbox.LinearAlgebra;

public class LinearAlgebraTests {

    public static final Vector3f X_AXIS = new Vector3f(1f, 0, 0);
    public static final Vector3f Y_AXIS = new Vector3f(0, 1f, 0);
    public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1f);

    public void convertingCoordinateSystemOther(Vector3f oldAxis, Vector3f newAxis) {
        System.out.println(oldAxis + " - " + newAxis);
        Vector3f rotAxis = Vector3f.cross(oldAxis, newAxis, null);
        rotAxis.normalise();
        float angle = Vector3f.angle(oldAxis, newAxis);
        System.out.println("Rotation axis: " + rotAxis + "\nAngle between the vectors: " + Math.toDegrees(angle));

        Matrix3f rodriguesRotMatrix = new Matrix3f();
        rodriguesRotMatrix.setIdentity();

        Matrix3f crossProductMatrix = new Matrix3f();
        crossProductMatrix.setZero();
        crossProductMatrix.m10 = -rotAxis.z;
        crossProductMatrix.m20 = rotAxis.y;
        crossProductMatrix.m01 = rotAxis.z;
        crossProductMatrix.m21 = -rotAxis.x;
        crossProductMatrix.m02 = -rotAxis.y;
        crossProductMatrix.m12 = rotAxis.x;

        System.out.println(crossProductMatrix);

        Matrix3f summandOne = new Matrix3f();
        summandOne.setZero();
        double factorOne = Math.sin(angle);
        System.out.println("Factor one: " + factorOne);
        summandOne.m10 = (float) (crossProductMatrix.m10 * factorOne);
        summandOne.m20 = (float) (crossProductMatrix.m20 * factorOne);
        summandOne.m01 = (float) (crossProductMatrix.m01 * factorOne);
        summandOne.m21 = (float) (crossProductMatrix.m21 * factorOne);
        summandOne.m02 = (float) (crossProductMatrix.m02 * factorOne);
        summandOne.m12 = (float) (crossProductMatrix.m12 * factorOne);

        Matrix3f summandTwo = new Matrix3f();
        summandTwo.setZero();
        double factorTwo = 1 - Math.cos(angle);
        System.out.println("Factor two: " + factorTwo + " " + Math.cos(angle));
        summandTwo.m10 = crossProductMatrix.m10;
        summandTwo.m20 = crossProductMatrix.m20;
        summandTwo.m01 = crossProductMatrix.m01;
        summandTwo.m21 = crossProductMatrix.m21;
        summandTwo.m02 = crossProductMatrix.m02;
        summandTwo.m12 = crossProductMatrix.m12;
        System.out.println("summandTwo 1:\n" + summandTwo);
        Matrix3f.mul(summandTwo, summandTwo, summandTwo);
        System.out.println("summandTwo 2:\n" + summandTwo);

        summandTwo.m00 *= factorTwo;
        summandTwo.m01 *= factorTwo;
        summandTwo.m02 *= factorTwo;
        summandTwo.m10 *= factorTwo;
        summandTwo.m11 *= factorTwo;
        summandTwo.m12 *= factorTwo;
        summandTwo.m20 *= factorTwo;
        summandTwo.m21 *= factorTwo;
        summandTwo.m22 *= factorTwo;

        System.out.println("summandTwo 3:\n" + summandTwo);

        Matrix3f.add(rodriguesRotMatrix, summandOne, rodriguesRotMatrix);
        Matrix3f.add(rodriguesRotMatrix, summandTwo, rodriguesRotMatrix);

        System.out.println(rodriguesRotMatrix);

        Vector3f tfVector = new Vector3f();
        tfVector.set(LinearAlgebraTests.X_AXIS.x, LinearAlgebraTests.X_AXIS.y, LinearAlgebraTests.X_AXIS.z);
        Matrix3f.transform(rodriguesRotMatrix, tfVector, tfVector);
        tfVector.normalise();
        System.out.println("Transformed x-axis: " + tfVector);

        tfVector.set(LinearAlgebraTests.Y_AXIS.x, LinearAlgebraTests.Y_AXIS.y, LinearAlgebraTests.Y_AXIS.z);
        Matrix3f.transform(rodriguesRotMatrix, tfVector, tfVector);
        tfVector.normalise();
        System.out.println("Transformed y-axis: " + tfVector);

        tfVector.set(LinearAlgebraTests.Z_AXIS.x, LinearAlgebraTests.Z_AXIS.y, LinearAlgebraTests.Z_AXIS.z);
        Matrix3f.transform(rodriguesRotMatrix, tfVector, tfVector);
        tfVector.normalise();
        System.out.println("Transformed z-axis: " + tfVector);
    }

    public void convertingCoordinateSystemEuler(Vector3f oldAxis, Vector3f newAxis) throws Exception {
        double heading, attitude, bank;
        Vector3f rotVector = Vector3f.cross(oldAxis, newAxis, null);
        double rotX = rotVector.x;
        double rotY = rotVector.y;
        double rotZ = rotVector.z;
        double angle = Math.toDegrees(Vector3f.angle(newAxis, oldAxis));

        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double t = 1 - c;

        double magnitude = Math.sqrt(rotX * rotX + rotY * rotY + rotZ * rotZ);
        if (magnitude == 0)
            throw new Exception();

        rotX /= magnitude;
        rotY /= magnitude;
        rotZ /= magnitude;

        if ((rotX * rotY * t + rotZ * s) > 0.998) { // north pole singularity detected
            heading = 2 * Math.atan2(rotX * Math.sin(angle / 2), Math.cos(angle / 2));
            attitude = Math.PI / 2;
            bank = 0;
            return;
        }
        if ((rotX * rotY * t + rotZ * s) < -0.998) { // south pole singularity detected
            heading = -2 * Math.atan2(rotX * Math.sin(angle / 2), Math.cos(angle / 2));
            attitude = -Math.PI / 2;
            bank = 0;
            return;
        } else {
            heading = Math.atan2(rotY * s - rotX * rotZ * t, 1 - (rotY * rotY + rotZ + rotZ) * t);
            attitude = Math.asin(rotX * rotY * t + rotZ * s) ;
            bank = Math.atan2(rotX * s - rotY * rotZ * t, 1 - (rotX * rotX + rotZ * rotZ) * t);
        }

        Vector3f euler = new Vector3f((float) heading, (float) attitude, (float) bank);
        Matrix4f tfMatrix = LinearAlgebra.createTransformationMatrix(null, (float) heading, (float) attitude, (float) bank, 1f);
        Vector4f tfVector = new Vector4f(0, 0, 0, 1f);
        System.out.println("Conversion matrix:\n" + tfMatrix);

        tfVector.set(LinearAlgebraTests.X_AXIS.x, LinearAlgebraTests.X_AXIS.y, LinearAlgebraTests.X_AXIS.z);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        System.out.printf("New x-axis: (%f|%f|%f)\n", tfVector.x, tfVector.y, tfVector.z);

        tfVector.set(LinearAlgebraTests.Y_AXIS.x, LinearAlgebraTests.Y_AXIS.y, LinearAlgebraTests.Y_AXIS.z);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        System.out.printf("New y-axis: (%f|%f|%f)\n", tfVector.x, tfVector.y, tfVector.z);

        tfVector.set(LinearAlgebraTests.Z_AXIS.x, LinearAlgebraTests.Z_AXIS.y, LinearAlgebraTests.Z_AXIS.z);
        Matrix4f.transform(tfMatrix, tfVector, tfVector);
        System.out.printf("New z-axis: (%f|%f|%f)\n", tfVector.x, tfVector.y, tfVector.z);
    }

}