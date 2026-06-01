/**
 * BlinnPhongMaterial defines how a surface RESPONDS TO LIGHT.
 * It is completely separate from Material (which holds textures from the MTL file).
 *
 * When a Triangle has both:
 *   - a Material    → provides diffuse texture + normal map (from MTL)
 *   - a BlinnPhongMaterial → provides ambient, diffuse, specular, shininess coefficients
 *
 * The hybrid shading in Raytracer.applyShading() combines both:
 *   base color  → from diffuse texture if available, else from the triangle's flat color
 *   light math  → uses BlinnPhongMaterial coefficients (or hardcoded defaults if absent)
 */
public class BlinnPhongMaterial {

    private double ambient;       // ka  — how much ambient light is reflected  [0, 1]
    private double diffuse;       // kd  — how much diffuse light is reflected   [0, 1]
    private double specular;      // ks  — how strong the specular highlight is  [0, 1]
    private double shininess;     // α   — sharpness of the specular highlight   (e.g. 8–512)
    private double transparency;  // kt  — 0 = opaque, 1 = fully transparent    [0, 1]  (future use)
    private double reflectivity;  // kr  — 0 = no reflection, 1 = mirror        [0, 1]  (future use)

    /**
     * @param ambient      Ambient coefficient  ka  [0, 1]
     * @param diffuse      Diffuse coefficient  kd  [0, 1]
     * @param specular     Specular coefficient ks  [0, 1]
     * @param shininess    Shininess exponent   α   (8 = dull, 512 = mirror-like)
     * @param transparency Transparency factor  kt  [0, 1]  — reserved for future refraction
     * @param reflectivity Reflectivity factor  kr  [0, 1]  — reserved for future reflection rays
     */
    public BlinnPhongMaterial(double ambient, double diffuse, double specular,
                               double shininess, double transparency, double reflectivity) {
        this.ambient      = ambient;
        this.diffuse      = diffuse;
        this.specular     = specular;
        this.shininess    = shininess;
        this.transparency = transparency;
        this.reflectivity = reflectivity;
    }

    public double getAmbient()      { return ambient; }
    public double getDiffuse()      { return diffuse; }
    public double getSpecular()     { return specular; }
    public double getShininess()    { return shininess; }
    public double getTransparency() { return transparency; }
    public double getReflectivity() { return reflectivity; }

    public void setAmbient(double ambient)           { this.ambient      = ambient; }
    public void setDiffuse(double diffuse)           { this.diffuse      = diffuse; }
    public void setSpecular(double specular)         { this.specular     = specular; }
    public void setShininess(double shininess)       { this.shininess    = shininess; }
    public void setTransparency(double transparency) { this.transparency = transparency; }
    public void setReflectivity(double reflectivity) { this.reflectivity = reflectivity; }
}
