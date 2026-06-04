import java.awt.*;

public class Plane extends Object3D {
    private Vector3D normal;
    private double d;  // Constant in the equation: normal·P + d = 0


    public Plane(Vector3D point, Vector3D normal, Color color) {
        super(color, point);
        Vector3D n = Vector3D.normalize(normal);
        this.normal = n;
        this.d = -Vector3D.dot(n, point);
    }

    public Vector3D getNormal() {
        return normal;
    }

    public double getD() {
        return d;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        // dominator: n · dir
        double denom = Vector3D.dot(normal, ray.getDirection());

        // If denom is almost 0, the ray is parallel to the plane
        if (Math.abs(denom) < 1e-8) {
            return null;
        }

        // numerator: -(n·origin + d)
        double numerator = -(Vector3D.dot(normal, ray.getOrigin()) + d);
        double t = numerator / denom;

        // The point of intersection must be in front of the origin of the ray
        if (t < 0.001) {
            return null;
        }

        Vector3D position = ray.getPoint(t);

        // The returned normal always points in the same direction as the normal of the plane

        // For other type of shading:::

        // if (Vector3D.dot(normal, ray.getDirection()) > 0) {
        //     normal = Vector3D.scalar(normal, -1);
        // }

        return new Intersection(position, t, normal, this);
    }
}