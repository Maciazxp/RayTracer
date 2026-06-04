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
                    current = new Material(parts[1]);
                    materials.put(parts[1], current);

                } else if (parts[0].equals("map_Kd") && current != null) {
                    // Diffuse texture  (may have spaces in path so take everything after keyword xd)
                    String path = line.substring("map_Kd".length()).trim();
                    current.loadDiffuseTexture(path);

                } else if (parts[0].equals("map_Bump") && current != null) {
                    // Normal map: Blender exports it starting with this line: map_Bump -bm 1.000000 <path>
                    String texPath = extractTexturePath(line, "map_Bump");
                    current.loadNormalMap(texPath);
                }
                // Other fields (Ns, Ka, Ks, map_Ns, Ni, d, illum, Kr) ignored
            }

        } catch (IOException e) {
            System.err.println("Error reading MTL file: " + filePath);
            e.printStackTrace();
        }

        System.out.println("MTL loaded: " + materials.size() + " materials.");
        return materials;
    }

    // Extracts the texture path from a line, skipping any flags (like -bm 1.0)
    // Flags start with '-', so we skip pairs of (flag, value) until we find the path
    private static String extractTexturePath(String line, String keyword) {
        // Remove the keyword from the start
        String rest = line.substring(keyword.length()).trim();
        String[] tokens = rest.split("\\s+");

        // Walk through tokens: skip flag-value pairs (flag starts with '-')
        int i = 0;
        while (i < tokens.length - 1 && tokens[i].startsWith("-")) {
            i += 2; // skip the flag and its value
        }

        // Remaining tokens form the path (rejoin in case of spaces)
        StringBuilder path = new StringBuilder();
        for (int j = i; j < tokens.length; j++) {
            if (j > i) path.append(" ");
            path.append(tokens[j]);
        }
        return path.toString();
    }
}