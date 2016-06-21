package artificialIntelligence.algorithms;

import entities.playable.Ball;
import entities.playable.RealBall;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import physics.engine.PhysicsEngine;
import physics.noise.Friction;
import physics.utils.ShotData;
import terrains.World;

public class MonteCarlo {

    private static final int NR_SHOTS = 20;
    private static final float MAX_VELOCITY = 50;

    public Vector3f calculateShot(Ball b, World w) {
        ShotData curShotData;
        Vector3f initVelocity = new Vector3f(), bestVelocity = new Vector3f();
        float curDistanceSq, lowestDistanceSq = Float.MAX_VALUE;
        for (int i = 0; i < NR_SHOTS; i++) {
            //straightShotNonRandom(initVelocity, w.getEnd(), b.getPosition());
            //randomMagnitude(initVelocity, i);
            onlyRandom(initVelocity);
            onlyRandom(initVelocity);
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

    private Vector3f straightShot(Vector3f velocity, Vector3f holePosition, Vector3f ballPosition) {
        Vector3f temp = Vector3f.sub(holePosition, ballPosition, null);
        float length = temp.length();
        temp.normalise();
        temp.scale((float) (Math.random() * 2 * length + length));
        velocity.set(temp);
        return velocity;
    }

    private Vector3f straightShotNonRandom(Vector3f velocity, Vector3f holePosition, Vector3f ballPosition) {
        Vector3f temp = Vector3f.sub(holePosition, ballPosition, null);
        float finalMagnitude = (float) Math.sqrt(2 * Friction.COEFFICIENT * 230 * temp.length());
        temp.normalise();
        temp.scale(finalMagnitude);
        velocity.set(temp);
        return velocity;
    }

}
