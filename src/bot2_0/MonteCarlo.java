package bot2_0;

import entities.playable.Ball;
import entities.playable.RealBall;
import org.lwjgl.util.vector.Vector3f;
import physics.engine.PhysicsEngine;
import physics.utils.ShotData;
import terrains.World;

public class MonteCarlo {

    private static final int NR_SHOTS = 10;
    private static final float MAX_VELOCITY = 20;

    public Vector3f calculateShot(Ball b, World w) {
        ShotData curShotData;
        Vector3f initVelocity = new Vector3f(), bestVelocity = new Vector3f();
        float curDistanceSq, lowestDistanceSq = Float.MAX_VALUE;
        for (int i = 0; i < NR_SHOTS; i++) {
            initVelocity.set((float) (Math.random() * MAX_VELOCITY), 0, (float) (Math.random() * MAX_VELOCITY));
            curShotData = PhysicsEngine.getInstance().performVirtualShot((RealBall) b, initVelocity);
            curDistanceSq = (Vector3f.sub(w.getEnd(), curShotData.getEndPosition(), null)).lengthSquared();
            if (curDistanceSq < lowestDistanceSq) {
                lowestDistanceSq = curDistanceSq;
                bestVelocity.set(initVelocity.x, initVelocity.y, initVelocity.z);
            }
        }
        return bestVelocity;
    }

}
