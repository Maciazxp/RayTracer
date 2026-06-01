import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Material {
    private String name;
    private BufferedImage diffuseTexture; // map_Kd (base color)

    public Material(String name) {
        this.name = name;
        this.diffuseTexture = null;
    }

    public String getName() {
        return name;
    }

    public void loadDiffuseTexture(String absolutePath) {
        try {
            diffuseTexture = ImageIO.read(new File(absolutePath));
            System.out.println("Texture loaded: " + absolutePath);
        } catch (IOException e) {
            System.err.println("Could not load texture: " + absolutePath);
        }
    }

    public boolean hasDiffuseTexture() {
        return diffuseTexture != null;
    }

    // Sample the texture at UV coordinates (u, v), both in range [0, 1]
    public Color sampleDiffuse(double u, double v) {
        if (diffuseTexture == null) return Color.MAGENTA; // visible fallback if texture missing

        // Wrap UV coordinates (repeat mode)
        u = u - Math.floor(u);
        v = v - Math.floor(v);

        // Flip V: image Y axis goes down, UV V axis goes up
        v = 1.0 - v;

        int x = (int) Math.min(u * diffuseTexture.getWidth(),  diffuseTexture.getWidth()  - 1);
        int y = (int) Math.min(v * diffuseTexture.getHeight(), diffuseTexture.getHeight() - 1);

        return new Color(diffuseTexture.getRGB(x, y));
    }
}
