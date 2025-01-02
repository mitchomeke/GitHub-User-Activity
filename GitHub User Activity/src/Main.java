import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    static File file = new File("user.json");
    static String username;
    static ObjectNode rootNode;
    static ArrayNode arrayNode;
    static final String FAILED = "Get Response Code failed: Response Error %d";

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ObjectMapper mapper = new ObjectMapper();
        do {
            username = reader.readLine();
            FetchApi(mapper);
        }while (!username.equals("quit"));
    }
    private static void FetchApi(ObjectMapper mapper) throws IOException {
        if (!file.exists()){
            file.createNewFile();
        }
        JsonNode node = mapper.readTree(file);
        if (node != null && node.isObject()){
            rootNode = (ObjectNode) node;
        }else {
            rootNode = mapper.createObjectNode();
            rootNode.putArray("github");
            mapper.writeValue(file,rootNode);
        }
        arrayNode = (ArrayNode) rootNode.get("github");
        URL url = URI.create("https://api.github.com/users/" + username + "/events").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode()==200){
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            reader.close();
            arrayNode.add(mapper.valueToTree(builder.toString()));
            mapper.writeValue(file,rootNode);
            for (JsonNode nodes: arrayNode) {
                System.out.println(nodes.toPrettyString());
            }
        }
        else {
            System.out.printf(FAILED,connection.getResponseCode());
        }
        connection.disconnect();
    }
}