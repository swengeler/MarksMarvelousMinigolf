package entities;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Physics.CollisionData;
import Physics.PhysicalFace;
import models.TexturedModel;
import objConverter.ModelData;
import toolbox.Maths;

public class Entity {

	private TexturedModel model;
	private CollisionData cdata;
	private Vector3f position;
	protected Vector3f rotVel = new Vector3f();
	private float scale;
	private String type;

	private int textureIndex = 0;
	private boolean collidable = true;

	public Entity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
		this.model = model;
		this.position = position;
		this.rotVel.x = rotX;
		this.rotVel.y = rotY;
		System.out.println(rotY);
		this.rotVel.z = rotZ;
		this.scale = scale;
		this.type = type;
	}

	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
        this.rotVel.x = rotX;
        this.rotVel.y = rotY;
        this.rotVel.z = rotZ;
        this.scale = scale;
        this.type = type;
    }
	
	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
        this.rotVel.x = rotX;
        this.rotVel.y = rotY;
        this.rotVel.z = rotZ;
        this.scale = scale;
        this.type = "Undefined";
    }
	
	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale, boolean collidable) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
        this.rotVel.x = rotX;
        this.rotVel.y = rotY;
        this.rotVel.z = rotZ;
        this.scale = scale;
        this.collidable = collidable;
        this.type = "Undefined";
    }

    public Entity(TexturedModel model, int index, ModelData data, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
    	this.textureIndex = index;
    	this.model = model;
		this.position = position;
		this.rotVel.x = rotX;
		this.rotVel.y = rotY;
		this.rotVel.z = rotZ;
		this.scale = scale;
        this.type = "not declared";
		createCollisionData(data);
	}

	public Entity(TexturedModel model, int index, ModelData data, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
		this.textureIndex = index;
		this.model = model;
		this.position = position;
		this.rotVel.x = rotX;
		this.rotVel.y = rotY;
		this.rotVel.z = rotZ;
		this.scale = scale;
        this.type = type;
		createCollisionData(data);
	}

	public boolean isCollidable(){
		return collidable;
	}
	
	private void createCollisionData(ModelData data) {
		long before = System.currentTimeMillis();
		cdata = new CollisionData();
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(this.position,this.rotVel.x,this.rotVel.y,this.rotVel.z,this.scale);
		Vector4f tfVector = new Vector4f(0,0,0,1f);
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), n1 = new Vector3f(), n2 = new Vector3f(), n3 = new Vector3f();
		Vector3f normal = new Vector3f(), v1 = new Vector3f(), v2 = new Vector3f();

		float[] ver = data.getVertices();
        float[] norm = data.getNormals();
		int[] ind = data.getIndices();

		float minX = Float.MAX_VALUE;
		float minY = minX;
		float minZ = minX;
		float maxX = Float.MIN_VALUE;
		float maxY = maxX;
		float maxZ = maxX;

		PhysicalFace face;
		int[] curInd = new int[3];
		for (int i = 0; i < ind.length; i += 3) {
			curInd[0] = ind[i] * 3;
			curInd[1] = ind[i + 1] * 3;
			curInd[2] = ind[i + 2] * 3;

			// first vertex
			tfVector.set(ver[curInd[0]], ver[curInd[0] + 1], ver[curInd[0] + 2]);
			n1.set(norm[curInd[0]], norm[curInd[0] + 1], norm[curInd[0] + 2]);
			Matrix4f.transform(transformationMatrix, tfVector, tfVector);
			p1.set(tfVector.x, tfVector.y, tfVector.z);
			// second vertex
			tfVector.set(ver[curInd[1]], ver[curInd[1] + 1], ver[curInd[1] + 2]);
            n2.set(norm[curInd[1]], norm[curInd[1] + 1], norm[curInd[1] + 2]);
			Matrix4f.transform(transformationMatrix, tfVector, tfVector);
			p2.set(tfVector.x, tfVector.y, tfVector.z);
			// third vertex
			tfVector.set(ver[curInd[2]], ver[curInd[2] + 1], ver[curInd[2] + 2]);
            n3.set(norm[curInd[2]], norm[curInd[2] + 1], norm[curInd[2] + 2]);
			Matrix4f.transform(transformationMatrix, tfVector, tfVector);
			p3.set(tfVector.x, tfVector.y, tfVector.z);

			// adjusting max/min values
			minX = Math.min(minX, Math.min(p1.x, Math.min(p2.x, p3.x)));
			minY = Math.min(minY, Math.min(p1.y, Math.min(p2.y, p3.y)));
			minZ = Math.min(minZ, Math.min(p1.z, Math.min(p2.z, p3.z)));
			maxX = Math.max(maxX, Math.max(p1.x, Math.max(p2.x, p3.x)));
			maxY = Math.max(maxY, Math.max(p1.y, Math.max(p2.y, p3.y)));
			maxZ = Math.max(maxZ, Math.max(p1.z, Math.max(p2.z, p3.z)));

			// constructing a face from the three points p1, p2 and p3 and their resulting normal
			Vector3f.sub(p2, p1, v1);
			Vector3f.sub(p3, p1, v2);
			Vector3f.cross(v1, v2, normal);
            // Vector3f.add(n1, n2, normal);
            // Vector3f.add(normal, n3, normal);
			if (normal.lengthSquared() == 0) {
				normal.set(0, 1f, 0);
			}
			face = new PhysicalFace(normal, p1, p2, p3);

			cdata.addFace(face);
		}
		cdata.setBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		long after = System.currentTimeMillis();
		long difference = after - before;
		System.out.println("Time to construct faces (for entity): " + difference + "\n");
	}

	public CollisionData getCollisionData() {
		return cdata;
	}

	public void printBBox() {
		cdata.getBoundingBox().print();
	}

	public ArrayList<PhysicalFace> getCollidingFaces(Ball b) {
		return this.cdata.getCollidingFaces(b);
	}

	public boolean inBounds(Ball b) {
		return this.cdata.inBounds(b);
	}
	
	public boolean collides(Ball b) {
		return this.cdata.collides(b);
	}

	public float getTextureXOffset(){
		int column = textureIndex%model.getTexture().getNumberOfRows();
	    return (float)column/(float)model.getTexture().getNumberOfRows();
	}

	public float getTextureYOffset(){
		int row = textureIndex/model.getTexture().getNumberOfRows();
		return (float)row/(float)model.getTexture().getNumberOfRows();
	}


	public void increasePosition(float dx, float dy, float dz){
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public void increasePosition(Vector3f vec){
		this.position.x += vec.x;
		this.position.y += vec.y;
		this.position.z += vec.z;
	}

	public void increaseRotation(float dx, float dy, float dz){
		this.rotVel.x += dx;
		this.rotVel.y += dy;
		this.rotVel.z += dz;

	}

	public TexturedModel getModel() {
		return model;
	}

	public void setModel(TexturedModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getRotX() {
		return rotVel.x;
	}

	public void setRotX(float rotX) {
		this.rotVel.x = rotX;
	}

	public float getRotY() {
		return rotVel.y;
	}

	public void setRotY(float rotY) {
		this.rotVel.y = rotY;
	}

	public float getRotZ() {
		return rotVel.z;
	}

	public void setRotZ(float rotZ) {
		this.rotVel.z = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public String toString() {
		return "Entity of type \"" + type + "\" at (" + position.x + "|" + position.y + "|" + position.z + ")";
	}

}
