import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;


public class Raytracer {
    public static void main(String[] args) {
        int width = 1920;  //4096
        int height = 1080; //2160

        // -- CAMERA AND SCENE --
        Vector3D cameraPos = new Vector3D(0, 0.40, 2);
        Camera camera = new Camera(cameraPos, 40.0, width, height);

        Scene scene = new Scene(0.1, 50.0);
        scene.setCamera(camera);

        //-- LIGHTS --

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


        // -- OBJECTS --
        List<Triangle> baseMesh = OBJ.read("shield.obj", "shield.mtl", 1.0);
        scene.addMesh(baseMesh, new Vector3D(0, 0, 0), 3.0);

        List<Triangle> teapot = OBJ.read("SmallTeapot.obj", Color.cyan, 1.0);
        scene.addMesh(teapot, new Vector3D(0.45, 0.1, 0.7), 0.35);


        // -- BUILD BVH --
        scene.buildBVH();

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

    // Shading: diffuse (Lambert) + specular (Phong) + shadows + ambient + normal mapping
    static Color applyShading(Intersection intersect, Scene scene, Camera camera) {

        // --- NORMAL (with normal map if available) ---
        Vector3D N = intersect.getNormal(); // base normal (Phong-interpolated or flat)

        Object3D obj = intersect.getObject();
        if (obj instanceof Triangle tri) {
            if (tri.hasMaterial() && tri.getMaterial().hasNormalMap()
                    && tri.hasUVs() && tri.hasTBN()) {

                // 1. Sample the tangent-space normal from the map
                Vector3D tsNormal = tri.getMaterial().sampleNormal(intersect.getU(), intersect.getV());

                // 2. Build TBN matrix using the triangle's tangent and bitangent
                //    Re-orthogonalize tangent against the shading normal (Gram-Schmidt)
                //    so the TBN is consistent with Phong-interpolated normals
                Vector3D T = tri.getTangent();
                T = Vector3D.normalize(Vector3D.sub(T, Vector3D.scalar(N, Vector3D.dot(N, T))));
                Vector3D B = tri.getBitangent();

                // 3. Transform tangent-space normal → world space
                //    N_world = tsNormal.x*T + tsNormal.y*B + tsNormal.z*N
                Vector3D perturbedN = Vector3D.add(
                        Vector3D.scalar(T, tsNormal.getX()),
                        Vector3D.add(
                                Vector3D.scalar(B, tsNormal.getY()),
                                Vector3D.scalar(N, tsNormal.getZ())
                        )
                );
                N = Vector3D.normalize(perturbedN);
            }
        }

        // --- BASE COLOR ---
        Color OC;
        if (obj instanceof Triangle tri) {
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
            Vector3D L = light.getDirectionToLight(intersect.getPosition());

            // -- SHADOW
            Vector3D shadowOrigin = Vector3D.add(intersect.getPosition(), Vector3D.scalar(N, 0.001));
            Ray shadowRay = new Ray(shadowOrigin, L);

            double maxShadowDist = Double.MAX_VALUE;
            if (light instanceof PointLight) {
                maxShadowDist = Vector3D.magnitude(
                        Vector3D.sub(((PointLight) light).getPosition(), intersect.getPosition())
                );
            }

            if (scene.isOccluded(shadowRay, intersect.getObject(), maxShadowDist)) {
                continue;
            }

            double intensityAtPoint = light.getIntensityAt(intersect.getPosition());

            // -- DIFFUSE (Lambert)
            double nDotL = Vector3D.dot(N, L);
            double diffuse = Math.max(0.0, nDotL) * intensityAtPoint;

            // -- SPECULAR (Phong)
            double specular = 0.0;
            if (nDotL > 0) {
                Vector3D R = Vector3D.normalize(Vector3D.sub(Vector3D.scalar(N, 2.0 * nDotL), L));
                Vector3D V = Vector3D.normalize(Vector3D.sub(camera.getPosition(), intersect.getPosition()));
                double specPower = 32.0;
                specular = Math.pow(Math.max(0.0, Vector3D.dot(R, V)), specPower) * intensityAtPoint;
            }

            Color LC = light.getColor();
            totalR += (LC.getRed()   / 255.0) * (OC.getRed()   / 255.0) * diffuse + (LC.getRed()   / 255.0) * specular;
            totalG += (LC.getGreen() / 255.0) * (OC.getGreen() / 255.0) * diffuse + (LC.getGreen() / 255.0) * specular;
            totalB += (LC.getBlue()  / 255.0) * (OC.getBlue()  / 255.0) * diffuse + (LC.getBlue()  / 255.0) * specular;
        }

        // -- AMBIENT
        double ambient = 0.0;
        totalR += (OC.getRed()   / 255.0) * ambient;
        totalG += (OC.getGreen() / 255.0) * ambient;
        totalB += (OC.getBlue()  / 255.0) * ambient;

        int r = (int) Math.min(255, totalR * 255);
        int g = (int) Math.min(255, totalG * 255);
        int b = (int) Math.min(255, totalB * 255);

        return new Color(r, g, b);
    }
}