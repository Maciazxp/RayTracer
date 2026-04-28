import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private List<Object3D> objects;
    // Clipping planes
    private double nearClip = 0.1;
    private double farClip = 100.0;


    public Scene() {
        setObjects(new ArrayList<>());
    }
    public Scene(double nearClip, double farClip) {
        setObjects(new ArrayList<>());
        this.nearClip = nearClip;
        this.farClip = farClip;
    }

    public List<Object3D> getObjects() {
        if(objects == null){
            objects = new ArrayList<>();
        }
        return objects;
    }

    public void setObjects(List<Object3D> objects) {
        this.objects = objects;
    }

    public Camera getCamera() {
        return camera;
    }
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    public void addObject(Object3D object){
        getObjects().add(object);
    }

    public double getNearClip() {
        return nearClip;
    }
    public void setNearClip(double nearClip){
        this.nearClip = nearClip;
    }
    public double getFarClip(){
        return farClip;
    }
    public void setFarClip(double farClip){
        this.farClip  = farClip;
    }


    public Intersection rayCast(Ray ray) {
        Intersection closestIntersection = null;
        double minDistance = Double.MAX_VALUE;

        for (Object3D obj : getObjects()) {
            Intersection intersection = obj.getIntersection(ray);

            if (intersection != null) {
                double t = intersection.getDistance();

                // Clipping process of discard if it is out the frustum near or far
                if (t < nearClip || t > farClip) {
                    continue;
                }

                if (t < minDistance) {
                    minDistance = t;
                    closestIntersection = intersection;
                }
            }
        }
        return closestIntersection;
    }
}
