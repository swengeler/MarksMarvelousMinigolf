package bot2_0;

import entities.playable.Ball;
import terrains.World;

public class BobTheBot {

	private Algorithm alg;
	private Ball ball;
	private World world;
	
	public BobTheBot(int algorithm, Ball ball, World world){
		this.ball = ball;
		this.world = world;
		setAlgorithm(algorithm);
		
	}

	public Ball getBall() {
		return ball;
	}

	public void setBall(Ball ball) {
		this.ball = ball;
	}

	public Algorithm getAlg() {
		return alg;
	}
	
	public void setAlgorithm(int algorithm){
		if (algorithm == 0) {}
			alg = new HMPathing(ball, world);
	}
	
	public void shoot(){
		if(!ball.isMoving())
			alg.shootBall();
	}
	
}
