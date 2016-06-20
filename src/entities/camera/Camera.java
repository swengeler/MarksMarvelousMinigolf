package entities.camera;
import entities.playable.Ball;
import entities.playable.RealBall;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	private static Camera instance;
	
	protected float distanceFromBall = 100;
	protected float angleAroundBall = 0;
	
	protected Vector3f position = new Vector3f(0, 0, 0);
	protected float pitch = 20;			//How high or low the camera is aimed
	protected float yaw;				//How much left or right the camera is aiming
	protected float roll;				//How much the camera is tilted
	
	protected Ball ball;
	
	public Camera(Ball ball){
		this.ball = ball;
		if (ball instanceof Empty){
			distanceFromBall = 0;
			((Empty) ball).setCamera(this);
		}
		instance = this;
	}
	
	public static Camera getInstance(){
		return instance;
	}
	
	public Ball getBall() {
		return ball;
	}

	public void setBirdsEyeView() {
		position.set(150, 230, 150);
		pitch = 90;
		yaw = 180;
		distanceFromBall = 190;
	}

	public void move(){
		calculateZoom();
		calculatePitch();
		calculateAngleAroundBall();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (((RealBall)ball).getRotY() + angleAroundBall);
	}

	public void set(Vector3f position, float pitch, float yaw, float distanceFromBall) {
		this.position.set(position);
		this.pitch = pitch;
		this.yaw = yaw;
		this.distanceFromBall = distanceFromBall;
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

	public float getDistanceFromBall() {
		return distanceFromBall;
	}
	
	public float getAngleAroundBall(){
		return angleAroundBall;
	}
	
	protected void calculateCameraPosition (float horizDistance, float verticDistance){
		float theta = ball.getRotY() + angleAroundBall;
		float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));
		position.x = ball.getPosition().x - offsetX;
		position.y = ball.getPosition().y + verticDistance;
		position.z = ball.getPosition().z - offsetZ;
	}
	
	protected float calculateHorizontalDistance(){
		return (float) (distanceFromBall * Math.cos(Math.toRadians(pitch)));
	}
	
	protected float calculateVerticalDistance(){
		return (float) (distanceFromBall * Math.sin(Math.toRadians(pitch)));
	}
	
	private void calculateZoom(){
		float zoomLevel = Mouse.getDWheel()*0.1f;
		distanceFromBall -= zoomLevel;
	}
	
	private void calculatePitch(){
		if (Mouse.isButtonDown(0)){
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundBall(){
		if (Mouse.isButtonDown(0)){
			float angleChange = Mouse.getDX() * 0.3f;
			angleAroundBall -= angleChange;
			
		}
	}

	public void invertPitch() {
	 this.pitch = -pitch;
	}
	
}
