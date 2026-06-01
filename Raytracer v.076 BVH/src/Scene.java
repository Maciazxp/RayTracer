import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private List<Object3D> objects;
    private List<Light> lights;

    private BVHNode bvh = null;


    // Clipping planes
    private double nearClip = 0.1;
    private double farClip = 100.0;


    public Scene() {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
    }

    public Scene(double nearClip, double farClip) {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.nearClip = nearClip;
        this.farClip = farClip;
    }


    //OBJECTS
    public List<Object3D> getObjects() {
        if(objects == null){
            objects = new ArrayList<>();
        }
        return objects;
    }
    public void setObjects(List<Object3D> objects) {
        this.objects = objects;
    }
    public void addObject(Object3D object){
        getObjects().add(object);
    }


    //SIMPLY WAY TO ADD AN OBJ
    public void addMesh(List<Triangle> triangles) {
        for (Triangle t : triangles) {
            //The triangles already come complete from OBJ.read()
            // only need to clone them if you want to keep transformations separate.
            objects.add(t);
        }
    }

    //WAY TO ADD AN OBJ WITH TRANSLATION
    // En Scene.java
    public void addMesh(List<Triangle> triangles, Vector3D translation, double scale) {
        for (Triangle t : triangles) {
            Vector3D v0 = Vector3D.add(Vector3D.scalar(t.getVertex0(), scale), translation);
            Vector3D v1 = Vector3D.add(Vector3D.scalar(t.getVertex1(), scale), translation);
            Vector3D v2 = Vector3D.add(Vector3D.scalar(t.getVertex2(), scale), translation);

            Triangle newTri;
            if (t.hasMaterial() && t.hasUVs() && t.hasVertexNormals()) {
                newTri = new Triangle(v0, v1, v2,
                        t.getNormal0(), t.getNormal1(), t.getNormal2(),
                        t.getUV0(), t.getUV1(), t.getUV2(), t.getMaterial());
            } else if (t.hasMaterial() && t.hasUVs()) {
                newTri = new Triangle(v0, v1, v2, t.getUV0(), t.getUV1(), t.getUV2(), t.getMaterial());
            } else if (t.hasVertexNormals()) {
                newTri = new Triangle(v0, v1, v2, t.getNormal0(), t.getNormal1(), t.getNormal2(), t.getColor());
            } else {
                newTri = new Triangle(v0, v1, v2, t.getColor());
            }
            objects.add(newTri);
        }
    }



    //LIGHTS
    public List<Light> getLights() {
        if (lights == null){
            lights = new ArrayList<>();
        }
        return lights;
    }
    public void setLights(List<Light> lights) {
        this.lights = lights;
    }
    public void addLight(Light light){
        getLights().add(light);
    }

    //CAMERA
    public Camera getCamera() {
        return camera;
    }
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    //CLIPPING
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


    // To build BVH after adding all objects
    public void buildBVH() {
        if (!objects.isEmpty()) {
            bvh = BVHNode.build(new java.util.ArrayList<>(objects));
            System.out.println("BVH built.");
        }
    }

    public Intersection rayCast(Ray ray) {
        if (bvh != null) return bvh.intersect(ray, nearClip, farClip);

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


    // --SHADOW RAY CAST
    // Checks if there is ANY object blocking the path from a point to a light.
    // originObject: the object that was hit by the camera ray — excluded to avoid self-shadowing.
    // maxDistance:  for point lights, we pass the distance to the light so we don't count
    // objects BEHIND the light as occluders. Pass Double.MAX_VALUE for directional lights.

    public boolean isOccluded(Ray shadowRay, Object3D originObject, double maxDistance) {
        if (bvh != null) {
            Intersection hit = bvh.intersect(shadowRay, 0.001, maxDistance);
            return hit != null && hit.getObject() != originObject;
        }
        for (Object3D obj : getObjects()) {
            // Skip the object that cast this shadow ray (same triangle = self-shadow bug)
            if (obj == originObject) {
                continue;
            }

            Intersection hit = obj.getIntersection(shadowRay);

            if (hit != null) {
                double t = hit.getDistance();
                // t > 0.001 to avoid floating-point self-intersection ("shadow acne")
                // t < maxDistance → only count occluders between the surface and the light
                if (t > 0.001 && t < maxDistance) {
                    return true; // something is blocking the light
                }
            }
        }
        return false; // nothing blocking means that point is lit
    }


}
