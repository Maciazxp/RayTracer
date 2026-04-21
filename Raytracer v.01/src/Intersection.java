public class Intersection {
    public double distance;
    public Vector3D position;
    public Vector3D normal;
    public Object3D object;

    public Intersection(Vector3D position, double distance, Vector3D normal, Object3D object) {
        setPosition(position);
        setDistance(distance);
        setNormal(normal);
        setObject(object);
    }

    public void setDistance(double distance){
        this.distance = distance;
    }
    public void setPosition(Vector3D position){
        this.position = position;
    }
    public void setNormal(Vector3D normal){
        this.normal = normal;
    }
    public void setObject(Object3D object){
        this.object = object;
    }




}
