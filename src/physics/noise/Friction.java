package physics.noise;

import java.util.Random;

public class Friction {

    public static final float COEFFICIENT = 0.15f;

    private static Friction instance;

    private float standardDeviation;
    private Random r = new Random();

    private float curValue;

    public static final Friction getInstance(float standardDeviation) {
        if (instance == null)
            return (instance = new Friction(standardDeviation));
        return instance;
    }

    private Friction(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public float updateAndGet() {
        double std;
        if ((std = standardDeviation * COEFFICIENT) < 0)
            std = 0;
        curValue = (float) (r.nextGaussian() * std + COEFFICIENT);
        return curValue;
    }

    public float getCurValue() {
        return curValue;
    }

}
