public class Camera {
    private Vector3D position;
    private double fov;
    private double aspectRatio;

    public Camera(Vector3D position, double fov, int width, int height) {
        this.position = position;
        this.fov = fov;
        this.aspectRatio = (double) width / height;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Ray getRay(int x, int y, int width, int height) {
        double px = (2.0 * (x + 0.5) / width - 1.0) * aspectRatio;
        double py = 1.0 - 2.0 * (y + 0.5) / height;

        double fovRad = Math.toRadians(fov);
        double tanFov = Math.tan(fovRad / 2.0);
        px *= tanFov;
        py *= tanFov;

        Vector3D dirNotNormalized = new Vector3D(px, py, -1);
        Vector3D direction = Vector3D.normalize(dirNotNormalized);

        return new Ray(position,direction);
    }



}
