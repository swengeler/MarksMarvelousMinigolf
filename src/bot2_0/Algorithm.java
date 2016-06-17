package bot2_0;

import java.util.ArrayList;

import entities.playable.Ball;
import physics.utils.ShotData;
import terrains.World;

public abstract class Algorithm {
	
	public static final float CELL_SIZE = 1f;
	
	public abstract void shootBall();
	public abstract Node createGraph();
	
}
