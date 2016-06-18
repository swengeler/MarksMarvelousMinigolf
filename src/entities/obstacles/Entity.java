package entities.obstacles;

import java.io.Serializable;
import java.util.ArrayList;

import entities.playable.Ball;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import physics.collisions.CollisionData;
import physics.collisions.PhysicalFace;
import models.TexturedModel;
import objConverter.ModelData;
import toolbox.LinearAlgebra;

public class Entity implements Serializable {

	protected TexturedModel model;
	protected CollisionData cdata;
	protected Vector3f position;
	protected Vector3f rotation;
	protected float scale;
	protected String type;

	protected int textureIndex = 0;
	protected boolean isCollidable = true;

	public Entity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
		this.model = model;
		this.position = position;
		this.rotation = new Vector3f(rotX, rotY, rotZ);
		System.out.println(rotY);
		this.scale = scale;
		this.type = type;
	}

	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
		this.rotation = new Vector3f(rotX, rotY, rotZ);
        this.scale = scale;
        this.type = type;
    }
	
	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
        this.rotation = new Vector3f(rotX, rotY, rotZ);
        this.scale = scale;
        this.type = "Undefined";
    }
	
	public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale, boolean isCollidable) {
        this.textureIndex = index;
        this.model = model;
        this.position = position;
        this.rotation = new Vector3f(rotX, rotY, rotZ);
        this.scale = scale;
        this.isCollidable = isCollidable;
        this.type = "Undefined";
    }

    public Entity(TexturedModel model, int index, ModelData data, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
    	this.textureIndex = index;
    	this.model = model;
		this.position = position;
		this.rotation = new Vector3f(rotX, rotY, rotZ);
		this.scale = scale;
        this.type = "not declared";
		createCollisionData(data);
	}

	public Entity(TexturedModel model, int index, ModelData data, Vector3f position, float rotX, float rotY, float rotZ, float scale, String type) {
		this.textureIndex = index;
		this.model = model;
		this.position = position;
		this.rotation = new Vector3f(rotX, rotY, rotZ);
		this.scale = scale;
        this.type = type;
		createCollisionData(data);
	}

	public boolean isCollidable(){
		return isCollidable;
	}

	protected void createCollisionData(ModelData data) {
		long before = System.currentTimeMillis();
		cdata = new CollisionData();
		Matrix4f transformationMatrix = LinearAlgebra.createTransformationMatrix(this.position, this.rotation.x, this.rotation.y, this.rotation.z, this.scale);
		Vector4f tfVector = new Vector4f(0,0,0,1f);
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), n1 = new Vector3f(), n2 = new Vector3f(), n3 = new Vector3f();
		Vector3f normal = new Vector3f(), v1 = new Vector3f(), v2 = new Vector3f();

		float[] ver = data.getVertices();
        float[] norm = data.getNormals();
		int[] ind = data.getIndices();

		float minX = Float.MAX_VALUE;
		float minY = minX;
		float minZ = minX;
		float maxX = -Float.MIN_VALUE;
		float maxY = maxX;
		float maxZ = maxX;

		float overallMin = Float.MAX_VALUE;
		float overallMax = -Float.MIN_VALUE;

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

			overallMin = Math.min(overallMin, Math.min(minX, Math.min(minY, minZ)));
			overallMax = Math.max(overallMax, Math.max(maxX, Math.max(maxY, maxZ)));

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

		cdata.setBoundingBox(overallMin, overallMin, overallMin, overallMax, overallMax, overallMax);
		cdata.setBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

		long after = System.currentTimeMillis();
		long difference = after - before;
		//System.out.println("Time to construct faces (for entity): " + difference + "\n");
	}

	public CollisionData getCollisionData() {
		return cdata;
	}

	public void printBBox() {
		cdata.getBoundingBox().print();
	}

	public ArrayList<PhysicalFace> getCollidingFaces(Ball b) {
		return cdata.getCollidingFaces(b);
	}

	public float getHighestPointOnLine(Vector3f p, Vector3f q) {
		return cdata.getHighestPointOnLine(p, q);
	}

	public boolean inBounds(Ball b) {
		return this.cdata.inBounds(b);
	}

	public boolean inHorizontalBounds(Ball b) {
		return this.cdata.inHorizontalBounds(b);
	}

	public boolean inHorizontalBounds(float x, float z) {
		return this.cdata.inHorizontalBounds(x, z);
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
		this.rotation.x += dx;
		this.rotation.y += dy;
		this.rotation.z += dz;

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
		return rotation.x;
	}

	public void setRotX(float rotX) {
		this.rotation.x = rotX;
	}

	public float getRotY() {
		return rotation.y;
	}

	public void setRotY(float rotY) {
		this.rotation.y = rotY;
	}

	public float getRotZ() {
		return rotation.z;
	}

	public void setRotZ(float rotZ) {
		this.rotation.z = rotZ;
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
