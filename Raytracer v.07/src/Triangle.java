import java.awt.*;

public class Triangle extends Object3D{
    private Vector3D normal;
    private Vector3D vertex0, vertex1, vertex2;
    private Vector3D normal0, normal1, normal2; // per-vertex normals (for Phong)
    private static final double EPSILON = 1e-8;

    // constructor for flat shading
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color, v0); // position = v0
        this.vertex0 = v0;
        this.vertex1 = v1;
        this.vertex2 = v2;
        this.normal0 = null;
        this.normal1 = null;
        this.normal2 = null;
    }

    // constructor for phong shading
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        super(color, v0);
        this.vertex0 = v0;
        this.vertex1 = v1;
        this.vertex2 = v2;
        this.normal0 = n0;
        this.normal1 = n1;
        this.normal2 = n2;
    }

    public Vector3D getVertex0() { return vertex0; }
    public Vector3D getVertex1() { return vertex1; }
    public Vector3D getVertex2() { return vertex2; }

    // Getters for normals needed in Raytracer
    public Vector3D getNormal0() { return normal0; }
    public Vector3D getNormal1() { return normal1; }
    public Vector3D getNormal2() { return normal2; }

    public boolean hasVertexNormals() {
        return normal0 != null && normal1 != null && normal2 != null;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        // Möller-Trumbore algorithm

        // edge vectors from v0
        Vector3D v1v0 = Vector3D.sub(vertex1, vertex0); // v1 - v0
        Vector3D v2v0 = Vector3D.sub(vertex2, vertex0); // v2 - v0

        // P = D x v1v0
        Vector3D P = Vector3D.cross(ray.getDirection(), v1v0);

        // calculation for determinant = v2v0 · P
        double determinant = Vector3D.dot(v2v0, P);

        // If determinant is near zero, ray is parallel to triangle
        if (Math.abs(determinant) < EPSILON) {
            return null;
        }

        double invDet = 1.0 / determinant;

        // T = O - v0
        Vector3D T = Vector3D.sub(ray.getOrigin(), vertex0);

        // u = invDet x (T · P)
        double u = invDet * Vector3D.dot(T, P);

        // if u < 0 || u > 1 means that there's no intersection
        if (u < 0.0 || u > 1.0) {
            return null;
        }

        // Q = T x v2v0
        Vector3D Q = Vector3D.cross(T, v2v0);

        // v = invDet x (D · Q)
        double v = invDet * Vector3D.dot(ray.getDirection(), Q);

        // if v < 0 || (u + v) > (1 + epsilon) means that there's no intersection
        if (v < 0.0 || (u + v) > (1.0 + EPSILON)) {
            return null;
        }

        // t = invDet x (Q · v1v0) is for distance
        double t = invDet * Vector3D.dot(Q, v1v0);

        if (t < 0.001) {
            return null; // intersection behind the ray
        }

        // for the intersection point and normal
        Vector3D position = ray.getPoint(t);
        Vector3D finalNormal;

        if (hasVertexNormals()) {
            // Phong: interpolate normals using barycentric coordinates (u, v)
            // w = 1 - u - v  (weight of vertex0)
            double w = 1.0 - u - v;

            // Multiply normal1 with v and normal2 with u
            Vector3D n0_w = Vector3D.scalar(normal0, w);
            Vector3D n1_v = Vector3D.scalar(normal1, v);
            Vector3D n2_u = Vector3D.scalar(normal2, u);

            finalNormal = Vector3D.add(n0_w, Vector3D.add(n1_v, n2_u));
            finalNormal = Vector3D.normalize(finalNormal);

        } else {
            // Flat shading fallback if normals are missing
            finalNormal = Vector3D.normalize(Vector3D.cross(v1v0, v2v0));
        }

        return new Intersection(position, t, finalNormal, this);
    }
}