package physics.noise;

public class SurfaceNoise {

    private static SurfaceNoise instance;

    public static SurfaceNoise getInstance() {
        if (instance == null)
            return (instance = new SurfaceNoise());
        return instance;
    }

    private SurfaceNoise() {

    }

}
