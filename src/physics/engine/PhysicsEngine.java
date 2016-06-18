package physics.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import bot2_0.AIShot;
import bot2_0.Algorithm;
import bot2_0.Node;
import gameEngine.MainGameLoop;
import entities.playable.RealBall;
import entities.playable.VirtualBall;
import entities.playable.Ball;
import entities.obstacles.Entity;
import physics.noise.NoiseHandler;
import physics.utils.ShotData;
import physics.collisions.PhysicalFace;
import programStates.GameState;
import renderEngine.utils.DisplayManager;
import terrains.Terrain;
import terrains.World;
import toolbox.LinearAlgebra;


public class PhysicsEngine {
    float count=0;
    public static final float NORMAL_TH = 0.001f;
    private static final float ANGLE_TH = 5f;
    private static final float C = 0.001f;
    public static final float MIN_MOV_REQ = 0.000f;

    public static final float REAL_GRAVITY = 9.813f;

    public static final Vector3f GRAVITY = new Vector3f(0, -(REAL_GRAVITY / Ball.REAL_RADIUS), 0);
    public static final float COEFF_RESTITUTION = 0.75f;
    public static final float COEFF_FRICTION = 0.15f;

    private static final double FRICTION_STD = 0.5;

    private List<RealBall> balls;
    private World world;
    private NoiseHandler noiseHandler;

    private boolean enabled;
    private final float minenergy = 1;
    
    private static PhysicsEngine instance;

    public PhysicsEngine(List<Ball> balls, World world, NoiseHandler noiseHandler) {
        this.balls = new ArrayList<RealBall>();
        for (Ball b : balls)
            if (b instanceof RealBall)
                this.balls.add((RealBall) b);
        this.world = world;
       if (noiseHandler == null)
            this.noiseHandler = new NoiseHandler(NoiseHandler.HARD, NoiseHandler.OFF);
        else
            this.noiseHandler = noiseHandler;
        this.enabled = true;
        instance = this;
    }
    
    public static PhysicsEngine getInstance(List<Ball> balls, World world, NoiseHandler noiseHandler){
    	if (instance == null) {
            return (instance = new PhysicsEngine(balls, world, noiseHandler));
        }
        return instance;
    }

