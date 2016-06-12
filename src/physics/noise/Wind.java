package physics.noise;

import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class Wind {

    private static Wind instance;

    private float x, y, z;
    private double duration;
    private double standardDeviation;

    private Random r = new Random();

    public static Wind getInstance(float x, float y, float z, double duration, double std) {
        if (instance == null)
            return (instance = new Wind(x, y, z, duration, std));
        return instance;
    }

    private Wind(float x, float y, float z, double duration, double std) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.duration = duration;
        this.standardDeviation = std;
    }

    public Vector3f updateAndGet(Vector3f ballVelocity) {
        // should then return a vector that is roughly the same as baseDirection but has minor deviations in direction and magnitude
        double stdX = standardDeviation * x;
        double newX = r.nextGaussian() * stdX + x;
        ballVelocity.x += ((float) newX);

        double stdY = standardDeviation * y;
        double newY = r.nextGaussian() * stdY + y;
        ballVelocity.y  += ((float) newY);

        double stdZ = standardDeviation * z;
        double newZ = r.nextGaussian() * stdZ + z;
        ballVelocity.z += ((float) newZ);

        return ballVelocity;
    }

    public Vector3f getCurValue() {
        return new Vector3f(x, y, z);
    }

}
