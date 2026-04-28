

import java.awt.*;

public abstract class Object3D {
    private Color color;
    private Vector3D position;

    public Object3D(Color color, Vector3D position) {
        setPosition(position);
        setColor(color);
    }

    public Color getColor() {
        return color;
    }
    public Vector3D getPosition() {
        return position;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public abstract Intersection getIntersection(Ray ray);
}
