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
        Vector3D cameraPos = new Vector3D(0, 14, 10); // cameraPos recommended:v(0, 0, 0) origin
        Camera camera = new Camera(cameraPos, 75.0, width, height); // fov recommended: 60 (80 or more is for the temple.obj)

        // create the scene and set the camera (camera is part of the scene)
        Scene scene = new Scene(0.1, 2000.0); // test (it works to render the spheres and triangle): near=0.1, far=50
        scene.setCamera(camera);


        //--LIGHTS--

        /*
        // Directional light: direction, color, intensity
        // Direction = where the light is coming FROM
        scene.addLight(new DirectionalLight(
                new Vector3D(0,1,1), //coming from above-front
                Color.WHITE,
                5.0
        ));
        */

        //Point light in the scene
        scene.addLight(new PointLight(
                new Vector3D(5.0, 15.0, -10.0),
                Color.WHITE,
                80.0   // higher value because attenuation divides by distance^2
        ));



        //  -- OBJECTS --

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




        // --RENDER--

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
                    Color shadedColor = applyShading(intersect, scene, cameraReal);
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
    static Color applyShading(Intersection intersect, Scene scene, Camera camera) {
        Vector3D N = intersect.getNormal(); // already interpolated if Phong
        //Vector3D L = light.getDirection();
        Color OC = intersect.getObject().getColor();

        double totalR = 0, totalG = 0, totalB = 0;


        for (Light light : scene.getLights()) {
            // L = direction from intersection point TOWARDS the light
            Vector3D L = light.getDirectionToLight(intersect.getPosition());

            //-- SHADOW
            // Build a shadow ray starting slightly above the surface (offset avoids acne)
            Vector3D shadowOrigin = Vector3D.add(intersect.getPosition(), Vector3D.scalar(N,0.001));
            Ray shadowRay = new Ray(shadowOrigin, L);

            // For point lights we only block if the occluder is CLOSER than the light itself.
            // For directional lights the light is infinitely far, so we use MAX_VALUE.
            double maxShadowDist = Double.MAX_VALUE;
            if (light instanceof PointLight) {
                maxShadowDist = Vector3D.magnitude(
                        Vector3D.sub(((PointLight) light).getPosition(), intersect.getPosition())
                );
            }

            // If the point is in shadow, skip this light entirely
            if (scene.isOccluded(shadowRay, intersect.getObject(), maxShadowDist)) {
                continue;
            }

            //--LIGHT FALLOFF
            // getIntensityAt() already encodes the falloff model:
            //   DirectionalLight returns flat intensity
            //   PointLight returns intensity / d²
            double intensityAtPoint = light.getIntensityAt(intersect.getPosition());


            //-- DIFFUSE (LAMBERT)
            double nDotL = Vector3D.dot(N, L);
            double diffuse = Math.max(0.0, nDotL) * intensityAtPoint;


            // -- SPECULAR (PHONG)
            // Specular calculation
            double specular = 0.0;

            // Only calculate specular highlight if the light is hitting the front of the face
            if (nDotL > 0) {
                // R = reflection vector: 2 * (N.L) * N - L
                Vector3D R = Vector3D.normalize(Vector3D.sub(Vector3D.scalar(N, 2.0 * nDotL), L));
                // V = vector towards the camera
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

        // Ambient light — illuminates everything slightly regardless of shadows
        double ambient = 0.025; // it can change. tested on 0.05
        totalR += (OC.getRed()   / 255.0) * ambient;
        totalG += (OC.getGreen() / 255.0) * ambient;
        totalB += (OC.getBlue()  / 255.0) * ambient;

        // Add specular part. the specular highlight assumes light color, not object color.
        int r = (int) Math.min(255, totalR * 255);
        int g = (int) Math.min(255, totalG * 255);
        int b = (int) Math.min(255, totalB * 255);

        return new Color(r, g, b);
    }
}

