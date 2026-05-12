import java.awt.Color;

public class DirectionalLight extends Light {
    private Vector3D direction; //the direction of the light is coming from

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = Vector3D.normalize(direction);
    }

    public Vector3D getDirection(){
        return direction;
    }
    public void setDirection(Vector3D direction){
        this.direction = Vector3D.normalize(direction);
    }

    @Override
    public Vector3D getDirectionToLight(Vector3D point) {
        // Directional light: same direction everywhere (because infinite distance)
        return direction;
    }

    @Override
    public double getIntensityAt(Vector3D point) {
        //Directional light has no attenuation
        return getIntensity();
    }



}
