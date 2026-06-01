import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Material {
    private String name;
    private BufferedImage diffuseTexture; // map_Kd (base color)
    private BufferedImage normalMap;      // map_Bump (normal map)

    public Material(String name) {
        this.name = name;
        this.diffuseTexture = null;
        this.normalMap = null;
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

    public void loadNormalMap(String absolutePath) {
        try {
            normalMap = ImageIO.read(new File(absolutePath));
            System.out.println("Normal map loaded: " + absolutePath);
        } catch (IOException e) {
            System.err.println("Could not load normal map: " + absolutePath);
        }
    }

    public boolean hasDiffuseTexture() {
        return diffuseTexture != null;
    }

    public boolean hasNormalMap() {
        return normalMap != null;
    }

    // Sample the diffuse texture at UV coordinates (u, v), both in range [0, 1]
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

    // Sample the normal map and return a tangent-space normal in [-1, 1] range
    public Vector3D sampleNormal(double u, double v) {
        if (normalMap == null) return null;

        // Wrap UV coordinates (repeat mode)
        u = u - Math.floor(u);
        v = v - Math.floor(v);

        // Flip V: same convention as diffuse
        v = 1.0 - v;

        int x = (int) Math.min(u * normalMap.getWidth(),  normalMap.getWidth()  - 1);
        int y = (int) Math.min(v * normalMap.getHeight(), normalMap.getHeight() - 1);

        Color c = new Color(normalMap.getRGB(x, y));

        // RGB [0, 255] → XYZ [-1, 1]
        double nx = (c.getRed()   / 127.5) - 1.0;
        double ny = (c.getGreen() / 127.5) - 1.0;
        double nz = (c.getBlue()  / 127.5) - 1.0;

        return Vector3D.normalize(new Vector3D(nx, ny, nz));
    }
}