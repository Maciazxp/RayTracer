public class Ray {
    private Vector3D origin;
    private Vector3D direction;      // Already normalized
    private Vector3D invDirection;
    private int[] sign;              // for AAB

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        // Normalize ONLY ONCE in the constructor
        this.direction = Vector3D.normalize(direction);

        // Pre-calculate the inverse of the direction (useful for AABB)
        this.invDirection = new Vector3D(
                1.0 / this.direction.getX(),
                1.0 / this.direction.getY(),
                1.0 / this.direction.getZ()
        );

        // Pre-calculate signs for AABB (optimized slabs algorithm)
        this.sign = new int[3];
        this.sign[0] = this.invDirection.getX() < 0 ? 1 : 0;
        this.sign[1] = this.invDirection.getY() < 0 ? 1 : 0;
        this.sign[2] = this.invDirection.getZ() < 0 ? 1 : 0;
    }

    public Vector3D getPoint(double t) {
        return Vector3D.add(origin, Vector3D.scalar(direction, t));
    }

    public Vector3D getOrigin() {
        return origin;
    }

    public Vector3D getDirection() {
        return direction;  // Ya está normalizado, no lo normalices de nuevo
    }

    public Vector3D getInvDirection() {
        return invDirection;
    }

    public int[] getSign() {
        return sign;
    }
}