import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonFileHandler {
    private final String filePath;

    public JsonFileHandler(String filePath) {
        this.filePath = filePath;
    }

    public JSONArray loadJsonArray() throws TaskManagerException {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
            return new JSONArray();
        } catch (IOException | ParseException e) {
            throw new TaskManagerException("Lỗi khi đọc file database", e);
        }
    }

    public void saveJsonArray(JSONArray data) throws TaskManagerException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(data.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new TaskManagerException("Lỗi khi ghi vào file database", e);
        }
    }
}