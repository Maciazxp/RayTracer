public class Intersection {
    public double distance;
    public Vector3D position;
    public Vector3D normal;
    public Object3D object;
    public double u, v; // interpolated UV coordinates (0 if no UVs)

    // Constructor without UVs (backward compatible)
    public Intersection(Vector3D position, double distance, Vector3D normal, Object3D object) {
        setPosition(position);
        setDistance(distance);
        setNormal(normal);
        setObject(object);
        this.u=0;
        this.v=0;
    }

    // Constructor with UVs
    public Intersection(Vector3D position, double distance, Vector3D normal, Object3D object, double u, double v) {
        setPosition(position);
        setDistance(distance);
        setNormal(normal);
        setObject(object);
        this.u = u;
        this.v = v;
    }


    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    public void setNormal(Vector3D normal) {
        this.normal = normal;
    }
    public void setObject(Object3D object) {
        this.object = object;
    }

    public double getDistance() {
        return distance;
    }
    public Vector3D getPosition() {
        return position;
    }
    public Vector3D getNormal() {
        return normal;
    }
    public Object3D getObject() {
        return object;
    }
    public double getU() {
        return u;
    }
    public double getV() {
        return v;
    }




}
