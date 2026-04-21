public class Ray {
    private Vector3D origin;
    private Vector3D direction;

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = Vector3D.normalize(direction);;
    }

    public Vector3D getPoint(double t) {
        return Vector3D.add(origin,Vector3D.scalar(direction,t));
    }

    public Vector3D getOrigin() {
        return origin;
    }
    public Vector3D getDirection() {
        return Vector3D.normalize(direction);
    }


}
