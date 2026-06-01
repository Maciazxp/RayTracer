import java.awt.*;

public class Triangle extends Object3D{
    private Vector3D normal;
    private Vector3D vertex0, vertex1, vertex2;
    private Vector3D normal0, normal1, normal2; // per-vertex normals (for Phong)
    private double[] uv0, uv1, uv2;             // per-vertex UV coordinates
    private Material material;
    private static final double EPSILON = 1e-8;

    //-- CONSTRUCTORS DEPENDING ON THE CASE
    // constructor for flat shading
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color, v0);
        this.vertex0 = v0;
        this.vertex1 = v1;
        this.vertex2 = v2;
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
    }
    // Phong shading + UVs + Material
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material) {
        super(Color.WHITE, v0); // color ignored when material has a texture
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.normal0 = n0; this.normal1 = n1; this.normal2 = n2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
    }
    // Flat shading + UVs + Material
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material) {
        super(Color.WHITE, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
    }

    //--GETTERS
    //normals
    public Vector3D getVertex0() { return vertex0; }
    public Vector3D getVertex1() { return vertex1; }
    public Vector3D getVertex2() { return vertex2; }
    //vertex
    public Vector3D getNormal0() { return normal0; }
    public Vector3D getNormal1() { return normal1; }
    public Vector3D getNormal2() { return normal2; }
    // UVs
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
        //Möller-Trumbore algorithm

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

            finalNormal = Vector3D.normalize(
                    Vector3D.add(
                            Vector3D.scalar(normal0, w),
                            Vector3D.add(Vector3D.scalar(normal1, v), Vector3D.scalar(normal2, u))
                    )
            );
        } else {
            // Flat shading fallback if normals are missing
            finalNormal = Vector3D.normalize(Vector3D.cross(v1v0, v2v0));
        }
// UV: interpolate with barycentric coords if UVs exist
        if (hasUVs()) {
            double w = 1.0 - u - v;
            double interpU = uv0[0] * w + uv1[0] * v + uv2[0] * u;
            double interpV = uv0[1] * w + uv1[1] * v + uv2[1] * u;
            return new Intersection(position, t, finalNormal, this, interpU, interpV);
        }

        return new Intersection(position, t, finalNormal, this);
    }
}