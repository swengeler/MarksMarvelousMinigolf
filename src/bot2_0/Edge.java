package bot2_0;

import physics.utils.ShotData;

public class Edge {
	
	private float length;
	private boolean visited = false;
	
	public Edge(float length){
		this.length = length;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	
	
	
}
