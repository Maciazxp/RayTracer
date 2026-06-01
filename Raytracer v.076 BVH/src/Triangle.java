import java.awt.*;

public class Triangle extends Object3D{
    private Vector3D vertex0, vertex1, vertex2;
    private Vector3D normal0, normal1, normal2; // per-vertex normals (for Phong)
    private double[] uv0, uv1, uv2;             // per-vertex UV coordinates
    private Material material;
    private static final double EPSILON = 1e-8;

    // Pre-calculated edges
    private Vector3D edge1, edge2;
    private Vector3D flatNormal;  // Pre-calculated flat normal

    //-- CONSTRUCTORS DEPENDING ON THE CASE
    // constructor for flat shading
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color, v0);
        this.vertex0 = v0;
        this.vertex1 = v1;
        this.vertex2 = v2;
        initEdgesAndNormal();
    }

    // Phong shading, no UVs (original constructor)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        super(color, v0);
        this.vertex0 = v0;
        this.vertex1 = v1;
        this.vertex2 = v2;
        this.normal0 = n0;
        this.normal1 = n1;
        this.normal2 = n2;
        initEdgesAndNormal();
    }

    // Phong shading + UVs + Material
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material) {
        super(Color.WHITE, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.normal0 = n0; this.normal1 = n1; this.normal2 = n2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
        initEdgesAndNormal();
    }

    // Flat shading + UVs + Material
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material) {
        super(Color.WHITE, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
        initEdgesAndNormal();
    }

    // Helper method to pre-calculate edge vectors and flat normal
    private void initEdgesAndNormal() {
        this.edge1 = Vector3D.sub(vertex1, vertex0);
        this.edge2 = Vector3D.sub(vertex2, vertex0);
        // Pre-calculate flat normal (already normalized)
        this.flatNormal = Vector3D.normalize(Vector3D.cross(edge1, edge2));
    }

    //--GETTERS
    public Vector3D getVertex0() { return vertex0; }
    public Vector3D getVertex1() { return vertex1; }
    public Vector3D getVertex2() { return vertex2; }
    public Vector3D getNormal0() { return normal0; }
    public Vector3D getNormal1() { return normal1; }
    public Vector3D getNormal2() { return normal2; }
    public double[] getUV0() { return uv0; }
    public double[] getUV1() { return uv1; }
    public double[] getUV2() { return uv2; }

    public boolean hasVertexNormals() {
        return normal0 != null && normal1 != null && normal2 != null;
    }

    public Material getMaterial() { return material; }
    public boolean hasMaterial()  { return material != null; }
    public boolean hasUVs()       { return uv0 != null && uv1 != null && uv2 != null; }

    @Override
    public Intersection getIntersection(Ray ray) {
        // Get ray direction (normalized)
        Vector3D D = ray.getDirection();

        // Use pre-calculated edge vectors
        // P = D x edge1
        Vector3D P = Vector3D.cross(D, edge1);

        // Determinant = edge2 · P
        double determinant = Vector3D.dot(edge2, P);

        // If determinant is near zero, ray is parallel to triangle
        if (Math.abs(determinant) < EPSILON) {
            return null;
        }

        double invDet = 1.0 / determinant;

        // T = O - v0
        Vector3D T = Vector3D.sub(ray.getOrigin(), vertex0);

        // u = invDet * (T · P)
        double u = invDet * Vector3D.dot(T, P);
        if (u < 0.0 || u > 1.0) {
            return null;
        }

        // Q = T x edge2
        Vector3D Q = Vector3D.cross(T, edge2);

        // v = invDet * (D · Q)
        double v = invDet * Vector3D.dot(D, Q);
        if (v < 0.0 || (u + v) > (1.0 + EPSILON)) {
            return null;
        }

        // t = invDet * (Q · edge1)
        double t = invDet * Vector3D.dot(Q, edge1);

        if (t < 0.001) {
            return null;
        }

        // Intersection found
        Vector3D position = ray.getPoint(t);
        Vector3D finalNormal;

        if (hasVertexNormals()) {
            // Phong shading: interpolate normals (already normalized in OBJ loader)
            double w = 1.0 - u - v;

            // No need to normalize every time if normals are already normalized
            // and we are interpolating (the result is almost normalized)
            finalNormal = Vector3D.add(
                    Vector3D.scalar(normal0, w),
                    Vector3D.add(Vector3D.scalar(normal1, v), Vector3D.scalar(normal2, u))
            );
            // Normalize only once at the end for Phong
            finalNormal = Vector3D.normalize(finalNormal);
        } else {
            // Flat shading: use pre-calculated normal
            finalNormal = flatNormal;
        }

        // UV interpolation
        if (hasUVs()) {
            double w = 1.0 - u - v;
            double interpU = uv0[0] * w + uv1[0] * v + uv2[0] * u;
            double interpV = uv0[1] * w + uv1[1] * v + uv2[1] * u;
            return new Intersection(position, t, finalNormal, this, interpU, interpV);
        }

        return new Intersection(position, t, finalNormal, this);
    }
}