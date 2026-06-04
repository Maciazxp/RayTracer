import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private List<Object3D> objects;
    private List<Light> lights;
    private BVHNode bvh = null;

    private double nearClip = 0.1;
    private double farClip  = 100.0;

    public Scene() {
        this.objects = new ArrayList<>();
        this.lights  = new ArrayList<>();
    }

    public Scene(double nearClip, double farClip) {
        this.objects  = new ArrayList<>();
        this.lights   = new ArrayList<>();
        this.nearClip = nearClip;
        this.farClip  = farClip;
    }

    // -- OBJECTS -------------------------------------------------------------------

    public List<Object3D> getObjects() {
        if (objects == null) objects = new ArrayList<>();
        return objects;
    }
    public void setObjects(List<Object3D> objects) { this.objects = objects; }
    public void addObject(Object3D object)          { getObjects().add(object); }

     //Add a mesh as-is (no transform)
    public void addMesh(List<Triangle> triangles) {
        for (Triangle t : triangles) objects.add(t);
    }


      //Add a mesh with translation + scale.
      //Normals and UVs are preserved; vertex positions are scaled then translated.
    public void addMesh(List<Triangle> triangles, Vector3D translation, double scale) {
        addMesh(triangles, translation, scale, null);
    }


     // Add a mesh with translation + scale + BlinnPhong light behaviour.

     // The BlinnPhongMaterial is assigned to every new triangle so applyShading()
     // can use it. The MTL textures (diffuse + normal map) already stored in each
     // triangle's Material are kept intact.
    public void addMesh(List<Triangle> triangles, Vector3D translation,
                        double scale, BlinnPhongMaterial bp) {
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
                newTri = new Triangle(v0, v1, v2,
                        t.getUV0(), t.getUV1(), t.getUV2(), t.getMaterial());
            } else if (t.hasVertexNormals()) {
                newTri = new Triangle(v0, v1, v2,
                        t.getNormal0(), t.getNormal1(), t.getNormal2(), t.getColor());
            } else {
                newTri = new Triangle(v0, v1, v2, t.getColor());
            }

            // Attach light behaviour if provided
            if (bp != null) newTri.setBPMaterial(bp);

            objects.add(newTri);
        }
    }

    // -- LIGHTS -------------------------------------------------------------------

    public List<Light> getLights() {
        if (lights == null) lights = new ArrayList<>();
        return lights;
    }
    public void setLights(List<Light> lights) { this.lights = lights; }
    public void addLight(Light light)          { getLights().add(light); }

    // -- CAMERA -------------------------------------------------------------------

    public Camera getCamera()              { return camera; }
    public void setCamera(Camera camera)   { this.camera = camera; }

    // -- CLIPPING -------------------------------------------------------------------

    public double getNearClip()              { return nearClip; }
    public void   setNearClip(double v)      { this.nearClip = v; }
    public double getFarClip()               { return farClip; }
    public void   setFarClip(double v)       { this.farClip  = v; }

    // -- BVH -------------------------------------------------------------------

    public void buildBVH() {
        if (!objects.isEmpty()) {
            bvh = BVHNode.build(new ArrayList<>(objects));
            System.out.println("BVH built.");
        }
    }

    // -- RAY CAST -------------------------------------------------------------------

    public Intersection rayCast(Ray ray) {
        if (bvh != null) return bvh.intersect(ray, nearClip, farClip);

        Intersection closest = null;
        double minDist = Double.MAX_VALUE;

        for (Object3D obj : getObjects()) {
            Intersection hit = obj.getIntersection(ray);
            if (hit == null) continue;
            double t = hit.getDistance();
            if (t < nearClip || t > farClip || t >= minDist) continue;
            minDist = t;
            closest = hit;
        }
        return closest;
    }

    // -- SHADOW RAY -------------------------------------------------------------------

    public boolean isOccluded(Ray shadowRay, Object3D originObject, double maxDistance) {
        if (bvh != null) {
            Intersection hit = bvh.intersect(shadowRay, 0.001, maxDistance);
            return hit != null && hit.getObject() != originObject;
        }
        for (Object3D obj : getObjects()) {
            if (obj == originObject) continue;
            Intersection hit = obj.getIntersection(shadowRay);
            if (hit != null && hit.getDistance() > 0.001 && hit.getDistance() < maxDistance)
                return true;
        }
        return false;
    }
}