    public static PhysicsEngine getInstance() {
        return instance;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addBall(RealBall ball) {
        this.balls.add(ball);
    }

    public void tick() {
        for (RealBall b : balls) {
            b.applyAccel(GRAVITY);
            if (b.isMoving()) {
                System.out.println("Since the ball moved wind is applied");
                noiseHandler.applyWind(b.getVelocity());
            }
            if ((b.isMoving() && (b.movedLastStep() || b.getLastTimeElapsed() == 0)) || MainGameLoop.getCounter() < 10) {
                b.updateAndMove();
                System.out.println("\n---- Collision detection starts ----\n");
                resolveBallCollision(b);
                // it's possible that the ball collides with both obstacles and terrain at the same time
                // in that case only the obstacle collision is resolved to prevent strange behaviour based on the resulting velocity of the first collision
                if (!resolveObstacleCollision(b))
                    resolveTerrainCollision(b);
                System.out.println("\n---- Collision detection ends ----\n");
            } else {
                b.setVelocity(0, 0, 0);
                b.setMoving(false);
            }
        }
    }

    public void resolveTerrainCollision(Ball b) {
        /*if (((Vector3f) Vector3f.sub(b.getPosition(), new Vector3f(world.getEnd().x, b.getPosition().y, world.getEnd().z), null)).lengthSquared() < 100f) {
            return;
        }*/

        if (world.getHeightOfTerrain(b.getPosition().x, b.getPosition().z) > b.getPosition().y - Ball.RADIUS) {
            b.setPosition(new Vector3f(b.getPosition().x, world.getHeightOfTerrain(b.getPosition().x, b.getPosition().z) + Ball.RADIUS, b.getPosition().z));

            // calculate the closest face/plane after the ball was pushed out, which is then used for collision resolution
            Terrain t = world.getTerrain(b.getPosition().x, b.getPosition().z);
            if (t == null) {
                setEnabled(false);
                return;
            }
            float terrainX = b.getPosition().x - t.getX();
            float terrainZ = b.getPosition().z - t.getZ();
            float gridSquareSize = Terrain.getSize() / (float) (t.getHeights().length - 1);
            int gridX = (int) Math.floor(terrainX / gridSquareSize);
            int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
            float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
            float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
            PhysicalFace forResolution;
            if (xCoord <= (1 - zCoord)) {
                Vector3f v1 = new Vector3f(), v2 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), normal = new Vector3f();
                p1.set(0, t.getHeights()[gridX][gridZ], 0);
                p2.set(1, t.getHeights()[gridX + 1][gridZ], 0);
                p3.set(0, t.getHeights()[gridX][gridZ + 1], 1);

                Vector3f.sub(p2, p1, v1);
                Vector3f.sub(p3, p1, v2);
                Vector3f.cross(v1, v2, normal);

                forResolution = new PhysicalFace(normal, p1, p2, p3);
            } else {
                Vector3f v1 = new Vector3f(), v2 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), normal = new Vector3f();
                p1.set(1, t.getHeights()[gridX + 1][gridZ], 0);
                p2.set(1, t.getHeights()[gridX + 1][gridZ + 1], 1);
                p3.set(0, t.getHeights()[gridX][gridZ + 1], 1);

                Vector3f.sub(p2, p1, v1);
                Vector3f.sub(p3, p1, v2);
                Vector3f.cross(v1, v2, normal);

                forResolution = new PhysicalFace(normal, p1, p2, p3);
            }

            resolvePlaneCollision(b, forResolution);

        }
    }

    public boolean resolveObstacleCollision(Ball b) {
        ArrayList<PhysicalFace> collidingFaces = new ArrayList<>();
        collidingFaces.addAll(world.getCollidingFacesEntities(b));
        System.out.println("NUMBER OF COLLIDING FACES: " + collidingFaces.size());

        if (collidingFaces.size() == 0)
             return false;

        Vector3f resetPosition = new Vector3f(b.getPosition().x, b.getPosition().y, b.getPosition().z);

        ArrayList<PhysicalFace> combined = new ArrayList<PhysicalFace>();
        combined.add(collidingFaces.get(0));
        System.out.println("Normal added: (" + combined.get(0).getNormal().x + "|" + combined.get(0).getNormal().y + "|" + combined.get(0).getNormal().z + ")");
        for (PhysicalFace f : collidingFaces) {
            boolean found = false;
            for (int i = 0; !found && i < combined.size(); i++) {
                if (Math.abs(Math.abs(f.getNormal().x) - Math.abs(combined.get(i).getNormal().x)) < NORMAL_TH &&
                        Math.abs(Math.abs(f.getNormal().y) - Math.abs(combined.get(i).getNormal().y)) < NORMAL_TH &&
                        Math.abs(Math.abs(f.getNormal().z) - Math.abs(combined.get(i).getNormal().z)) < NORMAL_TH)
                    found = true;
            }
            if (!found) {
                System.out.println("Normal added: (" + f.getNormal().x + "|" + f.getNormal().y + "|" + f.getNormal().z + ")");
                combined.add(f);
            }
        }

        System.out.println("Number of combined faces: " + combined.size());

        PhysicalFace forResolution;
        ArrayList<PhysicalFace> stillColliding = new ArrayList<>();
        if (combined.size() == 1) {
            forResolution = combined.get(0);
            Vector3f normal = new Vector3f(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
            System.out.printf("Normal of closest: (%f|%f|%f)\n", normal.x, normal.y, normal.z);
            normal.scale(Vector3f.dot(b.getVelocity(), normal) / normal.lengthSquared());
            normal.scale(-0.001f);
            while (b.collidesWith(collidingFaces)) {
                b.increasePosition(normal);
            }
        } else {
            Vector3f revBM = new Vector3f(b.getVelocity().x, b.getVelocity().y, b.getVelocity().z);
            revBM.normalise();
            revBM.scale(-0.001f);
            while (b.collidesWith(collidingFaces)) {
                b.increasePosition(revBM);
            }

            // push the ball back into the faces it collided with to register which ones it's colliding with now
            revBM.negate();
            b.increasePosition(revBM);
            for (PhysicalFace f : collidingFaces) {
                if (f.collidesWithFace(b))
                    stillColliding.add(f);
            }
            //stillColliding = collidingFaces;
            System.out.println("Number of faces still colliding: " + stillColliding.size());
            for (PhysicalFace f : stillColliding) {
                System.out.printf("Still colliding: (%f|%f|%f)\n", f.getNormal().x, f.getNormal().y, f.getNormal().z);
            }

            if (stillColliding.size() == 1) {
                forResolution = stillColliding.get(0);
                //b.move();
                revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                revBM.scale(-0.001f);
                while (forResolution.collidesWithFace(b))
                    b.increasePosition(revBM);
            }
            else if (stillColliding.size() == 2) {
                Vector3f closest1 = stillColliding.get(0).getClosestPoint(b);
                Vector3f closest2 = stillColliding.get(1).getClosestPoint(b);
                if (Math.abs(closest1.x - closest2.x) < NORMAL_TH && Math.abs(closest1.y - closest2.y) < NORMAL_TH && Math.abs(closest1.z - closest2.z) < NORMAL_TH) {
                    Vector3f edge = stillColliding.get(0).getCommonEdge(stillColliding.get(1));
                    if (edge == null) {
                        System.out.println("EDGE COULD NOT BE RESOLVED -> ONE PLANE CHOSEN RANDOMLY");
                        forResolution = stillColliding.get(0);
                    } else {
                        System.out.println("Stuff for edge collision: " + Vector3f.dot(b.getVelocity(), closest1) + " - " + Vector3f.dot(b.getVelocity(), b.getPosition()) + " = " + (Vector3f.dot(b.getVelocity(), closest1) - Vector3f.dot(b.getVelocity(), b.getPosition())));
                        Vector3f normal = new Vector3f();
                        /*Vector3f projOnEdge = new Vector3f(edge.x, edge.y, edge.z);
                        projOnEdge.scale(Vector3f.dot(edge, b.getVelocity()) / edge.lengthSquared());
                        System.out.printf("Edge: (%f|%f|%f)\n", edge.x, edge.y, edge.z);
                        System.out.printf("Projection of velocity on edge: (%f|%f|%f)\n", projOnEdge.x, projOnEdge.y, projOnEdge.z);
                        System.out.printf("Ball's velocity: (%f|%f|%f)\n", b.getVelocity().x, b.getVelocity().y, b.getVelocity().z);
                        Vector3f.sub(b.getVelocity(), projOnEdge, normal);*/
                        System.out.printf("Closest point: (%f|%f|%f)\n", closest1.x, closest1.y, closest1.z);
                        Vector3f.sub(b.getPosition(), closest1, normal);
                        System.out.printf("Normal for resolution: (%f|%f|%f)\n", normal.x, normal.y, normal.z);
                        forResolution = new PhysicalFace(normal, closest1, closest1, closest1);
                    }
                } else if (LinearAlgebra.distancePtPtSq(closest1, b.getPosition()) < LinearAlgebra.distancePtPtSq(closest2, b.getPosition())) {
                    forResolution = stillColliding.get(0);
                    System.out.println("Collision with first plane out of two: " + forResolution.getNormal());
                    // resetting the ball's position to prevent weird-looking behaviour where the ball could jump between positions
                    b.getPosition().set(resetPosition);
                    //b.move();
                    // then pushing the ball out of the colliding obstacle along the normal of the colliding face to make for a smooth-looking collision
                    revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                    //if (Math.min(Vector3f.angle(revBM, b.getVelocity()), Math.PI - Vector3f.angle(revBM, b.getVelocity())) < Math.toRadians(45)) {
                        revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                        revBM.scale(-0.001f);
                        while (forResolution.collidesWithFace(b))
                            b.increasePosition(revBM);
                    //}

                } else {
                    forResolution = stillColliding.get(1);
                    System.out.println("Collision with second plane out of two: " + forResolution.getNormal());
                    // resetting the ball's position to prevent weird-looking behaviour where the ball could jump between positions
                    b.getPosition().set(resetPosition);
                    //b.move();
                    // then pushing the ball out of the colliding obstacle along the normal of the colliding face to make for a smooth-looking collision
                    revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                    //if (Math.min(Vector3f.angle(revBM, b.getVelocity()), Math.PI - Vector3f.angle(revBM, b.getVelocity())) < Math.toRadians(45)) {
                        revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                        revBM.scale(-0.001f);
                        while (forResolution.collidesWithFace(b))
                            b.increasePosition(revBM);
                    //}
                }
                //MainGameLoop.currState.cleanUp();
                //DisplayManager.closeDisplay();
            } else {
                // it is assumed that all faces join in one point
                ArrayList<Vector3f> closestPoints = new ArrayList<>();
                for (PhysicalFace f : stillColliding)
                    closestPoints.add(f.getClosestPoint(b));

                // try to find a common vertex (which would be the vertex joining all faces)
                Vector3f vertex = null, edge = null;
                Vector3f dist1 = new Vector3f(), dist2 = new Vector3f();
                float lowestDistFace = Float.MAX_VALUE, lowestDistEdge = Float.MAX_VALUE;
                PhysicalFace closestFace = null;
                for (int i = 0; i < closestPoints.size(); i++) {
                    for (int j = 0; j < closestPoints.size(); j++) {
                        Vector3f.sub(b.getPosition(), closestPoints.get(i), dist1);
                        Vector3f.sub(b.getPosition(), closestPoints.get(j), dist2);
                        if (LinearAlgebra.pointsAreEqual(closestPoints.get(i), closestPoints.get(j))) { // needs to be changed
                            vertex = closestPoints.get(i);
                        } else if (Math.abs(dist1.lengthSquared() - dist2.lengthSquared()) < 0.01 && LinearAlgebra.distancePtPtSq(b.getPosition(), closestPoints.get(i)) < lowestDistEdge) {
                            edge = stillColliding.get(i).getCommonEdge(stillColliding.get(j));
                            lowestDistEdge = LinearAlgebra.distancePtPtSq(b.getPosition(), closestPoints.get(i));
                        } else if (dist1.lengthSquared() < lowestDistFace) {
                            lowestDistFace = dist1.lengthSquared();
                            closestFace = stillColliding.get(i);
                        }
                    }
                }
                if (vertex != null) {
                    // collide with the vertex
                    Vector3f normal = new Vector3f();
                    Vector3f.sub(b.getPosition(), vertex, normal);
                    forResolution = new PhysicalFace(normal, vertex, vertex, vertex);
                    // resetting the ball's position to prevent weird-looking behaviour where the ball could jump between positions
                    b.getPosition().set(resetPosition);
                    //b.move();
                    // then pushing the ball out of the colliding obstacle along the normal of the colliding face to make for a smooth-looking collision
                    revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                    //if (Math.min(Vector3f.angle(revBM, b.getVelocity()), Math.PI - Vector3f.angle(revBM, b.getVelocity())) < Math.toRadians(45)) {
                        revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                        revBM.scale(-0.001f);
                        while (forResolution.collidesWithFace(b))
                            b.increasePosition(revBM);
                    //}
                } else if (edge != null) {
                    // collide with an edge
                    Vector3f normal = new Vector3f();
                    Vector3f projOnEdge = new Vector3f(b.getVelocity().x, b.getVelocity().y, b.getVelocity().z);
                    Vector3f.sub(projOnEdge, (Vector3f) edge.scale(Vector3f.dot(edge, projOnEdge)), projOnEdge);
                    Vector3f.sub(b.getVelocity(), projOnEdge, normal);
                    forResolution = new PhysicalFace(normal, null, null, null);
                    // resetting the ball's position to prevent weird-looking behaviour where the ball could jump between positions
                    b.getPosition().set(resetPosition);
                    //b.move();
                    // then pushing the ball out of the colliding obstacle along the normal of the colliding face to make for a smooth-looking collision
                    revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                    //if (Math.min(Vector3f.angle(revBM, b.getVelocity()), Math.PI - Vector3f.angle(revBM, b.getVelocity())) < Math.toRadians(45)) {
                        revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                        revBM.scale(-0.001f);
                        while (forResolution.collidesWithFace(b))
                            b.increasePosition(revBM);
                    //}
                } else {
                    // collide with the closest face
                    forResolution = closestFace;
                    // resetting the ball's position to prevent weird-looking behaviour where the ball could jump between positions
                    b.getPosition().set(resetPosition);
                    //b.move();
                    // then pushing the ball out of the colliding obstacle along the normal of the colliding face to make for a smooth-looking collision
                    revBM.set(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
                    //if (Math.min(Vector3f.angle(revBM, b.getVelocity()), Math.PI - Vector3f.angle(revBM, b.getVelocity())) < Math.toRadians(45)) {
                        revBM.scale(Vector3f.dot(b.getVelocity(), revBM) / revBM.lengthSquared());
                        revBM.scale(-0.001f);
                        while (forResolution.collidesWithFace(b))
                            b.increasePosition(revBM);
                    //}
                }
            }
        }

        resolvePlaneCollision(b, forResolution);
        System.out.println("COLLISION WITH OBSTACLE RESOLVED");
        //MainGameLoop.currState.cleanUp();
        //DisplayManager.closeDisplay();
        return true;
    }

    public void resolveBallCollision(Ball b1) {
        for (RealBall b2 : this.balls) {
            if (!b1.equals(b2)) {
                // the normal is chosen from b1 to b2 so that it will not only be parallel to the new vector of movement of b2 but will also point in the right direction
                Vector3f normal = new Vector3f(b2.getPosition().x - b1.getPosition().x, b2.getPosition().y - b1.getPosition().y, b2.getPosition().z - b1.getPosition().z);
                if (normal.lengthSquared() < Math.pow(2 * Ball.RADIUS, 2)) {
                    System.out.println("BALL COLLISION OCCURS");

                    // the moving ball (b1) is pushed out of the previously unmoving ball (b2)
                    Vector3f revM = new Vector3f(b1.getVelocity().x, b1.getVelocity().y, b1.getVelocity().z);
                    revM.negate();
                    revM.normalise();
                    revM.scale(0.001f);
                    while (normal.lengthSquared() < Math.pow(2 * Ball.RADIUS, 2)) {
                        b1.increasePosition(revM.x, revM.y, revM.z);
                        normal.set(b2.getPosition().x - b1.getPosition().x, b2.getPosition().y - b1.getPosition().y, b2.getPosition().z - b1.getPosition().z);
                    }

                    // this coefficient of restitution should be adapted to belong to ball-ball collisions (should be higher than for ball-green collisions)
                    b1.getVelocity().scale(noiseHandler.getRestitutionNoise());

                    // using simple geometry the magnitudes of the balls' velocity's after collision are calculated (their directions are along normal and tangent of the collision)
                    float alpha = Vector3f.angle(b1.getVelocity(), normal);
                    alpha = Math.min(alpha, (float) (Math.PI - alpha));
                    float factorB2 = (float) (Math.cos(alpha) * b1.getVelocity().length());
                    float factorB1 = (float) (Math.sin(alpha) * b1.getVelocity().length());

                    // a projection of the vector of movement of the moving ball (b1) on the collision plane is used to calculate the direction of its movement after the collision
                    // set the velocity of the second ball to one along the normal of the collision, scale it to the appropriate magnitude and update the "moving" status of the ball b2
                    normal.normalise();
                    Vector3f projection = new Vector3f(b1.getVelocity().x, b1.getVelocity().y, b1.getVelocity().z);
                    Vector3f.sub(projection, (Vector3f) normal.scale(Vector3f.dot(normal, projection)), projection);
                    normal.normalise();
                    normal.scale(factorB2);
                    // check if the ball b1 is real, so that it would actually influence the other ball's movement
                    if (!(b1 instanceof VirtualBall)) {
                        b2.setVelocity(normal);
                        b2.setMoving(true);
                    }

                    // set the velocity of the already previously moving ball to its appropriate magnitude and update b1's velocity
                    if (projection.lengthSquared() > 0) {
                        projection.normalise();
                        projection.scale(factorB1);
                    }
                    b1.setVelocity(projection);
                }
            }
        }
    }

    private void resolvePlaneCollision(Ball b, PhysicalFace forResolution) {
        System.out.println("Ball's velocity before collision resolution: " + b.getVelocity());
        // calculate the angle between the plane that is used for collision resolution and the velocity vector of the ball
        // since Vector3f.angle(x, y) can compute angles over 90 degrees, there is an additional check to make sure the angle is below 90 degrees
        float temp = Vector3f.angle(forResolution.getNormal(), b.getVelocity());
        float angle = (float) Math.min(Math.PI - temp, temp);
        angle = (float) (Math.PI / 2 - angle);

        // some pre-processing whose results will be used in both the bouncing and the rolling case
        // the normal component of the velocity is the projection of said velocity on the normal vector of the plane
        // that normal component will then be subtracted from the original velocity to get the new one
        Vector3f normal = new Vector3f(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
        Vector3f normalComponent = new Vector3f(normal.x, normal.y, normal.z);
        normalComponent.scale(2 * Vector3f.dot(b.getVelocity(), normal) * (1 / normal.lengthSquared()));
        Vector3f.sub(b.getVelocity(), normalComponent, b.getVelocity());

        // 45 degrees and 5 degrees are estimated
        if (angle > Math.toRadians(45) || (angle * b.getVelocity().lengthSquared() * C > 1 && angle > Math.toRadians(ANGLE_TH))) {
            // the ball is bouncing and the velocity can simply remain as is, only the coefficient of restitution has to be applied
            System.out.println("BOUNCING");
            System.out.println(b.getVelocity().x + " " + b.getVelocity().y + " " + b.getVelocity().z);
            b.scaleVelocity(noiseHandler.getRestitutionNoise());
            System.out.println(b.getRotation().x + " " + b.getRotation().y + " " + b.getRotation().z);
        } else {
            // the ball is rolling (or sliding but that is not implemented (yet)), therefore a projection on the plane instead of a reflection is used
            System.out.println("ROLLING");
            Vector3f projection = new Vector3f();
            normalComponent.scale(-0.5f);
            Vector3f.sub(b.getVelocity(), normalComponent, projection);

            // friction is applied in the opposite direction as the movement of the ball, the vector can therefore be constructed from the projection
            Vector3f frictionDir = new Vector3f(projection.x, projection.y, projection.z);
            if (frictionDir.lengthSquared() != 0)
                frictionDir.normalise();

            // the angle of inclination of the plane in the direction of movement/friction in respect to the horizontal plane
            // knowing this angle, the magnitude of the frictional force and acceleration can be computed
            // since F = m * a <=> F/m = a <=> F/m * t = v the effect on the velocity is computed as done below (Ffriction = coeffFriction * Fnormal)
            float angleIncl = Vector3f.angle(new Vector3f(frictionDir.x, 0, frictionDir.z), frictionDir);
            angleIncl = (float) Math.min(Math.PI - angleIncl, angleIncl);
            float frictionVelComponent = noiseHandler.getFrictionNoise() * (PhysicsEngine.GRAVITY.length() * (float) (Math.cos(angleIncl))) * b.getTimeElapsed();
            frictionDir = (Vector3f) frictionDir.scale(-frictionVelComponent);

            // finally, the velocity of the ball is set to the projection (since the ball is not supposed to be bouncing)
            // then friction is applied (if the effect of friction is larger than the actual velocity, the ball just stops)
            b.setVelocity(projection.x, projection.y, projection.z);
            if (b.getVelocity().lengthSquared() > frictionDir.lengthSquared())
                b.increaseVelocity(frictionDir);
            else if (b.isMoving()) {
                b.setVelocity(0, 0, 0);
                b.setMoving(false);
            }
        }
        System.out.println("Ball's velocity after collision resolution: " + b.getVelocity());
    }

    public ShotData performVirtualShot(RealBall b, Vector3f shotVel) {
        ArrayList<Entity> obstaclesHit = new ArrayList<Entity>();

        // the position and velocity of the virtual ball which is updated instead of a real ball
        VirtualBall ball = new VirtualBall(b, shotVel);
        System.out.printf("Initial position of the virtual ball: (%f|%f|%f)\n", ball.getPosition().x, ball.getPosition().y, ball.getPosition().z);
        int counter = 0;
        long one = System.currentTimeMillis();
        while (ball.isMoving() || counter < 10) {
            ball.applyAccel(GRAVITY);
            if ((ball.isMoving() && ball.movedLastStep()) || counter < 10) {
                ball.updateAndMove();
                resolveBallCollision(ball);
                if (resolveObstacleCollision(ball))
                    obstaclesHit.addAll(world.getCollidingEntities(ball));
                else
                    resolveTerrainCollision(ball);
            } else if (ball.isMoving()) {
                ball.setVelocity(0, 0, 0);
                ball.setMoving(false);
            }
            counter++;
        }
        long two = System.currentTimeMillis();
        System.out.println("Virtual shot took " + (two - one) + "ms");

        return new ShotData(shotVel, b.getPosition(), ball.getPosition(), obstaclesHit);
    }
    
    public AIShot aiTestShot(RealBall b, Vector3f shotVel, Node[][] grid){
    	AIShot shot = new AIShot(shotVel);
        VirtualBall ball = new VirtualBall(b, shotVel);
        System.out.printf("Initial position of the virtual ball: (%f|%f|%f)\n", ball.getPosition().x, ball.getPosition().y, ball.getPosition().z);
        int counter = 0;
        long one = System.currentTimeMillis();
        while (ball.isMoving() || counter < 10) {
            ball.applyAccel(GRAVITY);
            if ((ball.isMoving() && ball.movedLastStep()) || counter < 10) {
                ball.updateAndMove();
                resolveBallCollision(ball);
                if (!resolveObstacleCollision(ball))
                    resolveTerrainCollision(ball);
            } else if (ball.isMoving()) {
                ball.setVelocity(0, 0, 0);
                ball.setMoving(false);
            }
            counter++;

            int gridX = (int) (ball.getPosition().x / Algorithm.CELL_SIZE);
            int gridZ = (int) (ball.getPosition().z / Algorithm.CELL_SIZE);
            if (gridX >= 0 && gridZ >= 0 && gridX < grid.length && gridZ < grid.length){
                Node n = grid[gridX][gridZ];
                shot.addNode(n);
            }
        }
        long two = System.currentTimeMillis();
        System.out.println("Virtual shot took " + (two - one) + "ms");
    	
    	return shot;
    }

    // this method totally makes sense in the physics engine, trust me
    public Vector3f[] getEvasionVector(Vector3f position, Entity e) {
        // check in which region the ball is currently: either closest to an edge/corner of the bounding box or closest to one of its faces/sides
        Vector3f[] result = new Vector3f[2];
        float minX = e.getCollisionData().getBoundingBox().getMinX(), maxX = e.getCollisionData().getBoundingBox().getMaxX(), minZ = e.getCollisionData().getBoundingBox().getMinZ(), maxZ = e.getCollisionData().getBoundingBox().getMaxZ();
        Vector3f[] corners = {new Vector3f(minX, position.y, minZ), new Vector3f(minX, position.y, maxZ), new Vector3f(maxX, position.y, maxZ), new Vector3f(maxX, position.y, minZ)};
        Vector3f closestCorner = new Vector3f();
        float closestDistCorner = Float.MAX_VALUE, closestDistLineSeg = Float.MAX_VALUE, temp;
        for (int i = 0; i < corners.length; i++) {
            if (LinearAlgebra.distancePtPtSq(position, corners[i]) < closestDistCorner) {
                closestCorner = corners[i];
                closestDistCorner = LinearAlgebra.distancePtPtSq(position, closestCorner);
            }
            // distance to line segments: first minMin-minMax, then minMax-maxMax, then maxMax-maxMin, then maxMin-minMin
            if ((temp = LinearAlgebra.distancePtLineSegSq(position, corners[i], corners[i % 3])) < closestDistLineSeg) {
                closestDistLineSeg = temp;
            }
        }

        Vector3f nextClosest = new Vector3f();
        float nextClosestDistCorner = Float.MAX_VALUE;
        for (int i = 0; i < corners.length; i++) {
            if (corners[i] != closestCorner && LinearAlgebra.distancePtPtSq(position, corners[i]) < nextClosestDistCorner) {
                nextClosest = corners[i];
                nextClosestDistCorner = LinearAlgebra.distancePtPtSq(position, nextClosest);
            }
        }

        // if the ball is closest to a side take the two closest corner points as "evasion points"
        if (closestDistLineSeg <= closestDistCorner) {
            result[0] = Vector3f.sub(closestCorner, position, null);
            result[1] = Vector3f.sub(nextClosest, position, null);
            return result;
        }

        Vector3f secondNextClosest = new Vector3f();
        float secondNextClosestDistCorner = Float.MAX_VALUE;
        for (int i = 0; i < corners.length; i++) {
            if (corners[i] != closestCorner && corners[i] != secondNextClosest && LinearAlgebra.distancePtPtSq(position, corners[i]) < secondNextClosestDistCorner) {
                secondNextClosest = corners[i];
                secondNextClosestDistCorner = LinearAlgebra.distancePtPtSq(position, secondNextClosest);
            }
        }

        // if the ball is closest to a corner take the second and third-closest points as "evasion points"
        result[0] = Vector3f.sub(nextClosest, position, null);
        result[1] = Vector3f.sub(secondNextClosest, position, null);
        return result;
    }

    /*public float getHeightAt(float x, float z) {
        Ball b = new VirtualBall(new Vector3f(x, 20, z));
        Entity belowBall = null;
        for (Entity e : world.getEntities()) {
            if (e.inHorizontalBounds(b)) {
                belowBall = e;
            }
        }

        if (belowBall == null) {
            return world.getHeightOfTerrain(x, z);
        } else {
            while (!belowBall.collides(b)) {
                b.increasePosition(0, -1f, 0);
            }
            
            ArrayList<PhysicalFace> collidingFaces = belowBall.getCollidingFaces(b);
            while(b.collidesWith(collidingFaces)){
            	b.increasePosition(0, 0.01f, 0);
            }
            System.out.println("Difference in value " + Math.abs(world.getHeightOfTerrain(x, z) - b.getPosition().y));
            return b.getPosition().y - Ball.RADIUS;
        }
    }*/

    public float getHeightAt(float x, float z) {
        ArrayList<Entity> belowBall = null;
        for (Entity e : world.getEntities()) {
            if (e.isCollidable() && e.inHorizontalBounds(x, z)) {
                if (belowBall == null)
                    belowBall = new ArrayList<>();
                belowBall.add(e);
            }
        }

        if (belowBall == null) {
            return world.getHeightOfTerrain(x, z);
        }

        //System.out.println("Below ball: " + belowBall.get(0));

        float curHeight, maxHeight = -Float.MAX_VALUE;
        for (Entity e : belowBall) {
            if ((curHeight = e.getHighestPointOnLine(new Vector3f(x, 10, z), new Vector3f(x, 0, z))) > maxHeight)
                maxHeight = curHeight;
        }

        if (world.getHeightOfTerrain(x, z) > maxHeight) {
            return world.getHeightOfTerrain(x, z);
        }

        return maxHeight;
    }

}