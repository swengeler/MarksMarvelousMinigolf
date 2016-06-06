package Physics;

import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class Wind {

    private Vector3f baseDirection;
    private double duration;
    private double standardDeviation = 0.25;

    private Random r = new Random();

    public Wind(float x, float y, float z, double duration, double std) {
        this.baseDirection = new Vector3f(x, y, z);
        this.duration = duration;
        this.standardDeviation = std;
    }

    public boolean isActive() {
        return (duration > 0);
    }

    public void decreaseDuration(double delta) {
        duration -= delta;
    }

    public Vector3f getRandomWind(Vector3f w) {
        // should then return a vector that is roughly the same as baseDirection but has minor deviations in direction and magnitude
        double meanX = baseDirection.x;
        double stdX = standardDeviation * meanX;
        double newX = r.nextGaussian() * stdX + meanX;
        w.setX((float) newX);

        double meanY = baseDirection.y;
        double stdY = standardDeviation * meanX;
        double newY = r.nextGaussian() * stdY + meanY;
        w.setY((float) newY);

        double meanZ = baseDirection.z;
        double stdZ = standardDeviation * meanZ;
        double newZ = r.nextGaussian() * stdZ + meanZ;
        w.setZ((float) newZ);

        return w;
    }

    public Vector3f getRandomWind() {
        // should then return a vector that is roughly the same as baseDirection but has minor deviations in direction and magnitude
        return this.getRandomWind(new Vector3f());
    }

    public void setSTD(double std) {
        standardDeviation = std;
    }

}
