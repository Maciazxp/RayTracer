public class Vector3D {
    private double x, y, z;
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);


    public Vector3D(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }


    //add
    public static Vector3D add(Vector3D v1, Vector3D v2){
        return new Vector3D(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());
    }

    //subtract
    public static Vector3D sub(Vector3D v1, Vector3D v2){
        return new Vector3D( v1.getX() - v2.getX(), v1.getY() - v2.getY(), v1.getZ() - v2.getZ());
    }

    //scalar multiplication
    public static Vector3D scalar(Vector3D v1, double scalar){
        return new Vector3D(v1.getX()*scalar, v1.getY()*scalar, v1.getZ()*scalar);
    }

    //dot product
    public static double dot(Vector3D v1, Vector3D v2){
        return v1.getX()*v2.getX() + v1.getY()*v2.getY() + v1.getZ()*v2.getZ();
    }


    //normalize
    public static double magnitude(Vector3D v1){
        return Math.sqrt(dot(v1,v1));
    }
    public static Vector3D normalize(Vector3D v1){
        double magnitude =Vector3D.magnitude(v1);
        if (magnitude == 0){
            return Vector3D.ZERO;
        }else{
            return new Vector3D(v1.getX()/magnitude, v1.getY()/magnitude, v1.getZ()/magnitude);
        }
    }


}
