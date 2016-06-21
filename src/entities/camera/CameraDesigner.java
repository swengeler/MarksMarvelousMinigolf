package entities.camera;

import entities.playable.Ball;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class CameraDesigner extends Camera {
	
	public CameraDesigner(Ball ball) {
		super(ball);
		this.ball = ball;
		if (ball instanceof Empty){
			distanceFromBall = 0;
			((Empty) ball).setCamera(this);
		}
	}

	public void move() {
		calculateZoom();
		calculatePitch();
		calculateAngleAroundBall();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (ball.getRotY() + angleAroundBall);
		//System.out.println("Camera:\nPosition: " + position + "\nPitch: " + pitch + ", yaw: " + yaw + ", roll: " + roll + ", distance from ball: " + distanceFromBall + ", angle around ball: " + angleAroundBall);
	}

	private void calculateZoom(){
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			distanceFromBall++;
		}else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			distanceFromBall--;
		}
		//float zoomLevel = Mouse.getDWheel()*0.1f;
		//distanceFromBall -= zoomLevel;
	}
	
	private void calculatePitch(){
		/*
		if(Mouse.isButtonDown(0)){
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
			
		}
		*/
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			pitch++;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			pitch--;
		}
	}
	
	private void calculateAngleAroundBall(){

	}

}
