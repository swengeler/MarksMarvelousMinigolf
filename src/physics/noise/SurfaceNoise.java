package physics.noise;

import org.lwjgl.util.vector.Vector3f;

public class SurfaceNoise {

    private static SurfaceNoise instance;

    private double probabiliy;

    public static SurfaceNoise getInstance(double probability) {
        if (instance == null)
            return (instance = new SurfaceNoise(probability));
        return instance;
    }

    private SurfaceNoise(double probability) {

    }

    public Vector3f updateAndGet(Vector3f ballVelocity) {
        if (Math.random() < probabiliy);
        return null;
    }

}
