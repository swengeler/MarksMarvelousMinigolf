package bot2_0;

import java.util.ArrayList;

import entities.playable.Ball;
import physics.utils.ShotData;
import terrains.World;

public abstract class Algorithm {

	public abstract ArrayList<ShotData> getPath(Ball b, World w);
	public abstract Node createGraph(Ball b, World w);
	
}
