package physics.noise;

import org.lwjgl.util.vector.Vector3f;

public class NoiseHandler {

    private static NoiseHandler instance;

    public static final int OFF = -1;
    public static final int EASY = 0;
    public static final int MEDIUM = 1;
    public static final int HARD = 2;

    public static final int WIND = 0;
    public static final int FRICTION = 1;
    public static final int RESTITUTION = 2;
    public static final int SURFACE_NOISE = 3;

    private Wind wind;
    private Friction friction;
    private Restitution restitution;
    private SurfaceNoise surfaceNoise;

    public NoiseHandler(int difficulty, int... modes) {
        float x, y, z;
        for (int i : modes) {
            if (i == OFF) {
                //System.out.println("Noise off");
                wind = null;
                friction = null;
                restitution = null;
                surfaceNoise = null;
                return;
            } else if (i == WIND) {
                if (difficulty == EASY) {
                    x = (float) (Math.random() * 0.25 * Wind.MAX_XZ - 0.125 * Wind.MAX_XZ);
                    y = (float) (Math.random() * 0.25 * Wind.MAX_Y - 0.125 * Wind.MAX_Y);
                    z = (float) (Math.random() * 0.25 * Wind.MAX_XZ - 0.25 * Wind.MAX_XZ);
                    wind = Wind.getInstance(x, y, z, 500, 10000, 0);
                } else if (difficulty == MEDIUM) {
                    x = (float) (Math.random() * 0.5 * Wind.MAX_XZ - 0.25 * Wind.MAX_XZ);
                    y = (float) (Math.random() * 0.5 * Wind.MAX_Y - 0.25 * Wind.MAX_Y);
                    z = (float) (Math.random() * 0.5 * Wind.MAX_XZ - 0.25 * Wind.MAX_XZ);
                    wind = Wind.getInstance(x, y, z, 1000, 8000, 0);
                } else if (difficulty == HARD) {
                    x = (float) (Math.random() * 2 * Wind.MAX_XZ - Wind.MAX_XZ);
                    y = (float) (Math.random() * 2 * Wind.MAX_Y - Wind.MAX_Y);
                    z = (float) (Math.random() * 2 * Wind.MAX_XZ - Wind.MAX_XZ);
                    wind = Wind.getInstance(x, y, z, 2000, 5000, 0);
                }
            } else if (i == FRICTION) {
                if (difficulty == EASY) {
                    friction = Friction.getInstance(0.1f);
                } else if (difficulty == MEDIUM) {
                    friction = Friction.getInstance(0.25f);
                } else if (difficulty == HARD) {
                    friction = Friction.getInstance(0.4f);
                }
            } else if (i == RESTITUTION) {
                if (difficulty == EASY) {
                    restitution = Restitution.getInstance(0.1f);
                } else if (difficulty == MEDIUM) {
                    restitution = Restitution.getInstance(0.25f);
                } else if (difficulty == HARD) {
                    restitution = Restitution.getInstance(0.4f);
                }
            } else if (i == SURFACE_NOISE) {
                if (difficulty == EASY) {
                    surfaceNoise = SurfaceNoise.getInstance(0.1);
                } else if (difficulty == MEDIUM) {
                    surfaceNoise = SurfaceNoise.getInstance(0.2);
                } else if (difficulty == HARD) {
                    surfaceNoise = SurfaceNoise.getInstance(0.3);
                }
            }
        }
    }

    public NoiseHandler(Wind wind, Friction friction, Restitution restitution, SurfaceNoise surfaceNoise) {
        this.wind = wind;
        this.friction = friction;
        this.restitution = restitution;
        this.surfaceNoise = surfaceNoise;
    }

    public NoiseHandler() {
        wind = Wind.getInstance(0, 0, 0, 0, 0, 0);
        friction = Friction.getInstance(0);
        restitution = Restitution.getInstance(0);
        surfaceNoise = SurfaceNoise.getInstance(0);
    }

    public float getFrictionNoise() {
        if (friction == null)
            return Friction.COEFFICIENT;
        return friction.updateAndGet();
    }

    public float getRestitutionNoise() {
        if (restitution == null)
            return Restitution.COEFFICIENT;
        return restitution.updateAndGet();
    }

    public Vector3f applyWind(Vector3f ballVelocity) {
        if (wind == null)
            return ballVelocity;
        return wind.updateAndGet(ballVelocity);
    }

    public Vector3f applySurfaceNoise(Vector3f ballVelocity) {
        if (surfaceNoise == null)
            return ballVelocity;
        return null;
    }

}
