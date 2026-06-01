public class AABB {
    public Vector3D min, max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    // Expands the box to include another box
    public AABB expand(AABB other) {
        return new AABB(
                new Vector3D(Math.min(min.getX(), other.min.getX()),
                        Math.min(min.getY(), other.min.getY()),
                        Math.min(min.getZ(), other.min.getZ())),
                new Vector3D(Math.max(max.getX(), other.max.getX()),
                        Math.max(max.getY(), other.max.getY()),
                        Math.max(max.getZ(), other.max.getZ()))
        );
    }

    // Ray-box intersection test (slab method)
    public boolean intersects(Ray ray) {
        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        double[] origin = {ray.getOrigin().getX(), ray.getOrigin().getY(), ray.getOrigin().getZ()};
        double[] invDir = {ray.getInvDirection().getX(), ray.getInvDirection().getY(), ray.getInvDirection().getZ()};
        int[] sign = ray.getSign();

        double[] bmin = {min.getX(), min.getY(), min.getZ()};
        double[] bmax = {max.getX(), max.getY(), max.getZ()};

        for (int i = 0; i < 3; i++) {
            double t1 = (bmin[i] - origin[i]) * invDir[i];
            double t2 = (bmax[i] - origin[i]) * invDir[i];

            //use sign precalculated to ordered without branches
            if (sign[i] == 1) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }

            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);

            if (tmin > tmax) return false;
        }
        return tmax > 0;
    }
}
