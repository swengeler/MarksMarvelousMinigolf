import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import toolbox.Maths;

public class LinearAlgebraTests {

    public static final Vector3f X_AXIS = new Vector3f(1f, 0, 0);
    public static final Vector3f Y_AXIS = new Vector3f(0, 1f, 0);
    public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1f);

    public void convertingCoordinateSystem(Vector3f oldAxis, Vector3f newAxis) throws Exception {
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
        Matrix4f tfMatrix = Maths.createTransformationMatrix(null, (float) heading, (float) attitude, (float) bank, 1f);
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