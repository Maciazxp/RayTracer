/*
  BlinnPhongMaterial defines how a surface RESPONDS TO LIGHT.

  Parameters (To know what i am moving):
    ka           Ambient coefficient          [0, 1]
    kd           Diffuse coefficient          [0, 1]
    ks           Specular coefficient         [0, 1]
    shininess    Specular sharpness exponent  (8 = dull, 512 = mirror like)
    transparency Fraction of light that passes through (kt) [0=opaque, 1=fully transparent]
    reflectivity Fraction of light that reflects  (kr)      [0=none,   1=perfect mirror]
    ior          Index of Refraction — how much light bends when entering this material

 */
public class BlinnPhongMaterial {

    private double ambient;
    private double diffuse;
    private double specular;
    private double shininess;
    private double transparency;  // kt
    private double reflectivity;  // kr
    private double ior;           // Index of Refraction


      //-- Full constructor including IOR.

    public BlinnPhongMaterial(double ambient, double diffuse, double specular,
                               double shininess, double transparency,
                               double reflectivity, double ior) {
        this.ambient      = ambient;
        this.diffuse      = diffuse;
        this.specular     = specular;
        this.shininess    = shininess;
        this.transparency = transparency;
        this.reflectivity = reflectivity;
        this.ior          = ior;
    }


     //Constructor with NO IOR
    public BlinnPhongMaterial(double ambient, double diffuse, double specular,
                               double shininess, double transparency, double reflectivity) {
        this(ambient, diffuse, specular, shininess, transparency, reflectivity,
                transparency > 0 ? 1.5 : 1.0);
    }

    public double getAmbient()      { return ambient; }
    public double getDiffuse()      { return diffuse; }
    public double getSpecular()     { return specular; }
    public double getShininess()    { return shininess; }
    public double getTransparency() { return transparency; }
    public double getReflectivity() { return reflectivity; }
    public double getIor()          { return ior; }

    public void setAmbient(double v)      { this.ambient      = v; }
    public void setDiffuse(double v)      { this.diffuse      = v; }
    public void setSpecular(double v)     { this.specular     = v; }
    public void setShininess(double v)    { this.shininess    = v; }
    public void setTransparency(double v) { this.transparency = v; }
    public void setReflectivity(double v) { this.reflectivity = v; }
    public void setIor(double v)          { this.ior          = v; }
}
