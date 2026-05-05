
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Raytracer {
    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        /* simple test
        Vector3D camera = new Vector3D(0,0,0);
        Sphere sphere = new Sphere(new Vector3D(0,0,-5),1,Color.blue);
        */

        //create camera
        Vector3D cameraPos = new Vector3D(0, 0, 0);
        Camera camera = new Camera(cameraPos, 60.0, width, height);

        // create the scene and set the camera (camera is part of the scene)
        Scene scene = new Scene(0.1, 50.0); // test (it works to render the spheres and triangle): near=0.1, far=50
        scene.setCamera(camera);


        // Directional light: direction, color, intensity
        // Direction = where the light is coming FROM
        Light light = new Light(
                new Vector3D(0.0, 1.0, 1.0),  // coming from above-front
                Color.WHITE,
                1.0
        );

        // Add objects to the scene (SPHERES AND TRIANGLES)
        scene.addObject(new Sphere(new Vector3D(0, 0.5, -4), 0.3, Color.RED));
        scene.addObject(new Sphere(new Vector3D(1.5, 0.5, -6), 0.2, Color.BLUE));
        //Add the new triangle
        scene.addObject(new Triangle(
                new Vector3D(-0.5, -0.5, -3), //vertex 1
                new Vector3D( 0.5, -0.5, -3), //vertex 2
                new Vector3D( 0.0,  0.2, -3), //vertex 3
                Color.PINK
        ));

        //add an OBJ
        List<Triangle> objTriangles = OBJ.read("sword.obj", Color.ORANGE,1);
        for (Triangle t : objTriangles) { //iterates over ALL triangles of the OBJ
            //this is repeated for each individual triangle
            //the triangles must be in the same relative position (if not, the object would break)
            //each vector3D (v0,v1,v2) are the vertices of a triangle
            Vector3D v0 = new Vector3D(t.getVertex0().getX()-2, t.getVertex0().getY(), t.getVertex0().getZ() -15);
            Vector3D v1 = new Vector3D(t.getVertex1().getX()-2, t.getVertex1().getY(), t.getVertex1().getZ() -15);
            Vector3D v2 = new Vector3D(t.getVertex2().getX()-2, t.getVertex2().getY(), t.getVertex2().getZ() -15);
            scene.addObject(new Triangle(v0, v1, v2, Color.ORANGE)); //to construct a triangle of the object, it needs three vertices (and color)
        }


        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //get the camera from the scene
        Camera cameraReal = scene.getCamera();


        //get the render
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //generate the ray from the camera in the scene
                Ray ray = cameraReal.getRay(x, y, width, height);

                //get the closest intersection in the scene
                Intersection intersect = scene.rayCast(ray);

                //if there's an intersection
                if (intersect != null) {
                    Color shadedColor = applyFlatShading(intersect, light);
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

    // Flat shading: Diffuse = LC x OC x LI x (N · L)
    static Color applyFlatShading(Intersection intersect, Light light) {
        Vector3D N = intersect.getNormal();  // object normal
        Vector3D L = light.getDirection();   // light direction

        // N · L = cos(theta) — how much the surface faces the light
        double nDotL = Vector3D.dot(N, L);

        // Clamp to 0 so backfaces don't go negative
        double diffuse = Math.max(0.0, nDotL) * light.getIntensity();

        // Object color and light color components (0-255 → 0.0-1.0)
        Color OC = intersect.getObject().getColor();
        Color LC = light.getColor();

        // Diffuse = LC x OC x LI x (N · L)
        int r = (int) Math.min(255, (LC.getRed()   / 255.0) * (OC.getRed()   / 255.0) * diffuse * 255);
        int g = (int) Math.min(255, (LC.getGreen() / 255.0) * (OC.getGreen() / 255.0) * diffuse * 255);
        int b = (int) Math.min(255, (LC.getBlue()  / 255.0) * (OC.getBlue()  / 255.0) * diffuse * 255);

        return new Color(r, g, b);
    }
}



        /*
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //generate the ray from the camera in the scene
                Ray ray = cameraReal.getRay(x, y, width, height);

                //get the closest intersection in the scene
                Intersection intersect = scene.rayCast(ray);

                if (intersect != null) {
                    Color objectColor = intersect.object.getColor();
                    image.setRGB(x, y, objectColor.getRGB());
                } else {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        try {
            ImageIO.write(image, "png", new File("output.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
*/