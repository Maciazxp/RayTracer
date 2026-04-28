
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
        Scene scene = new Scene(0.1, 50.0); // test (it works): near=0.1, far=50
        scene.setCamera(camera);

        // Add objects to the scene
        scene.addObject(new Sphere(new Vector3D(0, 0.5, -5), 0.3, Color.RED));
        scene.addObject(new Sphere(new Vector3D(1.5, 0.5, -6), 0.2, Color.BLUE));
        //Add the new triangle
        scene.addObject(new Triangle(
                new Vector3D(-0.5, -0.5, -5), //vertex 1
                new Vector3D( 0.5, -0.5, -5), //vertex 2
                new Vector3D( 0.0,  0.2, -5), //vertex 3
                Color.PINK
        ));

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