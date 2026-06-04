import java.awt.Color;

public abstract class Light {
    private Color color;
    private double intensity;

    public Light(Color color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public Color getColor() {
        return color;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    // Returns the direction FROM the intersection point towards the light
    public abstract Vector3D getDirectionToLight(Vector3D point);

    // Returns the intensity at a given point (directional = constant, point = attenuated)
    public abstract double getIntensityAt(Vector3D point);


}