package terrains;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.collisions.CollisionData;
import physics.collisions.PhysicalFace;
import entities.playable.Ball;
import entities.playable.RealBall;
import models.RawModel;
import renderEngine.utils.Loader;
import textures.ModelTexture;

public class Terrain implements Serializable {

	private static final float SIZE = 300;
	private static final float MAX_HEIGHT = 40;
	private static final float RADIUS = 15;
	private static final float MAX_PIXEL_COLOR = 256 * 256 * 256;
	
	private float x;
	private float z;
	private float maxHeight;
	private float minHeight;
	private RawModel model;
	private ModelTexture texture;
	private CollisionData cdata;
	
	private float[][] heights;
	
	private int VERTEX_COUNT;
	
	public Terrain(int gridX, int gridZ, Loader loader, ModelTexture texture, boolean rand){
		this.texture = texture;
		this.x = gridX * getSize();
		this.z = gridZ * getSize();
		this.maxHeight = Float.MIN_VALUE;
		this.minHeight = Float.MAX_VALUE;
		this.model = generateTerrain(loader, rand);
		
	}
	
	public Terrain(int gridX, int gridZ, Loader loader, ModelTexture texture, String heightMap){
		this.texture = texture;
		this.x = gridX * getSize();
		this.z = gridZ * getSize();
		this.maxHeight = Float.MIN_VALUE;
		this.minHeight = Float.MAX_VALUE;
		this.model = generateTerrain(loader, heightMap);
		
	}
	
	public Terrain(int gridX, int gridZ, Loader loader, ModelTexture texture, float[][] heights, Vector2f hole){
		this.texture = texture;
		this.x = gridX * getSize();
		this.z = gridZ * getSize();
		this.heights = heights;
		this.model = generateTerrain(loader, hole);
		
	}
	
	public float[][] getHeights(){
		return heights;
	}
	
	public float getGridX(){
		return this.x/getSize();
	}
	
	public float getGridZ(){
		return this.z/getSize();
	}
	
	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public RawModel getModel() {
		return model;
	}

	public ModelTexture getTexture() {
		return texture;
	}

	private RawModel generateTerrain(Loader loader, boolean rand){
		if(rand){
			VERTEX_COUNT = 256;
			HeightsGenerator generator = new HeightsGenerator((int)(x/SIZE),(int)(z/SIZE), VERTEX_COUNT, (int)(Math.random()*100000));
			heights = new float[VERTEX_COUNT][VERTEX_COUNT];
			
			int count = VERTEX_COUNT * VERTEX_COUNT;
			float[] vertices = new float[count * 3];
			float[] normals = new float[count * 3];
			float[] textureCoords = new float[count*2];
			int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
			int vertexPointer = 0;
			for(int i=0;i<VERTEX_COUNT;i++){
				for(int j=0;j<VERTEX_COUNT;j++){
					vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * getSize();
					heights[j][i] = getHeight(j, i, generator);
					if (heights[j][i] > maxHeight) {
						maxHeight = heights[j][i];
					}
					if (heights[j][i] < minHeight) {
						minHeight = heights[j][i];
					}
					vertices[vertexPointer*3+1] = heights[j][i];
					vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * getSize();
					Vector3f normal = calculateNormal(j, i, generator);
					normals[vertexPointer*3] = normal.x;
					normals[vertexPointer*3+1] = normal.y;
					normals[vertexPointer*3+2] = normal.z;
					textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
					textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
					vertexPointer++;
				}
			}
			int pointer = 0;
			for(int gz=0;gz<VERTEX_COUNT-1;gz++){
				for(int gx=0;gx<VERTEX_COUNT-1;gx++){
					int topLeft = (gz*VERTEX_COUNT)+gx;
					int topRight = topLeft + 1;
					int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
					int bottomRight = bottomLeft + 1;
					indices[pointer++] = topLeft;
					indices[pointer++] = bottomLeft;
					indices[pointer++] = topRight;
					indices[pointer++] = topRight;
					indices[pointer++] = bottomLeft;
					indices[pointer++] = bottomRight;
				}
			}
			return loader.loadToVAO(vertices, textureCoords, normals, indices);
		} else {
			VERTEX_COUNT = 512;
			heights = new float[VERTEX_COUNT][VERTEX_COUNT];
			
			int count = VERTEX_COUNT * VERTEX_COUNT;
			float[] vertices = new float[count * 3];
			float[] normals = new float[count * 3];
			float[] textureCoords = new float[count*2];
			int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
			int vertexPointer = 0;
			for(int i=0;i<VERTEX_COUNT;i++){
				for(int j=0;j<VERTEX_COUNT;j++){
					vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * getSize();
					heights[j][i] = 0;
					vertices[vertexPointer*3+1] = 0;
					vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * getSize();
					normals[vertexPointer*3] = 0;
					normals[vertexPointer*3+1] = 1;
					normals[vertexPointer*3+2] = 0;
					textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
					textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
					vertexPointer++;
				}
			}
			int pointer = 0;
			for(int gz=0;gz<VERTEX_COUNT-1;gz++){
				for(int gx=0;gx<VERTEX_COUNT-1;gx++){
					int topLeft = (gz*VERTEX_COUNT)+gx;
					int topRight = topLeft + 1;
					int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
					int bottomRight = bottomLeft + 1;
					indices[pointer++] = topLeft;
					indices[pointer++] = bottomLeft;
					indices[pointer++] = topRight;
					indices[pointer++] = topRight;
					indices[pointer++] = bottomLeft;
					indices[pointer++] = bottomRight;
				}
			}
			maxHeight = 0;
			minHeight = 0;
			return loader.loadToVAO(vertices, textureCoords, normals, indices);
		}
	}
	
