import org.lwjgl.util.vector.Vector3f;

public class Main {

    public static void main(String[] args) throws Exception {
        // test for linear algebra
        System.out.println("\nLINEAR ALGEBRA TESTS:");
        LinearAlgebraTests linAlg = new LinearAlgebraTests();
        Vector3f test = new Vector3f(1, 1, 1);
        test.normalise();
        //System.out.println(test);
        linAlg.convertingCoordinateSystemOther(LinearAlgebraTests.Y_AXIS, test);
        System.out.println();
    }
}