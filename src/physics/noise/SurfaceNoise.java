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
        this.probabiliy = probability;
    }

    public Vector3f updateAndGet(Vector3f ballVelocity) {
        if (Math.random() < probabiliy) {
            double rand = Math.random();
            if (rand < 1 / 3) {
                ballVelocity.x += Math.random() * 0.5 - 0.5;
            } else if (rand < 1 / 6) {
                ballVelocity.y += Math.random() * 0.25;
            } else {
                ballVelocity.z += Math.random() * 0.5 - 0.5;
            }
        }
        return null;
    }

}