	private RawModel generateTerrain(Loader loader, String heightMap){
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("res/" + heightMap + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		VERTEX_COUNT = image.getHeight();
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * getSize();
				heights[j][i] = getHeight(j,i,image);
				if (heights[j][i] > maxHeight) {
					maxHeight = heights[j][i];
				}
				if (heights[j][i] < minHeight) {
					minHeight = heights[j][i];
				}
				vertices[vertexPointer*3+1] = heights[j][i];
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * getSize();
				Vector3f normal = calculateNormal(j, i, image);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}
	
	private RawModel generateTerrain(Loader loader, Vector2f hole){
		VERTEX_COUNT = heights.length;
		System.out.println(VERTEX_COUNT);
		float newHeight = -4;
		int holeRad = 4;
		float x = (hole.getX() / (SIZE/2)) * (VERTEX_COUNT/2);
		float z = (hole.getY() / (SIZE/2)) * (VERTEX_COUNT/2);
		System.out.println("Hole at x: " + x + " z: " + z);
		for (float i = -holeRad; i <= holeRad; i++) {
			for (float k = -holeRad; k  <= holeRad; k++) {
				if (x + i >= 0 && x + i < VERTEX_COUNT && z + k >= 0 && z + k < VERTEX_COUNT) {
					float distance = (float) Math.sqrt((i*i)+(k*k));
					if (distance <= holeRad) {
						float height = (float) ((newHeight/2) * -(Math.cos(Math.PI - (Math.PI * (distance/holeRad)))) + (newHeight/2));
						heights[(int)(x + i)][(int)(z + k)] =  height;		
					}
				}
			}	
		}
		
		
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * getSize();
				
				vertices[vertexPointer*3+1] = heights[j][i];
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * getSize();
				Vector3f normal = calculateNormal(j, i, heights);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}
	
	public void updateTerrain(Loader loader, float x, float z) {
		float maxHillHeight = 5;
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		float newHeight = 10;
		for (float i = -RADIUS; i <= RADIUS; i++) {
			for (float k = -RADIUS; k  <= RADIUS; k++) {
				if (x + i >= 0 && x + i < VERTEX_COUNT && z + k >= 0 && z + k < VERTEX_COUNT) {
					float distance = (float) Math.sqrt((i*i)+(k*k));
					if (distance <= RADIUS) {
						float height = (float) ((newHeight/2) * -(Math.cos(Math.PI - (Math.PI * (distance/RADIUS))))+(newHeight/2));
						if (height >= maxHillHeight) {
							heights[(int)(x + i)][(int)(z + k)] =  maxHillHeight;
						} else if (height <= -maxHillHeight) {
							heights[(int)(x + i)][(int)(z + k)] =  -maxHillHeight;
						} else {
							if (x + i >= 0 && x + i <= VERTEX_COUNT && z + k >= 0 && z + k <= VERTEX_COUNT) {
								if (height > heights[(int)(x + i)][(int)(z + k)]) {
									heights[(int)(x + i)][(int)(z + k)] =  height;		
								}
							}
						}
					}
				}
			}
		}
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * getSize();
				vertices[vertexPointer*3+1] = heights[j][i];
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * getSize();
				normals[vertexPointer*3] = 0;
				normals[vertexPointer*3+1] = 1;
				normals[vertexPointer*3+2] = 0;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		this.model = loader.loadToVAOTerrain(vertices, textureCoords, normals, indices);
		//return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}
	
	private float getHeight(int x, int z, BufferedImage image){
		if(x<0 || x>=image.getHeight() || z<0 || z>=image.getHeight()){
			return 0;
		}
		float height = image.getRGB(x,z);
		height += MAX_PIXEL_COLOR/2f;
		height /= MAX_PIXEL_COLOR/2f;
		height *= MAX_HEIGHT;
		return height;
			
	}
	
	private float getHeight(int x, int z, HeightsGenerator generator){
		return generator.generateHeight(x, z);
			
	}
	
	public float getMaxHeight() {
		return maxHeight;
	}

	public float getMinHeight() {
		return minHeight;
	}
	
	public boolean ballInTerrain(Ball b) {
		float ballR = RealBall.RADIUS;
		float ballX = b.getPosition().x;
		float ballZ = b.getPosition().z;
		return  (ballX - ballR) < (this.x + Terrain.SIZE) && (ballX + ballR) > this.x &&
				(ballZ - ballR) < (this.z + Terrain.SIZE) && (ballZ + ballR) > this.z; // other size? e.g. heights.length?
	}

	public ArrayList<PhysicalFace> getCollidingFaces(Ball b) {
		//System.out.println("getCollidingFaces in Terrain is called (Terrain at " + this.x + "|" + this.z + ")");
		float ballR = Ball.RADIUS;
		float ballX = b.getPosition().x - this.x;
		float ballZ = b.getPosition().z - this.z;

		if ((b.getPosition().y - ballR) > this.maxHeight)
			return new ArrayList<PhysicalFace>(0);

		int leftX = (int) Math.floor(ballX - ballR);
		int rightX = (int) Math.ceil(ballX + ballR);
		if (leftX > rightX) {
			int temp = leftX;
			leftX = rightX;
			rightX = temp;
		}
		if (leftX < 0)
			leftX = 0;
		else if (leftX >= heights[0].length)
			leftX = heights[0].length - 1;
		if (rightX >= heights[0].length)
			rightX = heights[0].length - 1;
		else if (rightX < 0)
			rightX = 0;

		int upperZ = (int) Math.floor(ballZ - ballR);
		int lowerZ = (int) Math.ceil(ballZ + ballR);
		if (upperZ > lowerZ) {
			int temp = upperZ;
			upperZ = lowerZ;
			lowerZ = temp;
		}
		if (upperZ < 0)
			upperZ = 0;
		else if (upperZ >= heights.length)
			upperZ = heights.length - 1;
		if (lowerZ >= heights.length)
			lowerZ = heights.length - 1;
		else if (lowerZ < 0)
			lowerZ = 0;

		//System.out.println("leftX = " + leftX + ", rightX = " + rightX + ", upperZ = " + upperZ + ", lowerZ = " + lowerZ);

		Vector3f p1 = new Vector3f(0,0,0), p2 = new Vector3f(0,0,0), p3 = new Vector3f(0,0,0), normal = new Vector3f(0,0,0), v1 = new Vector3f(0,0,0), v2 = new Vector3f(0,0,0);

		ArrayList<PhysicalFace> collidingFaces = new ArrayList<PhysicalFace>();
		for (int i = leftX; i <= rightX && i < heights.length - 1; i++) {
			for (int j = upperZ; j <= lowerZ && j < heights[0].length - 1; j++) {
				//System.out.println("2 faces added at (" + i + "|" + j + ")");

				// upper left corner
				p1.set(i + this.x, this.heights[i][j], j + this.z);
				// lower left corner
				p2.set(i + this.x + 1, this.heights[i + 1][j], j + this.z);
				// upper right corner
				p3.set(i + this.x, this.heights[i][j + 1], j + this.z + 1);

				Vector3f.sub(p2, p1, v1);
				Vector3f.sub(p3, p1, v2);
				Vector3f.cross(v1, v2, normal);
				normal.normalise();
				collidingFaces.add(new PhysicalFace(normal, p1, p2, p3));
				//System.out.println("Face added: " + collidingFaces.get(collidingFaces.size() - 1) + ".");

				// upper right corner
				p1.set(i + this.x, this.heights[i][j + 1], j + this.z + 1);
				// lower left corner
				p2.set(i + this.x + 1, this.heights[i + 1][j], j + this.z);
				// lower right corner
				p3.set(i + this.x + 1, this.heights[i + 1][j + 1], j  + this.z + 1);

				Vector3f.sub(p2, p1, v1);
				Vector3f.sub(p3, p1, v2);
				Vector3f.cross(v1, v2, normal);
				normal.normalise();
				collidingFaces.add(new PhysicalFace(normal, p1, p2, p3));
				//System.out.println("Face added: " + collidingFaces.get(collidingFaces.size() - 1) + ".");
			}
		}
		//System.out.println("Number of colliding faces (in Terrain): " + collidingFaces.size());
		return collidingFaces;
	}
	
	private Vector3f calculateNormal(int x, int z, BufferedImage image){
		float heightL = getHeight(x-1, z, image);
		float heightR = getHeight(x+1, z, image);
		float heightD = getHeight(x, z-1, image);
		float heightU = getHeight(x, z+1, image);
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD-heightU);
		normal.normalise();
		return normal;
	}
	private Vector3f calculateNormal(int x, int z, HeightsGenerator generator){
		float heightL = getHeight(x-1, z, generator);
		float heightR = getHeight(x+1, z, generator);
		float heightD = getHeight(x, z-1, generator);
		float heightU = getHeight(x, z+1, generator);
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD-heightU);
		normal.normalise();
		return normal;
	}
	private Vector3f calculateNormal(int x, int z, float[][] heights){
		if (x < 1 || x + 1 >= heights.length || z < 1 || z + 1 >= heights[0].length) return new Vector3f(0,0,0);
		float heightL = heights[x-1][z];
		float heightR = heights[x+1][z];
		float heightD = heights[x][z-1];
		float heightU = heights[x][z+1];
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD-heightU);
		normal.normalise();
		return normal;
	}

	public static float getSize() {
		return SIZE;
	}
	
	public float getVertexCount() {
		return VERTEX_COUNT;
	}

}
