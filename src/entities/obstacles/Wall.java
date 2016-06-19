package entities.obstacles;

import models.TexturedModel;
import objConverter.ModelData;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import toolbox.LinearAlgebra;

public class Wall extends Entity {

    private Vector2f p1, p2;

    public Wall(Vector2f p1, Vector2f p2, TexturedModel model, int textureIndex, ModelData data) {
        super(model, textureIndex, new Vector3f(0, 0, 0), 0, 0, 0, 1, "wall");
        this.p1 = new Vector2f(p1.x, p1.y);
        this.p2 = new Vector2f(p2.x, p2.y);
        // then calculate the rotation and scale necessary to build the wall
        rotateAndScale();
        createCollisionData(data, LinearAlgebra.createTransformationMatrixWall(this.position, this.rotation.x, this.rotation.y, this.rotation.z, this.scale));
    }

    private void rotateAndScale() {
        Vector2f difference = Vector2f.sub(p2, p1, null);
        System.out.println("Difference: " + difference);
        this.scale = difference.length();
        System.out.println("Scale: " + this.scale);
        // the angle of rotation around the y-axis is determined by the angle between the difference-vector and the z-axis, since the wall model goes along that axis
        Vector2f zAxis = new Vector2f(0, 1);
        float angle = Vector2f.angle(zAxis, difference);
        if (p1.x > p2.x)
            angle = (float) Math.min(angle, Math.PI - angle);
        this.rotation.y = (float) Math.toDegrees(angle);
        System.out.println("Angle to z-axis: " + this.rotation.y);
        // the position of the wall can also be determined by the two endpoints
        difference.scale(0.5f);
        Vector2f midPoint = Vector2f.add(p1, difference, null);
        System.out.println("Midpoint: " + midPoint);
        this.position.set(midPoint.x, 0, midPoint.y);
    }

    public Vector2f getP1() {
        return p1;
    }

    public Vector2f getP2() {
        return p2;
    }

}
