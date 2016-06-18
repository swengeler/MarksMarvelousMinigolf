package bot2_0;

import entities.playable.Ball;
import entities.playable.RealBall;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import physics.engine.PhysicsEngine;
import physics.utils.ShotData;
import terrains.World;

public class MonteCarlo {

    private static final int NR_SHOTS = 10;
    private static final float MAX_VELOCITY = 50;

    public Vector3f calculateShot(Ball b, World w) {
        ShotData curShotData;
        Vector3f initVelocity = new Vector3f(), bestVelocity = new Vector3f();
        float curDistanceSq, lowestDistanceSq = Float.MAX_VALUE;
        for (int i = 0; i < NR_SHOTS; i++) {
            randomMagnitude(initVelocity, i);
            curShotData = PhysicsEngine.getInstance().performVirtualShot((RealBall) b, initVelocity);
            curDistanceSq = (Vector3f.sub(w.getEnd(), curShotData.getEndPosition(), null)).lengthSquared();
            if (curDistanceSq < lowestDistanceSq) {
                lowestDistanceSq = curDistanceSq;
                bestVelocity.set(initVelocity.x, initVelocity.y, initVelocity.z);
            }
        }
        return bestVelocity;
    }

    private Vector3f onlyRandom(Vector3f velocity) {
        velocity.set((float) (Math.random() * MAX_VELOCITY), 0, (float) (Math.random() * MAX_VELOCITY));
        return velocity;
    }

    private Vector3f randomMagnitude(Vector3f velocity, int step) {
        Matrix4f rotMatrix = new Matrix4f();
        Vector4f rotVector = new Vector4f(0, 0, 0, 1);
        if (step == 0) {
            velocity.set((float) (Math.random() * MAX_VELOCITY), 0, (float) (Math.random() * MAX_VELOCITY));
            System.out.println("Velocity set initially to: " + velocity);
        } else {
            rotMatrix.rotate((float) (Math.PI / NR_SHOTS) * step, new Vector3f(0, 1, 0));
            rotVector.set(velocity.x, velocity.y, velocity.z);
            Matrix4f.transform(rotMatrix, rotVector, rotVector);
            velocity.set(rotVector.x, rotVector.y, rotVector.z);
            System.out.println("Velocity set to: " + velocity);
        }
        return velocity;
    }

}
