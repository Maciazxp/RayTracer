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
                    // Vertex: v x y z
                    double x = Double.parseDouble(parts[1]) * scale;
                    double y = Double.parseDouble(parts[2]) * scale;
                    double z = Double.parseDouble(parts[3]) * scale;
                    vertices.add(new Vector3D(x, y, z));

                } else if (parts[0].equals("f")) {
                    // Face: can be f v f v/vt f v/vt/vn f v//vn
                    // We only care about the vertex indices (first number before any '/')
                    int[] indices = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        // Split by '/' and take only the first index (vertex index)
                        String indexStr = parts[i].split("/")[0];
                        // OBJ indices are 1-based, convert to 0-based
                        indices[i - 1] = Integer.parseInt(indexStr) - 1;
                    }

                    // Each face is a triangle (3 vertices)
                    // If face has more than 3 vertices (quad), split into triangles
                    for (int i = 1; i < indices.length - 1; i++) {
                        Vector3D v0 = vertices.get(indices[0]);
                        Vector3D v1 = vertices.get(indices[i]);
                        Vector3D v2 = vertices.get(indices[i + 1]);
                        triangles.add(new Triangle(v0, v1, v2, color));
                    }
                }
                // Lines starting with vn, vt, o, g, s, are ignored (for now)


            }

        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + filePath);
            e.printStackTrace();
        }

        System.out.println("OBJ loaded: " + vertices.size() + " vertices, " + triangles.size() + " triangles.");
        return triangles;
    }
}
 