package physics.noise;

import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class Wind {

    private static Wind instance;

    public static final double MAX_XZ = 0.5;
    public static final double MAX_Y = 0.3;

    private float x, y, z;
    private long avDuration, avPauses;
    private long start, lastEnd;
    private double standardDeviation;
    private boolean active;

    private Random r = new Random();

    public static Wind getInstance(float x, float y, float z, long avDuration, long avPauses, double std) {
        if (instance == null)
            return (instance = new Wind(x, y, z, avDuration, avPauses, std));
        return instance;
    }

    private Wind(float x, float y, float z, long avDuration, long avPauses, double std) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.avDuration = avDuration;
        this.avPauses = avPauses;
        this.standardDeviation = std;
    }

    public Vector3f updateAndGet(Vector3f ballVelocity) {
        if (!active && (start == 0 || System.currentTimeMillis() - ((Math.random() * 0.7 * avPauses) + 0.6 * avPauses) <= lastEnd)) {
            start = System.currentTimeMillis();
            lastEnd = Long.MAX_VALUE;
            active = true;
        }
        if (System.currentTimeMillis() - ((Math.random() * 0.7 * avDuration) + 0.6 * avDuration) <= start) {
            System.out.println("Wind with avDuration = " + avDuration + " and avPauses = " + avPauses + " is applied at time " + System.currentTimeMillis() + ".");
            // should then return a vector that is roughly the same as baseDirection but has minor deviations in direction and magnitude
            double stdX = standardDeviation * x;
            double newX = r.nextGaussian() * stdX + x;
            ballVelocity.x += ((float) newX);

            double stdY = standardDeviation * y;
            double newY = r.nextGaussian() * stdY + y;
            ballVelocity.y += ((float) newY);

            double stdZ = standardDeviation * z;
            double newZ = r.nextGaussian() * stdZ + z;
            ballVelocity.z += ((float) newZ);
        } else {
            System.out.println("Wind stopped.");
            lastEnd = System.currentTimeMillis();
            active = false;
        }
        return ballVelocity;
    }

    public Vector3f getCurValue() {
        return new Vector3f(x, y, z);
    }

}
