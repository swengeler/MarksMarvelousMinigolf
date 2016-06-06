package water;

import org.lwjgl.util.vector.Vector3f;

public class WaterTile {
	
	public static final float TILE_SIZE = 400;
	
	private float height;
	private float x,z;
	
	public WaterTile(float centerX, float centerZ, float height){
		this.x = centerX;
		this.z = centerZ;
		this.height = height;
	}

	public float getHeight() {
		return height;
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public void setPosition(Vector3f terrainPoint) {
		this.x = terrainPoint.x;
		this.z = terrainPoint.z;
	}



}
