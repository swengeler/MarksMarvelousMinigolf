package entities;

import Physics.PhysicalFace;
import Physics.RotatingCollisionData;
import Physics.RotatingFace;
import models.TexturedModel;
import objConverter.ModelData;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import toolbox.Maths;

import java.util.ArrayList;

public class RotatingEntity extends Entity {

    private Vector3f rotVelocity;

    public RotatingEntity(TexturedModel model, int index, ModelData data, Vector3f position, Vector3f rotation, float scale, Vector3f rotVel) {
        super(model, index, data, position, rotation.x, rotation.y, rotation.z, scale);
        rotVelocity = new Vector3f(rotVel.x, rotVel.y, rotVel.z);
    }

    public RotatingEntity(TexturedModel model, int index, ModelData data, Vector3f position, Vector3f rotation, float scale, String type, Vector3f rotVel) {
        super(model, index, data, position, rotation.x, rotation.y, rotation.z, scale, type);
        rotVelocity = new Vector3f(rotVel.x, rotVel.y, rotVel.z);
    }

    @Override
    protected void createCollisionData(ModelData data) {
        long before = System.currentTimeMillis();
        cdata = new RotatingCollisionData();
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(this.position, this.rotation.x, this.rotation.y, this.rotation.z, this.scale);
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

        float overallMin = Float.MAX_VALUE;
        float overallMax = Float.MIN_VALUE;

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
            face = new RotatingFace(normal, p1, p2, p3);

            cdata.addFace(face);
        }
        cdata.setBoundingBox(overallMin, overallMin, overallMin, overallMax, overallMax, overallMax);

        long after = System.currentTimeMillis();
        long difference = after - before;
        System.out.println("Time to construct faces (for entity): " + difference + "\n");
    }

    @Override
    public ArrayList<PhysicalFace> getCollidingFaces(Ball b) {
        ((RotatingCollisionData) cdata).updateTFMatrix(Maths.createTransformationMatrix(position, rotation.x, rotation.y, rotation.z, scale));
        return cdata.getCollidingFaces(b);
    }

    @Override
    public boolean collides(Ball b) {
        ((RotatingCollisionData) cdata).updateTFMatrix(Maths.createTransformationMatrix(position, rotation.x, rotation.y, rotation.z, scale));
        return this.cdata.collides(b);
    }

}
