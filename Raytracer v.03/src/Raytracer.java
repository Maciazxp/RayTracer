
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
        for (Triangle t : objTriangles) {
            Vector3D v0 = new Vector3D(t.getVertex0().getX()-2, t.getVertex0().getY(), t.getVertex0().getZ() -13);
            Vector3D v1 = new Vector3D(t.getVertex1().getX()-2, t.getVertex1().getY(), t.getVertex1().getZ() -13);
            Vector3D v2 = new Vector3D(t.getVertex2().getX()-2, t.getVertex2().getY(), t.getVertex2().getZ() -13);
            scene.addObject(new Triangle(v0, v1, v2, Color.ORANGE));
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