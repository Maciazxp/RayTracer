/**
 * TypicalMaterials — ready-to-use BlinnPhongMaterial presets.
 *
 * Constructor order: (ambient, diffuse, specular, shininess, transparency, reflectivity)
 *
 * Usage example in Raytracer.java / main():
 *
 *   List<Triangle> mesh = OBJ.read("shield.obj", "shield.mtl", 1.0);
 *   scene.addMesh(mesh, new Vector3D(0, 0, 0), 3.0, TypicalMaterials.GOLD);
 *
 * The mesh will use its MTL textures for color/normal and GOLD for light behaviour.
 */
public class TypicalMaterials {

    // ── METALS ───────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial GOLD = new BlinnPhongMaterial(
            0.10, 0.50, 0.90, 64.0,  0.0, 0.50);

    public static final BlinnPhongMaterial SILVER = new BlinnPhongMaterial(
            0.10, 0.50, 0.95, 96.0,  0.0, 0.90);

    public static final BlinnPhongMaterial BRONZE = new BlinnPhongMaterial(
            0.10, 0.50, 0.80, 64.0,  0.0, 0.60);

    public static final BlinnPhongMaterial SHINY_METAL = new BlinnPhongMaterial(
            0.10, 0.30, 1.00, 256.0, 0.0, 0.90);

    public static final BlinnPhongMaterial IRON = new BlinnPhongMaterial(
            0.05, 0.40, 0.70, 48.0,  0.0, 0.40);

    // ── PLASTICS ─────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial PLASTIC = new BlinnPhongMaterial(
            0.30, 0.60, 0.80, 10.0,  0.0, 0.30);

    public static final BlinnPhongMaterial RUBBER = new BlinnPhongMaterial(
            0.02, 0.01, 0.40, 10.0,  0.0, 0.05);

    // ── STONE & CONCRETE ─────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial MARBLE = new BlinnPhongMaterial(
            0.25, 0.40, 0.30, 10.0,  0.0, 0.30);

    public static final BlinnPhongMaterial MARBLE_OLD = new BlinnPhongMaterial(
            0.35, 0.20, 0.10, 5.0,   0.0, 0.20);

    public static final BlinnPhongMaterial CONCRETE = new BlinnPhongMaterial(
            0.60, 0.10, 0.05, 5.0,   0.0, 0.02);

    public static final BlinnPhongMaterial STONE = new BlinnPhongMaterial(
            0.40, 0.35, 0.10, 8.0,   0.0, 0.05);

    // ── WOOD ─────────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial WOOD = new BlinnPhongMaterial(
            0.20, 0.50, 0.10, 10.0,  0.0, 0.10);

    public static final BlinnPhongMaterial DARK_WOOD = new BlinnPhongMaterial(
            0.20, 0.50, 0.10, 10.0,  0.0, 0.10);

    // ── FABRIC & CLOTH ───────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial CLOTH = new BlinnPhongMaterial(
            0.40, 0.30, 0.10, 5.0,   0.0, 0.05);

    public static final BlinnPhongMaterial SILK = new BlinnPhongMaterial(
            0.25, 0.50, 0.60, 32.0,  0.0, 0.20);

    // ── LEATHER ──────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial LEATHER = new BlinnPhongMaterial(
            0.50, 0.30, 0.25, 15.0,  0.0, 0.08);

    // ── GEMS ─────────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial EMERALD = new BlinnPhongMaterial(
            0.20, 0.20, 1.00, 256.0, 0.3, 0.60);

    public static final BlinnPhongMaterial RUBY = new BlinnPhongMaterial(
            0.25, 0.15, 1.00, 256.0, 0.2, 0.60);

    public static final BlinnPhongMaterial SAPPHIRE = new BlinnPhongMaterial(
            0.25, 0.20, 1.00, 256.0, 0.2, 0.60);

    public static final BlinnPhongMaterial DIAMOND = new BlinnPhongMaterial(
            0.30, 0.10, 1.00, 512.0, 0.7, 0.90);

    public static final BlinnPhongMaterial AMETHYST = new BlinnPhongMaterial(
            0.20, 0.20, 1.00, 256.0, 0.3, 0.60);

    // ── GLASS ────────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial GLASS = new BlinnPhongMaterial(
            0.05, 0.10, 1.00, 128.0, 0.9, 0.10);

    public static final BlinnPhongMaterial GLASS_FROSTED = new BlinnPhongMaterial(
            0.20, 0.40, 0.80, 32.0,  0.7, 0.20);

    public static final BlinnPhongMaterial GLASS_TINTED = new BlinnPhongMaterial(
            0.05, 0.10, 1.00, 128.0, 0.8, 0.30);

    // ── WATER ────────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial WATER = new BlinnPhongMaterial(
            0.05, 0.20, 0.80, 32.0,  0.7, 0.20);

    // ── ORGANIC / NATURE ─────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial LEAF = new BlinnPhongMaterial(
            0.20, 0.50, 0.20, 15.0,  0.1, 0.10);

    public static final BlinnPhongMaterial SAND = new BlinnPhongMaterial(
            0.25, 0.40, 0.10, 5.0,   0.0, 0.05);

    // ── PAINT ────────────────────────────────────────────────────────────────────
    public static final BlinnPhongMaterial PAINT_GLOSSY = new BlinnPhongMaterial(
            0.40, 0.50, 0.60, 30.0,  0.0, 0.20);

    public static final BlinnPhongMaterial PAINT_MATTE = new BlinnPhongMaterial(
            0.55, 0.20, 0.10, 8.0,   0.0, 0.05);

    public static final BlinnPhongMaterial PAINT_SATIN = new BlinnPhongMaterial(
            0.50, 0.35, 0.30, 20.0,  0.0, 0.10);
}
