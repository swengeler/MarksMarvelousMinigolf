package physics.noise;

import java.util.Random;

public class Restitution {

    public static final float COEFFICIENT = 0.75f;

    private static Restitution instance;

    private float standardDeviation;
    private Random r = new Random();

    private float curValue;

    public static final Restitution getInstance(float standardDeviation) {
        if (instance == null)
            return (instance = new Restitution(standardDeviation));
        return instance;
    }

    private Restitution(float standardDeviation) {
        this.standardDeviation = standardDeviation;
        this.r = new Random();
    }

    public float updateAndGet() {
        double std;
        if ((std = standardDeviation * COEFFICIENT) > 1)
            std = 1;
        curValue = (float) (r.nextGaussian() * std + COEFFICIENT);
        return curValue;
    }

    public float getCurValue() {
        return curValue;
    }

}
