import org.lwjgl.util.vector.Vector3f;

public class Main {

    public static void main(String[] args) throws Exception {
        // test for linear algebra
        System.out.println("LINEAR ALGEBRA TESTS:");
        LinearAlgebraTests linAlg = new LinearAlgebraTests();
        Vector3f test = new Vector3f(1, 1, 1);
        test.normalise();
        linAlg.convertingCoordinateSystem(LinearAlgebraTests.Y_AXIS, new Vector3f(0, 0, 1));
        System.out.println();
    }
}