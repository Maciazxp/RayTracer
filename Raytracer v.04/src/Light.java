import java.awt.Color;

public class Light {
    private Vector3D direction; // direction the light is coming FROM
    private Color color;
    private double intensity;

    public Light (Vector3D direction, Color color, double intensity){
        this.direction = Vector3D.normalize(direction);
        this.color = color;
        this.intensity = intensity;
    }

    public Vector3D getDirection() { return direction; }
    public Color getColor() { return color; }
    public double getIntensity() { return intensity; }


    public void setDirection(Vector3D direction) {
        this.direction = direction;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
 