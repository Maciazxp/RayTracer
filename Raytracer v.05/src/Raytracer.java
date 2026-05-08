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

        // create camera
        Vector3D cameraPos = new Vector3D(0, 14, 10); // cameraPos recommended:v(0, 0, 0) origin
        Camera camera = new Camera(cameraPos, 75.0, width, height); // fov recommended: 60 (80 or more is for the temple.obj)

        // create the scene and set the camera (camera is part of the scene)
        Scene scene = new Scene(0.1, 2000.0); // test (it works to render the spheres and triangle): near=0.1, far=50
        scene.setCamera(camera);

        // Directional light: direction, color, intensity
        // Direction = where the light is coming FROM
        Light light = new Light(
                new Vector3D(0.0, 1.0, 1.0),  // coming from above-front
                Color.WHITE,
                1.0
        );

        // add an OBJ
        List<Triangle> objTriangles1 =  OBJ.read("Temple.obj", Color.CYAN, 1);
        for (Triangle t : objTriangles1) { // iterates over ALL triangles of the OBJ
            // this is repeated for each individual triangle
            // the triangles must be in the same relative position (if not, the object would break)
            // each vector3D (v0,v1,v2) are the vertices of a triangle
            Vector3D v0 = new Vector3D(t.getVertex0().getX(), t.getVertex0().getY(), t.getVertex0().getZ() - 15);
            Vector3D v1 = new Vector3D(t.getVertex1().getX(), t.getVertex1().getY(), t.getVertex1().getZ() - 15);
            Vector3D v2 = new Vector3D(t.getVertex2().getX(), t.getVertex2().getY(), t.getVertex2().getZ() - 15);

            // Preserve vertex normals if the triangle has them
            if (t.hasVertexNormals()) {
                scene.addObject(new Triangle(v0, v1, v2,
                        t.getNormal0(), t.getNormal1(), t.getNormal2(), Color.CYAN));
            } else {
                scene.addObject(new Triangle(v0, v1, v2, Color.CYAN)); // to construct a triangle of the object, it needs three vertices (and color)
            }
        }

        // create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // get the camera from the scene
        Camera cameraReal = scene.getCamera();

        // get the render
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // generate the ray from the camera in the scene
                Ray ray = cameraReal.getRay(x, y, width, height);

                // get the closest intersection in the scene
                Intersection intersect = scene.rayCast(ray);

                // if there's an intersection
                if (intersect != null) {
                    // Use Phong if the object has vertex normals
                    Color shadedColor = applyShading(intersect, light, cameraReal);
                    image.setRGB(x, y, shadedColor.getRGB());
                } else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        try {
            ImageIO.write(image, "png", new File("output.png"));
            System.out.println("Render complete: output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Unified shading — uses the normal already interpolated by Triangle.getIntersection()
    // If the triangle had vn normals, the normal is already Phong-interpolated.
    // If not, it's the flat face normal.
    // Diffuse = LC x OC x LI x (N · L)
    static Color applyShading(Intersection intersect, Light light, Camera camera) {
        Vector3D N = intersect.getNormal(); // already interpolated if Phong
        Vector3D L = light.getDirection();

        double nDotL = Vector3D.dot(N, L);
        double diffuse = Math.max(0.0, nDotL) * light.getIntensity();

        //Specular calculation
        double specular = 0.0;

        // Only calculate specular highlight if the light is hitting the front of the face
        if (nDotL > 0) {
            // R = reflection vector: 2 * (N.L) * N - L
            Vector3D R = Vector3D.sub(Vector3D.scalar(N, 2.0 * nDotL), L);
            R = Vector3D.normalize(R);

            // V = vector towards the camera
            Vector3D V = Vector3D.normalize(Vector3D.sub(camera.getPosition(), intersect.getPosition()));

            double specPower = 32.0; // Phong exponent (how concentrated the highlight is)
            specular = Math.pow(Math.max(0.0, Vector3D.dot(R, V)), specPower) * light.getIntensity();
        }

        Color OC = intersect.getObject().getColor();
        Color LC = light.getColor();

        // Add specular part. the specular highlight assumes light color, not object color.
        int r = (int) Math.min(255, ((LC.getRed() / 255.0)   * (OC.getRed() / 255.0)   * diffuse + (LC.getRed() / 255.0)   * specular) * 255);
        int g = (int) Math.min(255, ((LC.getGreen() / 255.0) * (OC.getGreen() / 255.0) * diffuse + (LC.getGreen() / 255.0) * specular) * 255);
        int b = (int) Math.min(255, ((LC.getBlue() / 255.0)  * (OC.getBlue() / 255.0)  * diffuse + (LC.getBlue() / 255.0)  * specular) * 255);

        return new Color(r, g, b);
    }
}