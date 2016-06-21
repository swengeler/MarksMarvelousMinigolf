package artificialIntelligence.algorithms;

import artificialIntelligence.utils.Node;

public abstract class Algorithm {
	
	public static final float CELL_SIZE = 1f;
	
	public abstract void shootBall();
	public abstract Node createGraph();
	
}
