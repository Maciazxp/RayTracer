
import java.awt.*;

public class Sphere extends Object3D {
    double radius;

    public Sphere(Vector3D position, double radius, Color color) {
        super(color, position);
        setRadius(radius);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        Vector3D L = Vector3D.sub(getPosition(), ray.getOrigin());
        double tca = Vector3D.dot(L, ray.getDirection());
        double d2 = Vector3D.dot(L, L) - tca * tca;

        if (d2 > radius * radius) {
            return null;
        }

        double thc = Math.sqrt(radius * radius - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;

        double t = -1;
        if (t0 > 0.001) {
            t = t0;
        } else if (t1 > 0.001) {
            t = t1;
        } else {
            return null;
        }
        Vector3D position = ray.getPoint(t);
        Vector3D normal = Vector3D.normalize(Vector3D.sub(position, getPosition()));

        return new Intersection(position, t, normal, this);
    }

    /*
    public double getIntersection (Ray ray){
        Vector3D L = Vector3D.sub(getPosition(),ray.getOrigin());
        double tca = Vector3D.dot(L,ray.getDirection());
        double d2 = Vector3D.dot(L,L)- tca*tca;
        if (d2 > radius*radius){
            return -1;
        }
        double thc = Math.sqrt((radius*radius)-d2);

        double t0=tca-thc;
        double t1=tca+thc;
        if (t0 < 0 && t1 < 0)
            return -1;
        if (t0 < 0)
            return t1;
        if (t1 < 0)
            return t0;
        return Math.min(t0, t1);
        }

*/
    }
