import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Raytracer {
    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        // -- CAMERA AND SCENE --

        // create camera
        Vector3D cameraPos = new Vector3D(0, 0.35, 2);
        Camera camera = new Camera(cameraPos, 45.0, width, height);

        // create the scene and set the camera (camera is part of the scene)
        Scene scene = new Scene(0.1, 50.0); // test (it works to render the spheres and triangle): near=0.1, far=50
        scene.setCamera(camera);


        //--LIGHTS--

        scene.addLight(new PointLight(
                new Vector3D(1, 1, 1.9),
                new Color(255, 255, 255),
                1.5
        ));

        scene.addLight(new PointLight(
                new Vector3D(0.45, 0.3, 1.8),
                new Color(255, 255, 255),
                1
        ));


        //  -- OBJECTS --

        // add an OBJ
        // Use the new OBJ.read overload that loads the MTL and textures
        List<Triangle> objTriangles = OBJ.read("triforce.obj", "triforce.mtl", 3.0);

        for (Triangle t : objTriangles) {// iterates over ALL triangles of the OBJ
            // this is repeated for each individual triangle
            // the triangles must be in the same relative position (if not, the object would break)
            // each vector3D (v0,v1,v2) are the vertices of a triangle
            // No Z offset needed camera is now positioned to face the object directly
            Vector3D v0 = new Vector3D(t.getVertex0().getX(), t.getVertex0().getY(), t.getVertex0().getZ());
            Vector3D v1 = new Vector3D(t.getVertex1().getX(), t.getVertex1().getY(), t.getVertex1().getZ());
            Vector3D v2 = new Vector3D(t.getVertex2().getX(), t.getVertex2().getY(), t.getVertex2().getZ());

            if (t.hasMaterial() && t.hasUVs() && t.hasVertexNormals()) {
                scene.addObject(new Triangle(v0, v1, v2,
                        t.getNormal0(), t.getNormal1(), t.getNormal2(),
                        t.getUV0(), t.getUV1(), t.getUV2(),
                        t.getMaterial()));
            } else if (t.hasMaterial() && t.hasUVs()) {
                scene.addObject(new Triangle(v0, v1, v2,
                        t.getUV0(), t.getUV1(), t.getUV2(),
                        t.getMaterial()));
            } else if (t.hasVertexNormals()) {
                scene.addObject(new Triangle(v0, v1, v2,
                        t.getNormal0(), t.getNormal1(), t.getNormal2(), Color.WHITE));
            } else {
                scene.addObject(new Triangle(v0, v1, v2, Color.WHITE));
            }
        }



        // -- RENDER --
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Camera cameraReal = scene.getCamera();

        long startTime = System.currentTimeMillis();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = cameraReal.getRay(x, y, width, height);
                Intersection intersect = scene.rayCast(ray);

                if (intersect != null) {
                    Color shadedColor = applyShading(intersect, scene, cameraReal);
                    image.setRGB(x, y, shadedColor.getRGB());
                } else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
            if (y % 60 == 0) {
                System.out.printf("Progress: %.1f%%\n", (y * 100.0 / height));
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Render complete in %.2f seconds\n", (endTime - startTime) / 1000.0);

        try {
            ImageIO.write(image, "png", new File("output.png"));
            System.out.println("Render complete: output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Shading: diffuse (Lambert) + specular (Phong) + shadows + ambient
    // Base color comes from the material texture if available, otherwise from the object color
    static Color applyShading(Intersection intersect, Scene scene, Camera camera) {
        Vector3D N = intersect.getNormal();

        // Resolve base color: sample texture if the triangle has one, else use flat color
        Color OC;
        Object3D obj = intersect.getObject();
        if (obj instanceof Triangle) {
            Triangle tri = (Triangle) obj;
            if (tri.hasMaterial() && tri.getMaterial().hasDiffuseTexture()) {
                OC = tri.getMaterial().sampleDiffuse(intersect.getU(), intersect.getV());
            } else {
                OC = obj.getColor();
            }
        } else {
            OC = obj.getColor();
        }

        double totalR = 0, totalG = 0, totalB = 0;

        for (Light light : scene.getLights()) {
            // Direction from intersection point towards the light
            Vector3D L = light.getDirectionToLight(intersect.getPosition());

            // -- SHADOW
            // Offset origin slightly along the normal to avoid self-intersection (shadow acne)
            Vector3D shadowOrigin = Vector3D.add(intersect.getPosition(), Vector3D.scalar(N, 0.001));
            Ray shadowRay = new Ray(shadowOrigin, L);

            // Point lights: block only occluders closer than the light itself
            // Directional lights: infinite distance, so MAX_VALUE
            double maxShadowDist = Double.MAX_VALUE;
            if (light instanceof PointLight) {
                maxShadowDist = Vector3D.magnitude(
                        Vector3D.sub(((PointLight) light).getPosition(), intersect.getPosition())
                );
            }

            if (scene.isOccluded(shadowRay, intersect.getObject(), maxShadowDist)) {
                continue; // point is in shadow — skip this light
            }

            // -- LIGHT FALLOFF
            // DirectionalLight: constant intensity
            // PointLight: intensity / d²
            double intensityAtPoint = light.getIntensityAt(intersect.getPosition());

            // -- DIFFUSE (Lambert)
            double nDotL = Vector3D.dot(N, L);
            double diffuse = Math.max(0.0, nDotL) * intensityAtPoint;

            // -- SPECULAR (Phong)
            double specular = 0.0;
            if (nDotL > 0) {
                // R = 2*(N·L)*N - L
                Vector3D R = Vector3D.normalize(Vector3D.sub(Vector3D.scalar(N, 2.0 * nDotL), L));
                // V = direction from intersection towards camera
                Vector3D V = Vector3D.normalize(Vector3D.sub(camera.getPosition(), intersect.getPosition()));
                double specPower = 32.0;
                specular = Math.pow(Math.max(0.0, Vector3D.dot(R, V)), specPower) * intensityAtPoint;
            }

            // Accumulate: diffuse uses object color, specular uses light color only
            Color LC = light.getColor();
            totalR += (LC.getRed()   / 255.0) * (OC.getRed()   / 255.0) * diffuse + (LC.getRed()   / 255.0) * specular;
            totalG += (LC.getGreen() / 255.0) * (OC.getGreen() / 255.0) * diffuse + (LC.getGreen() / 255.0) * specular;
            totalB += (LC.getBlue()  / 255.0) * (OC.getBlue()  / 255.0) * diffuse + (LC.getBlue()  / 255.0) * specular;
        }

        // -- AMBIENT: small constant term so shadowed areas aren't fully black
        double ambient = 0.025;
        totalR += (OC.getRed()   / 255.0) * ambient;
        totalG += (OC.getGreen() / 255.0) * ambient;
        totalB += (OC.getBlue()  / 255.0) * ambient;

        int r = (int) Math.min(255, totalR * 255);
        int g = (int) Math.min(255, totalG * 255);
        int b = (int) Math.min(255, totalB * 255);

        return new Color(r, g, b);
    }
}
