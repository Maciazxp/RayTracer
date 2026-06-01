import java.awt.*;

public class Triangle extends Object3D {
    private Vector3D vertex0, vertex1, vertex2;
    private Vector3D normal0, normal1, normal2; // per-vertex normals (for Phong shading)
    private double[] uv0, uv1, uv2;             // per-vertex UV coordinates
    private Material material;                   // texture data from MTL (diffuse + normal map)
    private BlinnPhongMaterial bpMaterial;       // light behaviour (ambient, diffuse, specular…)
    private static final double EPSILON = 1e-8;

    // Pre-calculated
    private Vector3D edge1, edge2;
    private Vector3D flatNormal;
    private Vector3D tangent;
    private Vector3D bitangent;

    // ── CONSTRUCTORS ─────────────────────────────────────────────────────────────

    // Flat shading, no UVs
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        initEdgesAndNormal();
    }

    // Phong shading, no UVs
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        super(color, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.normal0 = n0; this.normal1 = n1; this.normal2 = n2;
        initEdgesAndNormal();
    }

    // Phong shading + UVs + MTL Material
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

    // Flat shading + UVs + MTL Material
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material) {
        super(Color.WHITE, v0);
        this.vertex0 = v0; this.vertex1 = v1; this.vertex2 = v2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
        initEdgesAndNormal();
    }

    // ── INIT ─────────────────────────────────────────────────────────────────────

    private void initEdgesAndNormal() {
        this.edge1 = Vector3D.sub(vertex1, vertex0);
        this.edge2 = Vector3D.sub(vertex2, vertex0);
        this.flatNormal = Vector3D.normalize(Vector3D.cross(edge1, edge2));

        if (uv0 != null && uv1 != null && uv2 != null) {
            double du1 = uv1[0] - uv0[0], dv1 = uv1[1] - uv0[1];
            double du2 = uv2[0] - uv0[0], dv2 = uv2[1] - uv0[1];
            double denom = du1 * dv2 - du2 * dv1;

            if (Math.abs(denom) > 1e-8) {
                double f = 1.0 / denom;
                this.tangent = Vector3D.normalize(new Vector3D(
                        f * (dv2 * edge1.getX() - dv1 * edge2.getX()),
                        f * (dv2 * edge1.getY() - dv1 * edge2.getY()),
                        f * (dv2 * edge1.getZ() - dv1 * edge2.getZ())));
                this.bitangent = Vector3D.normalize(new Vector3D(
                        f * (-du2 * edge1.getX() + du1 * edge2.getX()),
                        f * (-du2 * edge1.getY() + du1 * edge2.getY()),
                        f * (-du2 * edge1.getZ() + du1 * edge2.getZ())));
            } else {
                this.tangent   = Vector3D.normalize(edge1);
                this.bitangent = Vector3D.normalize(Vector3D.cross(flatNormal, tangent));
            }
        }
    }

    // ── GETTERS ──────────────────────────────────────────────────────────────────

    public Vector3D getVertex0()  { return vertex0; }
    public Vector3D getVertex1()  { return vertex1; }
    public Vector3D getVertex2()  { return vertex2; }
    public Vector3D getNormal0()  { return normal0; }
    public Vector3D getNormal1()  { return normal1; }
    public Vector3D getNormal2()  { return normal2; }
    public double[] getUV0()      { return uv0; }
    public double[] getUV1()      { return uv1; }
    public double[] getUV2()      { return uv2; }
    public Vector3D getTangent()  { return tangent; }
    public Vector3D getBitangent(){ return bitangent; }

    // MTL texture material
    public Material getMaterial() { return material; }
    public boolean hasMaterial()  { return material != null; }

    // Blinn-Phong light behaviour material
    public BlinnPhongMaterial getBPMaterial() { return bpMaterial; }
    public boolean hasBPMaterial()            { return bpMaterial != null; }

    /** Assign a BlinnPhongMaterial after construction (used by Scene.addMesh). */
    public void setBPMaterial(BlinnPhongMaterial bp) { this.bpMaterial = bp; }

    public boolean hasVertexNormals() { return normal0 != null && normal1 != null && normal2 != null; }
    public boolean hasTBN()           { return tangent  != null && bitangent != null; }
    public boolean hasUVs()           { return uv0 != null && uv1 != null && uv2 != null; }

    // ── INTERSECTION ─────────────────────────────────────────────────────────────

    @Override
    public Intersection getIntersection(Ray ray) {
        Vector3D D = ray.getDirection();
        Vector3D P = Vector3D.cross(D, edge1);
        double det = Vector3D.dot(edge2, P);
        if (Math.abs(det) < EPSILON) return null;

        double invDet = 1.0 / det;
        Vector3D T = Vector3D.sub(ray.getOrigin(), vertex0);

        double u = invDet * Vector3D.dot(T, P);
        if (u < 0.0 || u > 1.0) return null;

        Vector3D Q = Vector3D.cross(T, edge2);
        double v = invDet * Vector3D.dot(D, Q);
        if (v < 0.0 || (u + v) > (1.0 + EPSILON)) return null;

        double t = invDet * Vector3D.dot(Q, edge1);
        if (t < 0.001) return null;

        Vector3D position = ray.getPoint(t);
        Vector3D finalNormal;

        if (hasVertexNormals()) {
            double w = 1.0 - u - v;
            finalNormal = Vector3D.normalize(Vector3D.add(
                    Vector3D.scalar(normal0, w),
                    Vector3D.add(Vector3D.scalar(normal1, v), Vector3D.scalar(normal2, u))));
        } else {
            finalNormal = flatNormal;
        }

        if (hasUVs()) {
            double w = 1.0 - u - v;
            double interpU = uv0[0] * w + uv1[0] * v + uv2[0] * u;
            double interpV = uv0[1] * w + uv1[1] * v + uv2[1] * u;
            return new Intersection(position, t, finalNormal, this, interpU, interpV);
        }

        return new Intersection(position, t, finalNormal, this);
    }
}
