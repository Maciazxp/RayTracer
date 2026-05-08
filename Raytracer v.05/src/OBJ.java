import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJ {
    // Reads an OBJ file and returns a list of Triangles with the given color
    public static List<Triangle> read(String filePath, Color color, double scale) {
        List<Vector3D> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        List<Vector3D> normals  = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");

                if (parts[0].equals("v")) {
                    // Vertex position
                    double x = Double.parseDouble(parts[1]) * scale;
                    double y = Double.parseDouble(parts[2]) * scale;
                    double z = Double.parseDouble(parts[3]) * scale;
                    vertices.add(new Vector3D(x, y, z));

                } else if (parts[0].equals("vn")) {
                    // Vertex normal (no scaling needed for normals)
                    double nx = Double.parseDouble(parts[1]);
                    double ny = Double.parseDouble(parts[2]);
                    double nz = Double.parseDouble(parts[3]);
                    normals.add(Vector3D.normalize(new Vector3D(nx, ny, nz)));

                } else if (parts[0].equals("f")) {
                    // Face: can be f v f v/vt f v/vt/vn f v//vn
                    int[] vIndices = new int[parts.length - 1];
                    int[] nIndices = new int[parts.length - 1];
                    boolean faceHasNormals = false;

                    for (int i = 1; i < parts.length; i++) {
                        String[] subParts = parts[i].split("/");

                        // OBJ indices are 1-based, convert to 0-based
                        vIndices[i - 1] = Integer.parseInt(subParts[0]) - 1;

                        // Check if normal index exists (format v/vt/vn or v//vn)
                        if (subParts.length >= 3 && !subParts[2].isEmpty()) {
                            nIndices[i - 1] = Integer.parseInt(subParts[2]) - 1;
                            faceHasNormals = true;
                        }
                    }

                    // Each face is a triangle (3 vertices)
                    // If face has more than 3 vertices (quad), split into triangles
                    for (int i = 1; i < vIndices.length - 1; i++) {
                        Vector3D v0 = vertices.get(vIndices[0]);
                        Vector3D v1 = vertices.get(vIndices[i]);
                        Vector3D v2 = vertices.get(vIndices[i + 1]);

                        // If the face has normals, apply Phong Shading constructor
                        if (faceHasNormals) {
                            Vector3D n0 = normals.get(nIndices[0]);
                            Vector3D n1 = normals.get(nIndices[i]);
                            Vector3D n2 = normals.get(nIndices[i + 1]);
                            triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, color));
                        } else {
                            // Fallback to Flat Shading
                            triangles.add(new Triangle(v0, v1, v2, color));
                        }
                    }
                }
                // Lines starting with vt, o, g, s, are ignored (for now)
            }

        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + filePath);
            e.printStackTrace();
        }

        System.out.println("OBJ loaded: " + vertices.size() + " vertices, " + normals.size() + " normals, " + triangles.size() + " triangles.");
        return triangles;
    }
}
