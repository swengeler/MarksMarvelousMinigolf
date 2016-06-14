package bot2_0;

import entities.playable.Ball;

public class BobTheBot {

	private Algorithm alg;
	private Ball ball;
	
	public BobTheBot(int algorithm, Ball ball){
		this.ball = ball;
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
		
	}
	
}
