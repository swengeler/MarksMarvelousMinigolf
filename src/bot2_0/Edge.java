package bot2_0;

import physics.utils.ShotData;

public class Edge {
	
	private ShotData data;
	private float sqDistance;
	
	public Edge(ShotData sData, float d){
		data = sData;
		sqDistance = d;
	}

	public ShotData getData() {
		return data;
	}

	public void setData(ShotData data) {
		this.data = data;
	}

	public float getDistance() {
		return sqDistance;
	}

	public void setDistance(float distance) {
		this.sqDistance = distance;
	}
	
	
	
}
