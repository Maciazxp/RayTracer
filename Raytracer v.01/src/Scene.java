import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private List<Object3D> objects;

    public Scene() {
        setObjects(new ArrayList<>());
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


    public Intersection rayCast(Ray ray) {
        Intersection closestIntersection = null;
        double minDistance = Double.MAX_VALUE;

        for (Object3D obj : getObjects()) {
            Intersection intersection = obj.getIntersection(ray);

            if (intersection != null) {
                if (intersection.distance < minDistance) {
                    minDistance = intersection.distance;
                    closestIntersection = intersection;
                }
            }
        }
        return closestIntersection;
    }


}
