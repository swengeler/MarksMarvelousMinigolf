package Physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import engineTester.MainGameLoop;
import entities.RealBall;
import entities.VirtualBall;
import entities.Ball;
import entities.Entity;
import terrains.Terrain;
import terrains.World;
import toolbox.Maths;

public class PhysicsEngine {

	static final float NORMAL_TH = 0.001f;
	private static final float ANGLE_TH  = 5f;
	private static final float C = 0.001f;
	public static final float MIN_MOV_REQ = 0.000f;

	public static final float REAL_GRAVITY = 9.813f;

	public static final Vector3f GRAVITY = new Vector3f(0, -230f, 0);
	public static final float COEFF_RESTITUTION = 0.75f;
	public static final float COEFF_FRICTION = 0.15f;

    private static final double FRICTION_STD = 0.5;
	

	private List<RealBall> balls;
	private World world;

    private Random r;
	private boolean enabled;

    private ArrayList<Vector3f> globalAccel;

	public PhysicsEngine(List<Ball> balls, World world) {
		this.balls = new ArrayList<RealBall>();
		for(Ball b:balls)
			if(b instanceof RealBall)
				this.balls.add((RealBall)b);
		this.world = world;
		this.enabled = true;
        this.r = new Random();
        this.globalAccel = new ArrayList<Vector3f>();
        this.addGlobalAccel(PhysicsEngine.GRAVITY);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void addBall(RealBall ball) {
		this.balls.add(ball);
	}

    public void addGlobalAccel(Vector3f accel) {
        this.globalAccel.add(accel);
    }

    public void removeGlobalAccel(Vector3f accel) {
        this.globalAccel.remove(accel);
    }

	public void tick() {
		for (RealBall b : balls) {
            b.applyGlobalAccel(this.globalAccel, r);
            b.applyAccel();
			if ((b.isMoving() && (b.movedLastStep() || b.getLastTimeElapsed() == 0)) || MainGameLoop.getCounter() < 10) {
				b.updateAndMove();
				System.out.println("\n---- Collision detection starts ----\n");
				resolveBallCollision(b);
				resolveObstacleCollision(b);
				resolveTerrainCollision(b);
				System.out.println("\n---- Collision detection ends ----\n");
			} else {
				b.setVelocity(0, 0, 0);
			}
		}
	}

	public void resolveTerrainCollision(Ball b) {
		/*
		 * The collision detection and resolution works according to the following steps:
		 * 1. Collision is detected based on triangle mesh of the terrain (NOT JUST height below ball)
		 * 2. Ball is pushed upwards until it no longer collides (according to the ball/mesh criteria)
		 * 3. The then-closest plane/triangle is taken as the point of contact and all further calculations are using that one plane
		 * 4. The movement of the ball is adjusted based on angle of incidence, velocity, friction/restitution etc. with the selected plane
		 */

		// get all faces/triangles of the terrain mesh that the ball collides with
		//ArrayList<PhysicalFace> collidingFaces = new ArrayList<PhysicalFace>();
		//collidingFaces.addAll(world.getCollidingFacesTerrains(b));

		// e.g. if the ball is above the maximum height of the terrain, then there is no need to actually resolve any collision
		//if (collidingFaces.isEmpty())
			//return;

		// push the ball out of the terrain so it remains on the surface
		// since its a terrain (which comes with certain restrictions) the ball is simply pushed upwards to simplify matters
		//while (b.collidesWith(collidingFaces))
			//b.increasePosition(0, 0.01f, 0);

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
			PhysicalFace forResolution = null;
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

	public void resolveObstacleCollision(Ball b) {
		ArrayList<PhysicalFace> collidingFaces = new ArrayList<PhysicalFace>();
		collidingFaces.addAll(world.getCollidingFacesEntities(b));
		
		if (collidingFaces.size() == 0) 
			return;
		
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
		
		if (combined.size() > 1) {
			Vector3f revBM = new Vector3f(b.getVelocity().x, b.getVelocity().y, b.getVelocity().z);
			revBM.normalise();
			revBM.scale(-0.001f);
			while (b.collidesWith(collidingFaces)) {
				b.increasePosition(revBM);
			}
		}
		
		PhysicalFace closest = collidingFaces.get(0);
		float lowestDistSq = closest.distanceToFaceSq(b);
		for (PhysicalFace f : collidingFaces) {
			if (f.distanceToFaceSq(b) < lowestDistSq) {
				closest = f;
				lowestDistSq = f.distanceToFaceSq(b);
			}
		}
		
		System.out.println("Lowest distance to ball: " + Math.sqrt(lowestDistSq));
		
		if (combined.size() == 1) {
			Vector3f normal = new Vector3f(closest.getNormal().x, closest.getNormal().y, closest.getNormal().z);
			System.out.printf("Normal of closest: (%f|%f|%f)\n", normal.x, normal.x, normal.x);
			normal.scale(Vector3f.dot(b.getVelocity(), normal)/normal.lengthSquared());
			normal.scale(-0.001f);
			while (b.collidesWith(collidingFaces)) {
				b.increasePosition(normal);
			}
		}
		
		resolvePlaneCollision(b, closest);
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
					b1.getVelocity().scale(COEFF_RESTITUTION);

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
		// calculate the angle between the plane that is used for collision resolution and the velocity vector of the ball
		// since Vector3f.angle(x, y) can compute angles over 90 degrees, there is an additional check to make sure the angle is below 90 degrees
		float temp = Vector3f.angle(forResolution.getNormal(), b.getVelocity());
		float angle = (float) Math.min(Math.PI - temp, temp);
		angle = (float) (Math.PI/2 - angle);

		// some pre-processing whose results will be used in both the bouncing and the rolling case
		// the normal component of the velocity is the projection of said velocity on the normal vector of the plane
		// that normal component will then be subtracted from the original velocity to get the new one
		Vector3f normal = new Vector3f(forResolution.getNormal().x, forResolution.getNormal().y, forResolution.getNormal().z);
		Vector3f normalComponent = new Vector3f(normal.x, normal.y, normal.z);
		normalComponent.scale(2 * Vector3f.dot(b.getVelocity(), normal) * (1/normal.lengthSquared()));
		Vector3f.sub(b.getVelocity(), normalComponent, b.getVelocity());

		// 45 degrees and 5 degrees are estimated
		if (angle > Math.toRadians(45) || (angle * b.getVelocity().lengthSquared() * C > 1 && angle > Math.toRadians(ANGLE_TH))) {
			// the ball is bouncing and the velocity can simply remain as is, only the coefficient of restitution has to be applied
			System.out.println("BOUNCING");
			System.out.println(b.getVelocity().x + " " + b.getVelocity().y+ " " + b.getVelocity().z);
			//b.scaleVelocity(COEFF_RESTITUTION);
			System.out.println(b.getVelocity().x + " " + b.getVelocity().y+ " " + b.getVelocity().z);
			if(b.getVelocity().x!=0){
			b.setVelocity(xspeed(b), b.getVelocity().y*COEFF_RESTITUTION, b.getVelocity().z);
			}else
				b.scaleVelocity(COEFF_RESTITUTION);
				
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
			float angleIncl = Vector3f.angle(new Vector3f(frictionDir.x,0,frictionDir.z), frictionDir);
			angleIncl = (float) Math.min(Math.PI - angleIncl, angleIncl);
			float frictionVelComponent = randomiseFriction() * (PhysicsEngine.GRAVITY.length() * (float) (Math.cos(angleIncl))) * b.getTimeElapsed();
			frictionDir = (Vector3f) frictionDir.scale(-frictionVelComponent);
            //randomiseFriction(frictionDir);

            // finally, the velocity of the ball is set to the projection (since the ball is not supposed to be bouncing)
			// then friction is applied (if the effect of friction is larger than the actual velocity, the ball just stops)
			b.setVelocity(projection.x, projection.y, projection.z);
			if (b.getVelocity().lengthSquared() > frictionDir.lengthSquared())
				b.increaseVelocity(frictionDir);
			else {
				b.setVelocity(0, 0, 0);
				b.setMoving(false);
			}
		}
	}

    private float randomiseFriction() {
        double std = FRICTION_STD * COEFF_FRICTION;
        double newFriction = r.nextGaussian() * std + COEFF_FRICTION;

        System.out.println("\nFRICTION FROM " + COEFF_FRICTION + " TO " + newFriction + "\n");
        return (float) newFriction;
    }

    private void randomiseFriction(Vector3f friction) {
        double mean = friction.length();
        double std = FRICTION_STD * mean;
        double newLength = r.nextGaussian() * std + mean;
        friction.scale((float) (newLength / mean));

        System.out.println("\nFRICTION FROM " + mean + " TO " + newLength + "\n");
    }

	public ShotData performVirtualShot(RealBall b, Vector3f shotVel) {
		ArrayList<Entity> obstaclesHit = new ArrayList<Entity>();

		// the position and velocity of the virtual ball which is updated instead of a real ball
		VirtualBall ball = new VirtualBall(b, shotVel);
		System.out.printf("Initial position of the virtual ball: (%f|%f|%f)\n", ball.getPosition().x, ball.getPosition().y, ball.getPosition().z);
		int counter = 0;
		while (ball.isMoving() || counter < 10) {
			ball.applyAccelerations();
			if ((ball.isMoving() && ball.movedLastStep()) || counter < 10) {
				ball.updateAndMove();
				resolveTerrainCollision(ball);
				resolveBallCollision(ball);
				obstaclesHit.addAll(world.getCollidingEntities(ball));
				//resolveObstacleCollision(ball);
			} else {
				ball.setVelocity(0, 0, 0);
				ball.setMoving(false);
			}
			counter++;
		}

		return new ShotData(shotVel, ball.getPosition(), obstaclesHit);
	}
	public float applyspin(Ball b){
		float x;
		float a=(float) 0.4;
		x=b.getRotation().x*((a-COEFF_RESTITUTION)/(1+a)+(((1+COEFF_RESTITUTION)/(1+a))*b.getVelocity().x/(b.getRadius()*b.getRotation().x)));
		return x;
	}
	
	public float xspeed(Ball b){
		float x;
		float a= (float) 0.4;
		x=b.getVelocity().x*((1-a*COEFF_RESTITUTION)/(1+a)+ (a*(1+COEFF_RESTITUTION)/(1+a))*(b.getRadius()*b.getRotation().x)/b.getVelocity().x);
	return x;	
	}

}
