import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBJ {
    public static List<Triangle> read(String objPath, String mtlPath, double scale) {
        List<Vector3D> vertices  = new ArrayList<>();
        List<Vector3D> normals   = new ArrayList<>();
        List<double[]> uvs       = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        // Load materials from MTL (empty map if mtlPath is null)
        Map<String, Material> materials = new HashMap<>();
        if (mtlPath != null) {
            materials = MTL.read(mtlPath);
        }

        Material currentMaterial = null; // active material set by usemtl

        try (BufferedReader br = new BufferedReader(new FileReader(objPath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("v")) {
                    double x = Double.parseDouble(parts[1]) * scale;
                    double y = Double.parseDouble(parts[2]) * scale;
                    double z = Double.parseDouble(parts[3]) * scale;
                    vertices.add(new Vector3D(x, y, z));

                } else if (parts[0].equals("vn")) {
                    double nx = Double.parseDouble(parts[1]);
                    double ny = Double.parseDouble(parts[2]);
                    double nz = Double.parseDouble(parts[3]);
                    normals.add(Vector3D.normalize(new Vector3D(nx, ny, nz)));

                } else if (parts[0].equals("vt")) {
                    double u = Double.parseDouble(parts[1]);
                    double v = Double.parseDouble(parts[2]);
                    uvs.add(new double[]{u, v});

                } else if (parts[0].equals("usemtl")) {
                    // Switch active material
                    String matName = parts[1];
                    currentMaterial = materials.getOrDefault(matName, null);
                    if (currentMaterial == null) {
                        System.out.println("Warning: material '" + matName + "' not found in MTL.");
                    }

                } else if (parts[0].equals("f")) {
                    int[] vIdx  = new int[parts.length - 1];
                    int[] vtIdx = new int[parts.length - 1];
                    int[] vnIdx = new int[parts.length - 1];
                    boolean hasNormals = false;
                    boolean hasUVs    = false;

                    for (int i = 1; i < parts.length; i++) {
                        String[] sub = parts[i].split("/");
                        vIdx[i - 1] = Integer.parseInt(sub[0]) - 1;

                        if (sub.length >= 2 && !sub[1].isEmpty()) {
                            vtIdx[i - 1] = Integer.parseInt(sub[1]) - 1;
                            hasUVs = true;
                        }
                        if (sub.length >= 3 && !sub[2].isEmpty()) {
                            vnIdx[i - 1] = Integer.parseInt(sub[2]) - 1;
                            hasNormals = true;
                        }
                    }

                    // Fan triangulation for quads and ngons
                    for (int i = 1; i < vIdx.length - 1; i++) {
                        Vector3D v0 = vertices.get(vIdx[0]);
                        Vector3D v1 = vertices.get(vIdx[i]);
                        Vector3D v2 = vertices.get(vIdx[i + 1]);

                        // Build triangle based on available data
                        if (currentMaterial != null && hasUVs && hasNormals) {
                            Vector3D n0 = normals.get(vnIdx[0]);
                            Vector3D n1 = normals.get(vnIdx[i]);
                            Vector3D n2 = normals.get(vnIdx[i + 1]);
                            double[] uv0 = uvs.get(vtIdx[0]);
                            double[] uv1 = uvs.get(vtIdx[i]);
                            double[] uv2 = uvs.get(vtIdx[i + 1]);
                            triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2, currentMaterial));

                        } else if (currentMaterial != null && hasUVs) {
                            double[] uv0 = uvs.get(vtIdx[0]);
                            double[] uv1 = uvs.get(vtIdx[i]);
                            double[] uv2 = uvs.get(vtIdx[i + 1]);
                            triangles.add(new Triangle(v0, v1, v2, uv0, uv1, uv2, currentMaterial));

                        } else if (hasNormals) {
                            Vector3D n0 = normals.get(vnIdx[0]);
                            Vector3D n1 = normals.get(vnIdx[i]);
                            Vector3D n2 = normals.get(vnIdx[i + 1]);
                            triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, Color.WHITE));

                        } else {
                            triangles.add(new Triangle(v0, v1, v2, Color.WHITE));
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + objPath);
            e.printStackTrace();
        }

        System.out.println("OBJ loaded: " + vertices.size() + " vertices, " +
                normals.size() + " normals, " + uvs.size() + " UVs, " +
                triangles.size() + " triangles.");
        return triangles;
    }

    // Original with no MTL, and flat color
    public static List<Triangle> read(String filePath, Color color, double scale) {
        List<Vector3D> vertices  = new ArrayList<>();
        List<Vector3D> normals   = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("v")) {
                    double x = Double.parseDouble(parts[1]) * scale;
                    double y = Double.parseDouble(parts[2]) * scale;
                    double z = Double.parseDouble(parts[3]) * scale;
                    vertices.add(new Vector3D(x, y, z));

                } else if (parts[0].equals("vn")) {
                    double nx = Double.parseDouble(parts[1]);
                    double ny = Double.parseDouble(parts[2]);
                    double nz = Double.parseDouble(parts[3]);
                    normals.add(Vector3D.normalize(new Vector3D(nx, ny, nz)));

                } else if (parts[0].equals("f")) {
                    int[] vIdx = new int[parts.length - 1];
                    int[] nIdx = new int[parts.length - 1];
                    boolean hasNormals = false;

                    for (int i = 1; i < parts.length; i++) {
                        String[] sub = parts[i].split("/");
                        vIdx[i - 1] = Integer.parseInt(sub[0]) - 1;
                        if (sub.length >= 3 && !sub[2].isEmpty()) {
                            nIdx[i - 1] = Integer.parseInt(sub[2]) - 1;
                            hasNormals = true;
                        }
                    }

                    for (int i = 1; i < vIdx.length - 1; i++) {
                        Vector3D v0 = vertices.get(vIdx[0]);
                        Vector3D v1 = vertices.get(vIdx[i]);
                        Vector3D v2 = vertices.get(vIdx[i + 1]);

                        if (hasNormals) {
                            triangles.add(new Triangle(v0, v1, v2,
                                    normals.get(nIdx[0]), normals.get(nIdx[i]), normals.get(nIdx[i + 1]), color));
                        } else {
                            triangles.add(new Triangle(v0, v1, v2, color));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + filePath);
            e.printStackTrace();
        }

        System.out.println("OBJ loaded: " + vertices.size() + " vertices, " +
                normals.size() + " normals, " + triangles.size() + " triangles.");
        return triangles;
    }
}
