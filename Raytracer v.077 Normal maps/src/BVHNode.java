import java.util.List;

public class BVHNode {
    public AABB box;
    public BVHNode left, right;
    public List<Object3D> objects;

    // Builds the tree recursively
    public static BVHNode build(List<Object3D> objs) {
        BVHNode node = new BVHNode();
        node.box = computeAABB(objs);

        if (objs.size() <= 8) { // leaf: maximum 8 objects
            node.objects = objs;
            return node;
        }

        // Split by the longest axis of the box
        Vector3D size = Vector3D.sub(node.box.max, node.box.min);
        int axis = 0;
        if (size.getY() > size.getX()) axis = 1;
        if (size.getZ() > (axis == 0 ? size.getX() : size.getY())) axis = 2;

        final int ax = axis;
        objs.sort((a, b) -> {
            double ca = centroid(a, ax);
            double cb = centroid(b, ax);
            return Double.compare(ca, cb);
        });

        int mid = objs.size() / 2;
        node.left  = build(objs.subList(0, mid));
        node.right = build(objs.subList(mid, objs.size()));
        return node;
    }

    // Recursive ray-tree intersection
    public Intersection intersect(Ray ray, double nearClip, double farClip) {
        if (!box.intersects(ray)) return null; // if the box is not touched, skip

        if (objects != null) { // leaf node → check triangles
            Intersection closest = null;
            double minDist = Double.MAX_VALUE;
            for (Object3D obj : objects) {
                Intersection hit = obj.getIntersection(ray);
                if (hit != null && hit.getDistance() > nearClip
                        && hit.getDistance() < farClip
                        && hit.getDistance() < minDist) {
                    minDist = hit.getDistance();
                    closest = hit;
                }
            }
            return closest;
        }

        // Internal node → search in both children and keep the closest one
        Intersection l = left  != null ? left.intersect(ray, nearClip, farClip)  : null;
        Intersection r = right != null ? right.intersect(ray, nearClip, farClip) : null;

        if (l == null) return r;
        if (r == null) return l;
        return l.getDistance() < r.getDistance() ? l : r;
    }

    // Computes the AABB of a list of objects
    private static AABB computeAABB(List<Object3D> objs) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Object3D obj : objs) {
            AABB b = getAABB(obj);
            minX = Math.min(minX, b.min.getX()); minY = Math.min(minY, b.min.getY()); minZ = Math.min(minZ, b.min.getZ());
            maxX = Math.max(maxX, b.max.getX()); maxY = Math.max(maxY, b.max.getY()); maxZ = Math.max(maxZ, b.max.getZ());
        }
        return new AABB(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    private static AABB getAABB(Object3D obj) {
        if (obj instanceof Triangle t) {
            double minX = Math.min(t.getVertex0().getX(), Math.min(t.getVertex1().getX(), t.getVertex2().getX()));
            double minY = Math.min(t.getVertex0().getY(), Math.min(t.getVertex1().getY(), t.getVertex2().getY()));
            double minZ = Math.min(t.getVertex0().getZ(), Math.min(t.getVertex1().getZ(), t.getVertex2().getZ()));
            double maxX = Math.max(t.getVertex0().getX(), Math.max(t.getVertex1().getX(), t.getVertex2().getX()));
            double maxY = Math.max(t.getVertex0().getY(), Math.max(t.getVertex1().getY(), t.getVertex2().getY()));
            double maxZ = Math.max(t.getVertex0().getZ(), Math.max(t.getVertex1().getZ(), t.getVertex2().getZ()));
            return new AABB(new Vector3D(minX - 1e-4, minY - 1e-4, minZ - 1e-4),
                    new Vector3D(maxX + 1e-4, maxY + 1e-4, maxZ + 1e-4));
        }
        // For spheres or other objects
        Vector3D p = obj.getPosition();
        double r = (obj instanceof Sphere s) ? s.getRadius() : 1.0;
        return new AABB(new Vector3D(p.getX()-r, p.getY()-r, p.getZ()-r),
                new Vector3D(p.getX()+r, p.getY()+r, p.getZ()+r));
    }

    private static double centroid(Object3D obj, int axis) {
        AABB b = getAABB(obj);
        double[] lo = {b.min.getX(), b.min.getY(), b.min.getZ()};
        double[] hi = {b.max.getX(), b.max.getY(), b.max.getZ()};
        return (lo[axis] + hi[axis]) * 0.5;
    }
}