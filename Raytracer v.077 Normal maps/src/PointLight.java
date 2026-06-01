import java.awt.Color;

public class PointLight extends Light {
    private Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    public Vector3D getPosition() {
        return position;
    }
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    @Override
    public Vector3D getDirectionToLight(Vector3D point) {
        // Direction from intersection point towards the light position
        return Vector3D.normalize(Vector3D.sub(position, point));
    }

    @Override
    public double getIntensityAt(Vector3D point) {
        // Attenuation: intensity drops with the square of the distance
        double distance = Vector3D.magnitude(Vector3D.sub(position, point));
        return getIntensity() / (distance * distance);
    }



}
