import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Raytracer {

    public static void main(String[] args) {
        int width  = 1920;
        int height = 1080;

        // -- CAMERA --
        Vector3D cameraPos = new Vector3D(0, 0.40, 2);
        Camera camera = new Camera(cameraPos, 40.0, width, height);

        // -- SCENE --
        Scene scene = new Scene(0.1, 50.0);
        scene.setCamera(camera);

        // -- LIGHTS --
        scene.addLight(new PointLight(
                new Vector3D(1, 1, 1.9), new Color(255, 255, 255), 1.5));
        scene.addLight(new PointLight(
                new Vector3D(0.45, 0.3, 1.8), new Color(255, 255, 255), 1.0));

        // -- OBJECTS --
        // OBJ with MTL textures  +  BlinnPhong light behaviour
        // The MTL provides the diffuse texture and normal map automatically.
        // The last argument defines how that surface reacts to light.

        List<Triangle> shield = OBJ.read("shield.obj", "shield.mtl", 1.0);
        scene.addMesh(shield, new Vector3D(0, 0, 0), 3.0, TypicalMaterials.IRON);

        /*
        // Solid-color object with a BlinnPhong material (no MTL)
        List<Triangle> teapot = OBJ.read("SmallTeapot.obj", Color.CYAN, 1.0);
        scene.addMesh(teapot, new Vector3D(0.45, 0.1, 0.7), 0.35, TypicalMaterials.PLASTIC);
        */

        // -- BUILD BVH --
        scene.buildBVH();

        // -- RENDER --
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Camera cam = scene.getCamera();
        long start = System.currentTimeMillis();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = cam.getRay(x, y, width, height);
                Intersection hit = scene.rayCast(ray);
                image.setRGB(x, y, hit != null
                        ? applyShading(hit, scene, cam).getRGB()
                        : Color.BLACK.getRGB());
            }
            if (y % 60 == 0)
                System.out.printf("Progress: %.1f%%\n", y * 100.0 / height);
        }

        System.out.printf("Render complete in %.2f seconds\n",
                (System.currentTimeMillis() - start) / 1000.0);

        try {
            ImageIO.write(image, "png", new File("output.png"));
            System.out.println("Saved: output.png");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // HYBRID BLINN-PHONG SHADING
    //
    // Priority:
    //   1. Normal     → normal map (TBN) if available, else Phong-interpolated / flat
    //   2. Base color → diffuse texture from MTL if available, else flat object color
    //   3. Lighting   → BlinnPhongMaterial coefficients if assigned to the triangle,
    //                   else sensible defaults (ambient=0.05 diffuse=1.0 specular=0.5)
    // ─────────────────────────────────────────────────────────────────────────────
    static Color applyShading(Intersection intersect, Scene scene, Camera camera) {
        Object3D obj = intersect.getObject();

        // ── 1. NORMAL ────────────────────────────────────────────────────────────
        Vector3D N = intersect.getNormal();

        if (obj instanceof Triangle tri
                && tri.hasMaterial() && tri.getMaterial().hasNormalMap()
                && tri.hasUVs() && tri.hasTBN()) {

            Vector3D tsN = tri.getMaterial().sampleNormal(intersect.getU(), intersect.getV());
            // Gram-Schmidt re-orthogonalize tangent against shading normal
            Vector3D T = tri.getTangent();
            T = Vector3D.normalize(Vector3D.sub(T, Vector3D.scalar(N, Vector3D.dot(N, T))));
            Vector3D B = tri.getBitangent();

            N = Vector3D.normalize(Vector3D.add(
                    Vector3D.scalar(T, tsN.getX()),
                    Vector3D.add(Vector3D.scalar(B, tsN.getY()),
                            Vector3D.scalar(N, tsN.getZ()))));
        }

        // ── 2. BASE COLOR ────────────────────────────────────────────────────────
        Color OC = obj.getColor(); // fallback: flat color
        if (obj instanceof Triangle tri
                && tri.hasMaterial() && tri.getMaterial().hasDiffuseTexture()) {
            OC = tri.getMaterial().sampleDiffuse(intersect.getU(), intersect.getV());
        }

        // ── 3. BlinnPhong COEFFICIENTS ───────────────────────────────────────────
        // Use the triangle's assigned material, or fall back to neutral defaults.
        double ka, kd, ks, alpha;
        if (obj instanceof Triangle tri && tri.hasBPMaterial()) {
            BlinnPhongMaterial bp = tri.getBPMaterial();
            ka    = bp.getAmbient();
            kd    = bp.getDiffuse();
            ks    = bp.getSpecular();
            alpha = bp.getShininess();
        } else {
            // Defaults: low ambient, full diffuse, moderate specular
            ka    = 0.05;
            kd    = 1.00;
            ks    = 0.50;
            alpha = 32.0;
        }

        double totalR = 0, totalG = 0, totalB = 0;

        // ── 4. AMBIENT ───────────────────────────────────────────────────────────
        totalR += (OC.getRed()   / 255.0) * ka;
        totalG += (OC.getGreen() / 255.0) * ka;
        totalB += (OC.getBlue()  / 255.0) * ka;

        // ── 5. PER-LIGHT: DIFFUSE + SPECULAR ─────────────────────────────────────
        for (Light light : scene.getLights()) {
            Vector3D L = light.getDirectionToLight(intersect.getPosition());

            // Shadow test
            Vector3D shadowOrigin = Vector3D.add(intersect.getPosition(),
                    Vector3D.scalar(N, 0.001));
            Ray shadowRay = new Ray(shadowOrigin, L);

            double maxShadowDist = Double.MAX_VALUE;
            if (light instanceof PointLight pl)
                maxShadowDist = Vector3D.magnitude(
                        Vector3D.sub(pl.getPosition(), intersect.getPosition()));

            if (scene.isOccluded(shadowRay, intersect.getObject(), maxShadowDist))
                continue;

            double intensity = light.getIntensityAt(intersect.getPosition());
            double nDotL     = Math.max(0.0, Vector3D.dot(N, L));

            // Diffuse (Lambert)
            double diffuse = kd * nDotL * intensity;

            // Specular (Blinn-Phong: half-vector H instead of reflect vector R)
            double specular = 0.0;
            if (nDotL > 0) {
                Vector3D V = Vector3D.normalize(
                        Vector3D.sub(camera.getPosition(), intersect.getPosition()));
                Vector3D H = Vector3D.normalize(Vector3D.add(L, V)); // half-vector
                double nDotH = Math.max(0.0, Vector3D.dot(N, H));
                specular = ks * Math.pow(nDotH, alpha) * intensity;
            }

            Color LC = light.getColor();
            double lr = LC.getRed()   / 255.0;
            double lg = LC.getGreen() / 255.0;
            double lb = LC.getBlue()  / 255.0;

            totalR += lr * (OC.getRed()   / 255.0) * diffuse + lr * specular;
            totalG += lg * (OC.getGreen() / 255.0) * diffuse + lg * specular;
            totalB += lb * (OC.getBlue()  / 255.0) * diffuse + lb * specular;
        }

        int r = (int) Math.min(255, totalR * 255);
        int g = (int) Math.min(255, totalG * 255);
        int b = (int) Math.min(255, totalB * 255);
        return new Color(r, g, b);
    }
}
