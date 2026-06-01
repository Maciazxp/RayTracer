import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MTL {
    // Reads a .mtl file and returns a map of materialName -> Material
    public static Map<String, Material> read(String filePath) {
        Map<String, Material> materials = new HashMap<>();
        Material current = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("newmtl")) {
                    // Start a new material block
                    current = new Material(parts[1]);
                    materials.put(parts[1], current);

                } else if (parts[0].equals("map_Kd") && current != null) {
                    // Diffuse texture path — may contain spaces, so rejoin from index 1
                    String path = line.substring("map_Kd".length()).trim();
                    current.loadDiffuseTexture(path);
                }
                // Other fields (Ns, Ka, Ks, map_Bump, map_Ns, Ni, d, illum) ignored for now
            }

        } catch (IOException e) {
            System.err.println("Error reading MTL file: " + filePath);
            e.printStackTrace();
        }

        System.out.println("MTL loaded: " + materials.size() + " materials.");
        return materials;
    }
}
